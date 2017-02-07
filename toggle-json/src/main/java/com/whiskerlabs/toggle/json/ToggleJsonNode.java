package com.whiskerlabs.toggle.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * A collection of functions which produce or otherwise pertain to
 * {@link JsonNode} objects that define toggles.
 */
public class ToggleJsonNode {
  public static final ObjectReader DEFAULT_OBJECT_READER =
    (new ObjectMapper()).reader();

  /**
   * Constructs a {@link JsonNode} from a JSON string.
   *
   * @param json A string encoding a JSON object
   * @return A {@code JsonNode} if the argument string is valid, otherwise
   *         {@link MissingNode}.
   */
  public static JsonNode fromString(String json) {
    try {
      return DEFAULT_OBJECT_READER.readTree(json);
    } catch (IOException err) {
      return MissingNode.getInstance();
    }
  }

  /**
   * Traverses the argument {@link Iterator Iterator}{@code
   * <JsonNode>} to find any contained {@link JsonNode} which match
   * the argument key, if present.
   *
   * If no {@code JsonNode} matching the argument key is found, then
   * {@link MissingNode} is returned.
   *
   * @param iterator An {@code Iterator<JsonNode>} to traverse
   * @param key A toggle key for which to search
   * @return A {@code JsonNode} matching the specified key, or else
   *         {@link MissingNode}.
   */
  public static JsonNode findByKey(Iterator<JsonNode> iterator, String key) {
    while (iterator.hasNext()) {
      final JsonNode node = iterator.next();

      if (node.path("key").textValue().equals(key)) {
        return node;
      }
    }

    return MissingNode.getInstance();
  }

  /**
   * Returns true if the argument {@link JsonNode} is an object and
   * has a cohort field which matches the argument cohort, if present.
   *
   * @param node A {@code JsonNode} to scan for cohort information.
   * @param cohortOpt An optional cohort string to match against the
   *        argument {@code JsonNode}.
   * @return {@code true} if the {@code JsonNode} is an object and has
   *         a cohort field which matches the argument cohort, if
   *         present.
   */
  public static boolean matchesCohort(JsonNode node, Optional<String> cohortOpt) {
    return node.isObject()
      && node.hasNonNull("type")
      && node.path("type").isTextual()
      && node.path("type").textValue().equals("cohort")
      && cohortOpt.isPresent()
      && node.hasNonNull("target")
      && node.path("target").isTextual()
      && node.path("target").textValue().equals(cohortOpt.get());
  }

  /**
   * Traverses the argument {@link JsonNode} to find any contained
   * {@code JsonNode JsonNodes} which match the argument cohort, if
   * present.
   *
   * If {@code node} is an {@code ArrayNode}, then the array is
   * traversed and the first matching filter node, if any, is
   * returned.
   *
   * @param node A {@code JsonNode} to traverse in search of cohort
   *        information.
   * @param cohortOpt An optional cohort string to match against the
   *        argument {@code JsonNode}.
   * @return A {@code JsonNode} matching the specified cohort, or else
   *         {@link MissingNode}.
   */
  public static JsonNode findByCohort(JsonNode node, Optional<String> cohortOpt) {
    if (node.isObject() && ToggleJsonNode.matchesCohort(node, cohortOpt)) {
      return node;
    } else if (node.isArray()) {
      final Iterator<JsonNode> iterator = node.elements();

      while (iterator.hasNext()) {
        final JsonNode containedNode = iterator.next();
        if (containedNode.isObject() && ToggleJsonNode.matchesCohort(containedNode, cohortOpt)) {
          return containedNode;
        }
      }

      return MissingNode.getInstance();
    } else {
      return MissingNode.getInstance();
    }
  }
}
