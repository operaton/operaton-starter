# starter-archetypes

## Role

Defines the `GenerationClient` interface and provides the Maven archetype integration for operaton-starter's generation engine.

This module is the extension point for alternative generation backends. The default implementation in `starter-server` calls `starter-templates` in-process; `starter-archetypes` provides the `mvn archetype:generate` integration path.

## Prerequisites

- Java 21+
- Maven 3.9+

## Build in Isolation

```bash
mvn verify -pl starter-archetypes -am
```

## Run / Use Locally

`starter-archetypes` is a library — it has no standalone process. It is consumed by `starter-server` as a Maven dependency.

The `GenerationClient` interface it defines allows the generation engine to be swapped out for alternative backends (e.g., calling a remote archetype catalog).

## Example

```bash
mvn test -pl starter-archetypes
```

See the [root README](../README.md#architecture) for how all modules interact.
