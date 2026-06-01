# starter-templates

## Role

Pure-Java generation engine for operaton-starter. Produces project ZIP archives from JTE templates. Has zero Spring dependency — it is a library, not a service.

JTE templates are precompiled at build time, which means the generation path is fully type-safe and has no runtime template parsing overhead.

## Prerequisites

- Java 21+
- Maven 3.9+

## Build in Isolation

```bash
mvn verify -pl starter-templates -am
```

The `-am` flag builds all Maven dependencies of this module automatically.

## Run / Use Locally

`starter-templates` is a library — it has no standalone process. Invoke it programmatically from Java:

```java
GenerationEngine engine = new GenerationEngine();
ProjectConfig config = new ProjectConfig();
config.setProjectType("PROCESS_APPLICATION");
config.setBuildSystem("MAVEN");
config.setGroupId("com.example");
config.setArtifactId("my-app");
config.setProjectName("My App");
config.setJavaVersion(21);
config.setDockerCompose(false);
config.setGithubActions(true);

byte[] zip = engine.generate(config);
assert zip.length > 0;
```

## Example

Running the existing test suite exercises all 6 project-type × build-system combinations:

```bash
mvn test -pl starter-templates
```

See `GenerationEngineTest.java` for full parameterized test coverage.
