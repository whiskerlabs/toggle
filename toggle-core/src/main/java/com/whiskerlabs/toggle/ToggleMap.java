package com.whiskerlabs.toggle;

import java.util.Set;
import java.util.function.Function;

/**
 * A mapping between a key type {@code K} and a collection of {@link
 * Toggle Toggles} of type {@code T}.
 *
 * A {@code ToggleMap} serves as a function for looking up toggle
 * objects by their canonical keys.
 *
 * @param <K> The key type of a {@code ToggleMap}.
 *
 * @param <T> The toggle input type. The toggle is applied to
 *            instances of type {@code T}.
 */
public abstract class ToggleMap<K, T> implements Function<K, Toggle<T>> {
  /**
   * Returns a {@link Set} view of the keys (i.e. the feature names or
   * paths) contained in this toggle map.
   *
   * @return A set view of the keys contained in this toggle map
   */
  public abstract Set<K> keySet();
}
