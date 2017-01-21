package com.sedmelluq.gradle.massrelocator;

import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator;

import java.util.HashSet;
import java.util.Set;

public class ShadowMassRelocator implements Relocator {
  private final String base;
  private final Set<String> inclusions = new HashSet<>();
  private final Set<String> exclusions = new HashSet<>();
  private final Set<String> exclusionPrefixes = new HashSet<>();

  public ShadowMassRelocator(String base) {
    this.base = base;
  }

  @Override
  public boolean canRelocatePath(String clazz) {
    return canRelocateFile(clazz);
  }

  @Override
  public String relocatePath(String clazz) {
    return base + clazz;
  }

  @Override
  public boolean canRelocateClass(String clazz) {
    return clazz.endsWith(".class") && canRelocateFile(clazz);
  }

  @Override
  public String relocateClass(String clazz) {
    return base + clazz;
  }

  @Override
  public String applyToSourceContent(String sourceContent) {
    throw new UnsupportedOperationException();
  }

  public void excludePackage(String packageName) {
    exclusions.add(packageName);
  }

  public void includePackage(String packageName) {
    inclusions.add(packageName);
  }

  public void excludePrefix(String excludePrefix) {
    exclusionPrefixes.add(excludePrefix);
  }

  private boolean canRelocateFile(String path) {
    int lastSlash = path.lastIndexOf('/');
    if (lastSlash != -1) {
      String packageName = path.substring(0, lastSlash);
      return !isExcludedByPrefix(path) && !exclusions.contains(packageName) && inclusions.contains(packageName);
    }
    return false;
  }

  private boolean isExcludedByPrefix(String path) {
    for (String exclusionPrefix : exclusionPrefixes) {
      if (path.startsWith(exclusionPrefix)) {
        return true;
      }
    }
    return false;
  }
}
