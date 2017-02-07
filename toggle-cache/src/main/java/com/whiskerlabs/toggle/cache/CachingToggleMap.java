package com.whiskerlabs.toggle.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.whiskerlabs.toggle.Toggle;
import com.whiskerlabs.toggle.ToggleMap;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link ToggleMap} which caches toggles retrieved from an
 * underlying {@code ToggleMap}.
 *
 * @param <K> The key type of a {@code ToggleMap}.
 *
 * @param <T> The toggle input type. The toggle is applied to
 *            instances of type {@code T}.
 */
public class CachingToggleMap<K, T> extends ToggleMap<K, T> {
  private final Toggle<T> ALWAYS_FALSE = Toggle.alwaysFalse();

  private final ToggleMap<K, T> underlying;
  private final LoadingCache<K, Toggle<T>> cache;

  // We keep explicit references to the last-cached toggles in order
  // to fall back to it in cases where lookups to the underlying
  // ToggleMap fail.
  private final Map<K, Toggle<T>> fallbackCache;

  public CachingToggleMap(ToggleMap<K, T> underlying, CaffeineSpec cacheSpec) {
    this.underlying = underlying;
    this.cache = Caffeine.from(cacheSpec)
      .build(underlying::apply);
    this.fallbackCache = new HashMap<>();
  }

  public CachingToggleMap(ToggleMap<K, T> underlying, String cacheSpec) {
    this(underlying, CaffeineSpec.parse(cacheSpec));
  }

  @Override
  public Toggle<T> apply(K key) {
    return new Toggle<T>() {
      @Override
      protected boolean test(T t, Optional<String> cohortOpt) {
        Toggle<T> underlying = null;

        try {
          underlying = cache.get(key);
          fallbackCache.put(key, underlying);
        } catch (NoSuchElementException err) {
          underlying = fallbackCache.getOrDefault(key, ALWAYS_FALSE);
        }

        if (cohortOpt.isPresent()) {
          return underlying
            .withCohort(cohortOpt.get())
            .test(t);
        } else {
          return underlying.test(t);
        }
      }
    };
  }

  @Override
  public Set<K> keySet() {
    final Set<K> underlyingKeySet = underlying.keySet();
    underlyingKeySet.forEach(cache::refresh);
    return underlyingKeySet;
  }
}
