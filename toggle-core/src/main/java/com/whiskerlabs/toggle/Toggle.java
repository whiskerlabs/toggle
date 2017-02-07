package com.whiskerlabs.toggle;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * A {@code Toggle} is a reusable, dynamically-configurable predicate
 * (boolean-valued function).
 *
 * @param <T> The toggle input type. The toggle is applied to
 *            instances of type {@code T}.
 */
public abstract class Toggle<T> implements Predicate<T>, Cohortable<Toggle<T>> {
  public static final String UNABLE_TO_LOOK_UP_KEY_PREFIX =
    "Unable to look up toggle state for key ";

  /**
   * Returns a {@link Predicate} which returns {@code true} for all
   * instances of type {@code T}.
   *
   * @param <T> The toggle input type. The toggle is applied to
   *            instances of type {@code T}.
   * @return A {@code Toggle} whose {@code test} method always returns
   *         {@code true}.
   */
  public static <T> Toggle<T> alwaysTrue() {
    return new Toggle<T>() {
      @Override
      protected boolean test(T t, Optional<String> cohortOpt) {
        return true;
      }
    };
  }

  /**
   * Returns a {@link Predicate} which returns {@code false} for all
   * instances of type {@code T}.
   *
   * @param <T> The toggle input type. The toggle is applied to
   *            instances of type {@code T}.
   * @return A {@code Toggle} whose {@code test} method always returns
   *         {@code false}.
   */
  public static <T> Toggle<T> alwaysFalse() {
    return new Toggle<T>() {
      @Override
      protected boolean test(T t, Optional<String> cohortOpt) {
        return false;
      }
    };
  }

  /**
   * Returns a weighted pseudorandom boolean value.
   *
   * Results are weight towards true according to the {@code weight}
   * parameter. Precisely, for a weight {@code w}, the probability
   * that this method returns true is {@code w/10000}.
   *
   * @param weight An integer which weights the returned boolean
   *        towards true, up to 10,000.
   * @return {@code true} with probability {@code w/10000} for a
   *         weight {@code w}. {@code false} otherwise.
   */
  protected static boolean nextBoolean(int weight) {
    return (weight == 0)
      ? false
      : ThreadLocalRandom.current().nextInt(10000 / weight) == 0;
  }

  @Override
  public Toggle<T> withCohort(String cohort) {
    final Optional<String> cohortOpt = Optional.of(cohort);
    final Toggle<T> outer = this;

    return new Toggle<T>() {
      @Override
      public boolean test(T t) {
        return outer.test(t, cohortOpt);
      }

      @Override
      protected boolean test(T t, Optional<String> cohortOpt) {
        return outer.test(t, cohortOpt);
      }
    };
  }

  @Override
  public boolean test(T t) {
    return test(t, Optional.empty());
  }

  /**
   * Evaluates this predicate on the given argument.
   *
   * An optional cohort may be provided, in which case the toggle can
   * be targeted by an applicable toggle filter.
   *
   * @param t The input of this toggle.
   * @param cohortOpt An optional cohort string used to match this
   *        toggle with an applicable toggle filter.
   * @return {@code true} with some probablility defined by the
   *         applicable toggle specification.
   */
  protected abstract boolean test(T t, Optional<String> cohortOpt);
}
