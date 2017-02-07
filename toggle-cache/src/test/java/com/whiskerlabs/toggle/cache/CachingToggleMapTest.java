package com.whiskerlabs.toggle.cache;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.whiskerlabs.toggle.Toggle;
import com.whiskerlabs.toggle.ToggleMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CachingToggleMapTest {
  private static final String onKey = "/feature/always_on";
  private static final String offKey = "/feature/always_off";
  private static final Toggle<Integer> toggle = Toggle.alwaysTrue();

  private final ToggleMap<String, Integer> underlying = mock(TestToggleMap.class);

  @Test
  public void testReadFromUnderlyingToggleMapOnMiss() {
    when(underlying.apply(onKey)).thenReturn(toggle);

    final CachingToggleMap<String, Integer> toggleMap =
      new CachingToggleMap<>(underlying, CaffeineSpec.parse("maximumSize=3"));

    assertThat(toggleMap.apply(onKey).test(1)).isTrue();
    verify(underlying).apply(eq(onKey));
  }

  @Test
  public void testReadFromCacheOnHit() {
    when(underlying.apply(onKey)).thenReturn(toggle);

    final CachingToggleMap<String, Integer> toggleMap =
      new CachingToggleMap<>(underlying, CaffeineSpec.parse("maximumSize=3"));

    toggleMap.apply(onKey).test(1);
    toggleMap.apply(onKey).test(2);
    verify(underlying, times(1)).apply(eq(onKey));
  }

  @Test
  public void testCohortsPassedThrough() {
    final Toggle<Integer> mockToggle = mock(Toggle.class);
    when(mockToggle.withCohort(anyString())).thenReturn(mockToggle);
    when(mockToggle.test(eq(1))).thenReturn(toggle.test(1));
    when(underlying.apply(onKey)).thenReturn(mockToggle);

    final CachingToggleMap<String, Integer> toggleMap =
      new CachingToggleMap<>(underlying, CaffeineSpec.parse("maximumSize=3"));

    toggleMap.apply(onKey)
      .withCohort("foo")
      .test(1);

    verify(underlying, times(1)).apply(eq(onKey));
    verify(mockToggle, times(1)).withCohort(eq("foo"));
    verify(mockToggle, times(1)).test(eq(1));
  }

  @Test
  public void testFallsBackToLastReadToggleOnFailedReadFromUnderlying() {
    when(underlying.apply(onKey))
      .thenReturn(toggle)
      .thenThrow(new NoSuchElementException());

    final CachingToggleMap<String, Integer> toggleMap =
      new CachingToggleMap<>(underlying, CaffeineSpec.parse("maximumSize=0"));

    final Toggle<Integer> t = toggleMap.apply(onKey);
    assertThat(t.test(1)).isTrue();
    assertThat(t.test(1)).isTrue();
    verify(underlying, times(2)).apply(eq(onKey));
  }

  @Test
  public void testReadKeySetFromUnderlying() {
    final Set<String> expectedKeySet = new HashSet();
    expectedKeySet.add(onKey);
    expectedKeySet.add(offKey);
    when(underlying.keySet()).thenReturn(expectedKeySet);

    final CachingToggleMap<String, Integer> toggleMap =
      new CachingToggleMap<>(underlying, CaffeineSpec.parse("maximumSize=3"));

    assertThat(toggleMap.keySet()).containsOnly(onKey, offKey);
  }
}
