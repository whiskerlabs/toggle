package com.whiskerlabs.toggle.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ToggleJsonNodeTest {
  @Test
  public void producesJsonNodeFromString() {
    final JsonNode node = ToggleJsonNode.fromString("{\"foo\": 4, \"bar\": true}");
    assertThat(node.isMissingNode()).isFalse();
    assertThat(node.fieldNames()).containsOnly("foo", "bar");
    assertThat(node.path("foo").intValue()).isEqualTo(4);
    assertThat(node.path("bar").booleanValue()).isTrue();
  }

  @Test
  public void producesMissingNodeForMalformedJsonString() {
    final JsonNode node = ToggleJsonNode.fromString("not a json string");
    assertThat(node.isMissingNode()).isTrue();
  }

  @Test
  public void findsToggleNodeByKey() {
    final String key = "/feature/admin_widget";
    final ObjectNode expected = JsonNodeFactory.instance.objectNode()
    .put("key", key)
    .put("value", 10000);

    final List<JsonNode> list = new ArrayList<>();
    list.add(expected);

    final JsonNode found = ToggleJsonNode.findByKey(list.iterator(), key);
    assertThat(found).isEqualTo(expected);
  }

  @Test
  public void returnsMissingNodeOnNonexistentKey() {
    final ObjectNode expected = JsonNodeFactory.instance.objectNode()
      .put("key", "/feature/admin_widget")
      .put("value", 10000);

    final List<JsonNode> list = new ArrayList<>();
    list.add(expected);

    final JsonNode found = ToggleJsonNode.findByKey(list.iterator(), "/feature/na");
    assertThat(found.isMissingNode()).isTrue();
  }

  @Test
  public void returnsTrueOnMatchedCohort() {
    final String cohort = "employee";
    final ObjectNode expected = JsonNodeFactory.instance.objectNode()
      .put("type", "cohort")
      .put("target", cohort)
      .put("value", 10000);
    assertThat(ToggleJsonNode.matchesCohort(expected, Optional.of(cohort))).isTrue();
  }

  @Test
  public void returnsFalseOnMismatchedCohort() {
    final ObjectNode expected = JsonNodeFactory.instance.objectNode()
      .put("type", "cohort")
      .put("target", "employee")
      .put("value", 10000);
    assertThat(ToggleJsonNode.matchesCohort(expected, Optional.of("na"))).isFalse();
  }

  @Test
  public void returnsFalseOnMalformedJsonNode() {
    final ObjectNode expected = JsonNodeFactory.instance.objectNode()
      .put("type", 4)
      .put("bar", true);
    assertThat(ToggleJsonNode.matchesCohort(expected, Optional.of("na"))).isFalse();
  }

  @Test
  public void findsFilterNodeByCohortFromObjectNode() {
    final String cohort = "employee";
    final ObjectNode expected = JsonNodeFactory.instance.objectNode()
      .put("type", "cohort")
      .put("target", cohort)
      .put("value", 10000);

    assertThat(ToggleJsonNode.findByCohort(expected, Optional.of(cohort))).isEqualTo(expected);
  }

  @Test
  public void findsFilterNodeByCohortFromArrayNode() {
    final String cohort = "employee";
    final ObjectNode filterNode = JsonNodeFactory.instance.objectNode()
      .put("type", "cohort")
      .put("target", cohort)
      .put("value", 10000);

    final ArrayNode expected = JsonNodeFactory.instance.arrayNode()
      .add(filterNode);

    assertThat(ToggleJsonNode.findByCohort(expected, Optional.of(cohort))).isEqualTo(filterNode);
  }

  @Test
  public void returnsMissingNodeForNonexistentCohortInObjectNode() {
    final ObjectNode expected = JsonNodeFactory.instance.objectNode()
      .put("type", "cohort")
      .put("target", "employee")
      .put("value", 10000);

    assertThat(ToggleJsonNode.findByCohort(expected, Optional.of("na")).isMissingNode()).isTrue();
  }

  @Test
  public void returnsMissingNodeForNonexistentCohortInArrayNode() {
    final ObjectNode filterNode = JsonNodeFactory.instance.objectNode()
      .put("type", "cohort")
      .put("target", "employee")
      .put("value", 10000);

    final ArrayNode expected = JsonNodeFactory.instance.arrayNode()
      .add(filterNode);

    assertThat(ToggleJsonNode.findByCohort(expected, Optional.of("na")).isMissingNode()).isTrue();
  }
}
