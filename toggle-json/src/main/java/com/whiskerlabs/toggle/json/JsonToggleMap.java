package com.whiskerlabs.toggle.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.whiskerlabs.toggle.Toggle;
import com.whiskerlabs.toggle.ToggleMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * A {@link ToggleMap} backed by a dynamic JSON toggle specification.
 *
 * A JSON toggle specification is provided as a thunk producing an
 * iterator of {@link JsonNode JsonNodes} (i.e. a {@link
 * Supplier}{@code <Iterator<JsonNode>>}). This JSON tree is consulted
 * on each toggle test, meaning that toggle complexity is {@code O(n)}
 * for a toggle spec containing {@code n} toggles. Users are advised
 * to wrap a {@code JsonToggleMap} in a {@code CachingToggleMap} in
 * order to limit the frequency with which the underlying {@code
 * JsonNode} is consulted.
 *
 * @param <T> The toggle input type. The toggle is applied to
 *            instances of type {@code T}.
 */
public class JsonToggleMap<T> extends ToggleMap<String, T> {
  public static final YAMLFactory DEFAULT_YAML_FACTORY = new YAMLFactory();
  public static final ObjectReader DEFAULT_OBJECT_READER =
    (new ObjectMapper()).reader();

  private final Supplier<Iterator<JsonNode>> jsonNodeSupplier;

  /**
   * Constructs a {@code JsonToggleMap} from a {@link Path}
   * representing a toggle specification file.
   *
   * Toggle specification file can be in either JSON or YAML format.
   *
   * @param <T> The toggle input type. The toggle is applied to
   *            instances of type {@code T}.
   * @param path A {@code Path} to a toggle specification file on the
   *        local file system.
   * @return A {@code JsonToggleMap} backed by a toggle specification
   *         drawn from the given local file.
   */
  public static <T> JsonToggleMap<T> fromPath(Path path) {
    return new JsonToggleMap<T>(() -> {
      try {
        final YAMLParser yamlParser = DEFAULT_YAML_FACTORY.createParser(path.toFile());
        return DEFAULT_OBJECT_READER.<JsonNode>readTree(yamlParser).elements();
      } catch (IOException err) {
        return DEFAULT_OBJECT_READER.createArrayNode().elements();
      }
    });
  }

  public JsonToggleMap(Supplier<Iterator<JsonNode>> jsonNodeSupplier) {
    this.jsonNodeSupplier = jsonNodeSupplier;
  }

  @Override
  public Toggle<T> apply(String key) {
    final JsonNode toggleNode =
      ToggleJsonNode.findByKey(jsonNodeSupplier.get(), key);

    if (toggleNode.isMissingNode()) {
      // TODO: Log the error.
      throw new NoSuchElementException(Toggle.UNABLE_TO_LOOK_UP_KEY_PREFIX + key);
    }

    return new Toggle<T>() {
      @Override
      protected boolean test(T t, Optional<String> cohortOpt) {
        final JsonNode filterNode =
        ToggleJsonNode.findByCohort(toggleNode.path("filter"), cohortOpt);

        if (filterNode.isMissingNode()) {
          // No filter found for cohort. Weight result by default value.
          return nextBoolean(toggleNode.path("value").intValue());
        } else {
          // Filter found for cohort. Weight result by the filter's value.
          return nextBoolean(filterNode.path("value").intValue());
        }
      }
    };
  }

  /**
   * Returns a {@link Set} view of the keys (i.e. the feature names or
   * paths) contained in this toggle map.
   *
   * @return A set view of the keys contained in this toggle map
   */
  @Override
  public Set<String> keySet() {
    final HashSet<String> builder = new HashSet<>();

    jsonNodeSupplier.get().forEachRemaining(node ->
      builder.add(node.path("key").textValue())
    );

    return Collections.unmodifiableSet(builder);
  }
}
