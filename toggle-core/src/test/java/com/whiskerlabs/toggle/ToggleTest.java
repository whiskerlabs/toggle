package com.whiskerlabs.toggle;

import org.junit.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class ToggleTest {
  @Test
  public void testAlwaysReturnsTrue() {
    final Predicate<String> p = Toggle.alwaysTrue();
    assertThat(p.test("")).isTrue();
    assertThat(p.test("hi")).isTrue();
  }

  @Test
  public void testAlwaysReturnsFalse() {
    final Predicate<String> p = Toggle.alwaysFalse();
    assertThat(p.test("")).isFalse();
    assertThat(p.test("hi")).isFalse();
  }
}
