package com.whiskerlabs.toggle.cache;

import com.whiskerlabs.toggle.Toggle;
import com.whiskerlabs.toggle.ToggleMap;

import java.util.Collections;
import java.util.Set;

public class TestToggleMap extends ToggleMap<String, Integer> {
  @Override
  public Toggle<Integer> apply(String key) {
    return Toggle.alwaysTrue();
  }

  @Override
  public Set<String> keySet() {
    return Collections.emptySet();
  }
}
