package com.whiskerlabs.toggle;

/**
 * Represents an entity which is composable with a cohort identifiable
 * by a canonical string.
 *
 * @param <T> A supertype which implements {Cohortable}.
 */
interface Cohortable<T> {
  /**
   * Composes this object with a cohort identified by a {@code
   * String}.
   *
   * Used as the means of matching a cohort with any configured cohort
   * filter that is applicable.
   */
  public T withCohort(String cohort);
}
