package com.whiskerlabs.toggle.dynamodb;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.whiskerlabs.toggle.Toggle;
import com.whiskerlabs.toggle.ToggleMap;
import com.whiskerlabs.toggle.json.ToggleJsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link ToggleMap} backed by an Amazon DynamoDB table.
 *
 * Toggles are defined by items stored in the backing DynamoDB
 * table. There is a one-to-one correspondence between toggles and
 * items in the table.
 *
 * The DynamoDB table is consulted on each toggle test, meaning that
 * toggle application entails a network call. Users are advised to
 * wrap a {@code DynamoDbToggleMap} in a {@code CachingToggleMap} in
 * order to limit the frequency with which the underlying {@code
 * JsonNode} is consulted.
 *
 * @param <T> The toggle input type. The toggle is applied to
 *            instances of type {@code T}.
 */
public class DynamoDbToggleMap<T> extends ToggleMap<String, T> {
  private static final ScanSpec KEY_SET_SCAN_SPEC = new ScanSpec()
    .withProjectionExpression("toggle_key");

  private static final Logger logger = LoggerFactory.getLogger(DynamoDbToggleMap.class);

  private final Table dynamoDbTable;

  public DynamoDbToggleMap(Table dynamoDbTable) {
    this.dynamoDbTable = dynamoDbTable;
  }

  @Override
  public Toggle<T> apply(String key) {
    final Item item = dynamoDbTable.getItem("toggle_key", key);

    if (item == null) {
      // TODO: Log the error.
      throw new NoSuchElementException(Toggle.UNABLE_TO_LOOK_UP_KEY_PREFIX + key);
    }

    final JsonNode toggleNode = ToggleJsonNode.fromString(item.toJSON());

     if (toggleNode.isMissingNode()) {
       throw new NoSuchElementException(Toggle.UNABLE_TO_LOOK_UP_KEY_PREFIX + key);
     }

    return new Toggle<T>() {
      @Override
      protected boolean test(T t, Optional<String> cohortOpt) {
        final JsonNode filterNode =
          ToggleJsonNode.findByCohort(toggleNode.path("filter"), cohortOpt);

        if (filterNode.isMissingNode()) {
          // No filter found for cohort. Weight result by default value.
          return nextBoolean(toggleNode.path("toggle_value").intValue());
        } else {
          // Filter found for cohort. Weight result by the filter's value.
          return nextBoolean(filterNode.path("toggle_value").intValue());
        }
      }
    };
  }

  @Override
  public Set<String> keySet() {
    final HashSet<String> builder = new HashSet<>();

    for (final Item item : dynamoDbTable.scan(KEY_SET_SCAN_SPEC)) {
      builder.add(item.getString("toggle_key"));
    }

    return Collections.unmodifiableSet(builder);
  }
}
