# toggle

A
[JSON Toggle](https://evanmeagher.net/2017/02/introducing-json-toggle)
ingestion library for Java 8.

JSON Toggle is a JSON document structure for specifying
[feature toggles](http://martinfowler.com/articles/feature-toggles.html). This
library provides a programming interface for writing boolean-valued
functions backed by toggle specifications stored in DynamoDB or in
flat JSON or YAML files.

## Usage

### ToggleMap

The first step is to construct a
[`ToggleMap`](toggle-core/src/main/java/com/whiskerlabs/toggle/ToggleMap.java),
backed by either DynamoDB or a file.

For example,

```
// Construct a ToggleMap backed by a YAML file.
ToggleMap<String, Integer> toggleMap =
  new JsonToggleMap.fromPath(Paths.get("/etc/toggle_spec.yml"));
```

or

```
// Construct a ToggleMap backed by a DynamoDB table.
Table dynamoDbTable = dynamoDbClient.getTable("production-toggles");
ToggleMap<String, Integer> toggleMap = new DynamoDbToggleMap<Integer>(dynamoDbTable);
```

Also, it's a good idea to wrap an underlying `ToggleMap` in a caching
decorator in order to reduce the read load on your backing
store. Caching is powered by
[Caffeine](https://github.com/ben-manes/caffeine), so the second
constructor argument here is a [Caffeine spec](https://static.javadoc.io/com.github.ben-manes.caffeine/caffeine/2.3.5/com/github/benmanes/caffeine/cache/CaffeineSpec.html):

```
// Construct a caching ToggleMap backed by a DynamoDB table.
ToggleMap<String, Integer> cachingToggleMap = new CachingToggleMap<>(
  toggleMap,
  "maximumSize=1000,expireAfterWrite=1m"
);
```

### Toggle

Individual toggles are identified by strings called _toggle
keys_. Toggles are created by applying a `ToggleMap` to a toggle key:

```
// Create a toggle backed by the "/feature/new_hotness" definition.
Toggle<Integer> fancyNewFeature = toggleMap.apply("/feature/new_hotness");
```

[`Toggle`](toggle-core/src/main/java/com/whiskerlabs/toggle/Toggle.java)
implements `java.util.function.Predicate`, meaning that in practice
they act as simple boolean-valued functions. Use them in your code to
predicate codepaths based on toggle probability:

```
// Use the toggle to guard some new functionality, based on a user ID.
if (fancyNewFeature.test(user.userId)) {
  // New hotness.
} else {
  // Old and busted.
}
```

By using toggles, conditional logic is made dynamically
configurable. This is a powerful and potentially-dangerous
technique. When predicating important codepaths with toggles, be sure
that you trust the backing store that providers toggle specifications.

## Packages

- `toggle-core` defines core API primitives in pure JDK 8 Java
- `toggle-cache` uses
  [Caffeine](https://github.com/ben-manes/caffeine) to memoize toggle
  lookups
- `toggle-dynamodb` reads toggle state from Amazon DynamoDB
- `toggle-json` uses [Jackson](https://github.com/FasterXML/jackson)
  to read toggle specifications from JSON or YAML files

## TODO

Refactoring:

- Formalize JSON string processing in e.g. a collection of model
  classes.

New packages:

- `toggle-sql`
- `toggle-dropwizard`

## Support

For questions or bug reports, please
[file an issue on Github](https://github.com/whiskerlabs/toggle/issues).

For any other inquiries, send mail to `software at whiskerlabs.com`.

## License

Copyright 2017 Whisker Labs

Licensed under the MIT License. See LICENSE for details.
