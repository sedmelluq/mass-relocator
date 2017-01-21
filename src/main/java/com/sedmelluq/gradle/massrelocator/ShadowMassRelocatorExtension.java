package com.sedmelluq.gradle.massrelocator;

import java.util.HashSet;
import java.util.Set;

public class ShadowMassRelocatorExtension {
  private final Set<String> exclusionPrefixes = new HashSet<>();
  private String base = "moved/";

  public void base(String base) {
    this.base = base + (base.endsWith("/") ? "" : "/");
  }

  public void excludePrefix(String excludePrefix) {
    exclusionPrefixes.add(excludePrefix);
  }

  public Set<String> getExclusionPrefixes() {
    return exclusionPrefixes;
  }

  public String getRelocateBase() {
    return base;
  }
}
