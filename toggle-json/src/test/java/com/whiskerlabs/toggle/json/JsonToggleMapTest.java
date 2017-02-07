package com.whiskerlabs.toggle.json;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonToggleMapTest {
  private static final Path YAML_FIXTURE = ResourcePath.get("toggle_spec.yml");
  private static final Path JSON_FIXTURE = ResourcePath.get("toggle_spec.json");
  private static final Path NONEXISTENT_FIXTURE = Paths.get("nonexistent.yml");
  private static final Path INVALID_FIXTURE = ResourcePath.get("not_a_toggle_spec.html");

  private static final String adminKey = "/feature/admin_widget";
  private static final String dogfoodKey = "/feature/dogfood_widget";
  private static final String incRolloutKey = "/feature/incremental_rollout";
  private static final String abKey = "/feature/ab_test";
  private static final String offKey = "/feature/always_off";

  @Test
  public void testEmptyToggleMapOnNonExistentToggleSpec() {
    final JsonToggleMap<Integer> toggleMap = JsonToggleMap.fromPath(NONEXISTENT_FIXTURE);
    assertThat(toggleMap.keySet()).isEmpty();
  }

  @Test
  public void testEmptyToggleMapOnInvalidToggleSpec() {
    final JsonToggleMap<Integer> toggleMap = JsonToggleMap.fromPath(INVALID_FIXTURE);
    assertThat(toggleMap.keySet()).isEmpty();
  }

  @Test
  public void testReturnsKeysYaml() {
    final JsonToggleMap<Integer> toggleMap = JsonToggleMap.fromPath(YAML_FIXTURE);

    assertThat(toggleMap.keySet())
      .containsOnly(adminKey, dogfoodKey, incRolloutKey, abKey, offKey);
  }

  @Test
  public void testProducesTogglesYaml() {
    final JsonToggleMap<Integer> toggleMap = JsonToggleMap.fromPath(YAML_FIXTURE);

    assertThat(toggleMap.apply(adminKey).test(1)).isFalse();
    assertThat(toggleMap.apply(adminKey).withCohort("admin").test(1)).isTrue();
    assertThat(toggleMap.apply(offKey).test(1)).isFalse();
  }

  @Test
  public void testProducesFalseTogglesOnNonexistentKeysYaml() {
    final JsonToggleMap<Integer> toggleMap = JsonToggleMap.fromPath(YAML_FIXTURE);
    assertThatThrownBy(() -> toggleMap.apply("/feature/nonexistent").test(1))
      .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  public void testReturnsKeysJson() {
    final JsonToggleMap<Integer> toggleMap = JsonToggleMap.fromPath(JSON_FIXTURE);

    assertThat(toggleMap.keySet())
      .containsOnly(adminKey, dogfoodKey, incRolloutKey, abKey, offKey);
  }

  @Test
  public void testProducesTogglesJson() {
    final JsonToggleMap<Integer> toggleMap = JsonToggleMap.fromPath(JSON_FIXTURE);

    assertThat(toggleMap.apply(adminKey).test(1)).isFalse();
    assertThat(toggleMap.apply(adminKey).withCohort("admin").test(1)).isTrue();
    assertThat(toggleMap.apply(offKey).test(1)).isFalse();
  }

  @Test
  public void testProducesFalseTogglesOnNonexistentKeysJson() {
    final JsonToggleMap<Integer> toggleMap = JsonToggleMap.fromPath(JSON_FIXTURE);
    assertThatThrownBy(() -> toggleMap.apply("/feature/nonexistent").test(1))
      .isInstanceOf(NoSuchElementException.class);
  }
}
