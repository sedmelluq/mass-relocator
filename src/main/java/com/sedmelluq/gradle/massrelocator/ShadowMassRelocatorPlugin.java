package com.sedmelluq.gradle.massrelocator;

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.objectweb.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ShadowMassRelocatorPlugin implements Plugin<Project> {
  @Override
  public void apply(Project target) {
    Task shadowTask = target.getTasks().getByName(ShadowJavaPlugin.getSHADOW_JAR_TASK_NAME());
    if (shadowTask instanceof ShadowJar) {
      shadowTask.doFirst(shadowJar -> processShadowJar((ShadowJar) shadowJar));
    }

    target.getExtensions().add("massRelocator", new ShadowMassRelocatorExtension());
  }

  private void processShadowJar(ShadowJar shadowJar) {
    ShadowMassRelocatorExtension extension = shadowJar.getProject().getExtensions().getByType(ShadowMassRelocatorExtension.class);
    ShadowMassRelocator relocator = new ShadowMassRelocator(extension.getRelocateBase());
    processDependencies(shadowJar.getIncludedDependencies(), relocator);
    copyExcludedPrefixes(extension, relocator);
    shadowJar.relocate(relocator);
  }

  private void copyExcludedPrefixes(ShadowMassRelocatorExtension extension, ShadowMassRelocator relocator) {
    extension.getExclusionPrefixes().forEach(relocator::excludePrefix);
  }

  private void processDependencies(Iterable<File> files, ShadowMassRelocator relocator) {
    for (File file : files) {
      if (file.isDirectory()) {
        processDirectory(file, relocator);
      } else {
        processArchive(file, relocator);
      }
    }
  }

  private void processArchive(File archive, ShadowMassRelocator relocator) {
    try (ZipInputStream zip = new ZipInputStream(new FileInputStream(archive))) {
      ZipEntry entry;

      while ((entry = zip.getNextEntry()) != null) {
        if (entry.getName().endsWith(".class")) {
          processClass(new BufferedInputStream(zip), relocator);
        }
      }
    } catch (IOException e) {
      throw new GradleException("Failed to read dependency jar: " + archive.getAbsolutePath(), e);
    }
  }

  private void processDirectory(File directory, ShadowMassRelocator relocator) {
    for (File file : directory.listFiles()) {
      if (file.isDirectory()) {
        processDirectory(directory, relocator);
      } else if (file.getName().endsWith(".class")) {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
          processClass(stream, relocator);
        } catch (IOException e) {
          throw new GradleException("Failed to read dependency file: " + file.getAbsolutePath(), e);
        }
      }
    }
  }

  private void processClass(InputStream inputStream, ShadowMassRelocator relocator) throws IOException {
    ClassReader reader = new ClassReader(inputStream);
    NativeMethodDetectingVisitor nativeMethodDetector = new NativeMethodDetectingVisitor();
    reader.accept(nativeMethodDetector, 0);

    int lastSlash = reader.getClassName().lastIndexOf('/');
    if (lastSlash != -1) {
      String packageName = reader.getClassName().substring(0, lastSlash);

      if (nativeMethodDetector.hasNativeMethods()) {
        relocator.excludePackage(packageName);
      } else {
        relocator.includePackage(packageName);
      }
    }
  }
}
