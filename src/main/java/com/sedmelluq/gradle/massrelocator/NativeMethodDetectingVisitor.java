package com.sedmelluq.gradle.massrelocator;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ACC_NATIVE;

public class NativeMethodDetectingVisitor extends ClassVisitor {
  private boolean hasNativeMethods;

  public NativeMethodDetectingVisitor() {
    super(Opcodes.ASM5);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    if ((access & ACC_NATIVE) != 0) {
      hasNativeMethods = true;
    }
    return null;
  }

  public boolean hasNativeMethods() {
    return hasNativeMethods;
  }
}
