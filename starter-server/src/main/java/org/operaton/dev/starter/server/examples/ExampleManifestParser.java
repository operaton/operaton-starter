package org.operaton.dev.starter.server.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses and validates manifest YAML files using SnakeYAML's SafeConstructor.
 *
 * <p>Key safety properties:
 * <ul>
 *   <li>Uses {@link SafeConstructor} to prevent arbitrary class instantiation</li>
 *   <li>Sets {@code codePointLimit} to 256 KB to prevent DOS attacks</li>
 *   <li>Validates {@code apiVersion}, example paths, and manifest size</li>
 *   <li>Silently ignores unknown fields</li>
 * </ul>
 */
@Component
public class ExampleManifestParser {
    private static final Logger log = LoggerFactory.getLogger(ExampleManifestParser.class);
    private static final int CODE_POINT_LIMIT = 256 * 1024; // 256 KB
    private static final String REQUIRED_API_VERSION_PREFIX = "operaton-starter/v1";

    /**
     * Parses a manifest from raw YAML bytes.
     *
     * @param manifestBytes the YAML content as bytes
     * @param sourceRepo the source repository token (e.g., "owner/repo" or "owner/repo@ref")
     * @param sourceRepoSha the commit SHA or ref identifier
     * @return a validated ParsedManifest
     * @throws ManifestRejected if validation fails
     */
    public ParsedManifest parse(byte[] manifestBytes, String sourceRepo, String sourceRepoSha)
            throws ManifestRejected {
        try {
            // Check manifest size before parsing
            if (manifestBytes.length > CODE_POINT_LIMIT) {
                throw new ManifestRejected("manifest-too-large");
            }

            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setCodePointLimit(CODE_POINT_LIMIT);

            Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
            String manifestContent = new String(manifestBytes, StandardCharsets.UTF_8);

            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = yaml.load(manifestContent);

            if (parsed == null) {
                parsed = new java.util.HashMap<>();
            }

            // Validate apiVersion
            Object apiVersionObj = parsed.get("apiVersion");
            if (apiVersionObj == null) {
                throw new ManifestRejected("api-version");
            }
            String apiVersion = apiVersionObj.toString();
            if (!apiVersion.startsWith(REQUIRED_API_VERSION_PREFIX)) {
                throw new ManifestRejected("api-version");
            }

            // Parse examples
            List<ParsedManifest.Example> examples = new ArrayList<>();
            Object examplesObj = parsed.get("examples");
            if (examplesObj instanceof List<?> examplesList) {
                for (Object ex : examplesList) {
                    if (ex instanceof Map<?, ?> exMap) {
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> exampleMap = (Map<String, Object>) exMap;
                            ParsedManifest.Example example = parseExample(exampleMap, sourceRepo);
                            if (example != null) {
                                examples.add(example);
                            }
                        } catch (ManifestRejected e) {
                            throw e;
                        } catch (Exception e) {
                            throw new ManifestRejected("invalid-example", e);
                        }
                    }
                }
            }

            return new ParsedManifest(apiVersion, examples, sourceRepo, sourceRepoSha);
        } catch (ManifestRejected e) {
            throw e;
        } catch (YAMLException e) {
            throw new ManifestRejected("malformed-yaml", e);
        } catch (Exception e) {
            throw new ManifestRejected("parse-error", e);
        }
    }

    /**
     * Parses a single example entry from the manifest.
     * Returns null if required fields (id, title, shortDescription) are missing — caller skips those entries.
     *
     * @param exampleMap the example map
     * @param sourceRepo source repo token for logging context
     * @return a parsed Example, or null if required fields are absent
     * @throws ManifestRejected if path validation fails
     */
    private ParsedManifest.Example parseExample(Map<String, Object> exampleMap, String sourceRepo)
            throws ManifestRejected {
        String id = getStringValue(exampleMap, "id", "");
        String title = getStringValue(exampleMap, "title", "");
        String shortDescription = getStringValue(exampleMap, "shortDescription", "");
        String path = getStringValue(exampleMap, "path", "");

        // Validate required fields
        if (id.isEmpty() || title.isEmpty() || shortDescription.isEmpty()) {
            log.warn("Skipping example in {} with missing required fields: id='{}', title='{}', shortDescription='{}'",
                    sourceRepo, id, title, shortDescription.isEmpty() ? "" : "<present>");
            return null;
        }

        // Validate path
        validatePath(path);

        List<ParsedManifest.Tag> tags = parseTags(exampleMap);

        String icon = getStringValue(exampleMap, "icon", null);
        String longDescription = getStringValue(exampleMap, "longDescription", null);
        String buildSystem = getStringValue(exampleMap, "buildSystem", null);
        String runtime = getStringValue(exampleMap, "runtime", null);
        String operatonVersion = getStringValue(exampleMap, "operatonVersion", null);
        String javaVersion = getStringValue(exampleMap, "javaVersion", null);
        String complexity = getStringValue(exampleMap, "complexity", null);
        List<String> integrations = getStringList(exampleMap, "integrations");
        List<String> bpmnConcepts = getStringList(exampleMap, "bpmnConcepts");
        String requires = getStringValue(exampleMap, "requires", null);
        List<ParsedManifest.Example.Author> authors = parseAuthors(exampleMap);
        String license = getStringValue(exampleMap, "license", null);
        String documentationUrl = getStringValue(exampleMap, "documentationUrl", null);
        String demoVideoUrl = getStringValue(exampleMap, "demoVideoUrl", null);
        List<String> screenshots = getStringList(exampleMap, "screenshots");
        String lastUpdated = getStringValue(exampleMap, "lastUpdated", null);

        return new ParsedManifest.Example(id, title, shortDescription, path, tags,
                icon, longDescription, buildSystem, runtime, operatonVersion, javaVersion,
                complexity, integrations, bpmnConcepts, requires, authors,
                license, documentationUrl, demoVideoUrl, screenshots, lastUpdated);
    }

    /**
     * Validates an example path to prevent traversal attacks.
     *
     * @param path the path to validate
     * @throws ManifestRejected if path is unsafe
     */
    private void validatePath(String path) throws ManifestRejected {
        if (path == null || path.isEmpty()) {
            throw new ManifestRejected("path-unsafe");
        }

        // No absolute paths
        if (path.startsWith("/")) {
            throw new ManifestRejected("path-unsafe");
        }

        // No parent directory traversal
        if (path.contains("..")) {
            throw new ManifestRejected("path-unsafe");
        }

        // No null bytes
        if (path.contains("\0")) {
            throw new ManifestRejected("path-unsafe");
        }
    }

    /**
     * Safely extracts a string value from a map.
     *
     * @param map the map to extract from
     * @param key the key to extract
     * @param defaultValue the default value if key is missing
     * @return the string value or default
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    /**
     * Extracts a list of strings from a map value.
     */
    private List<String> getStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    /**
     * Parses the tags field — supports both a list of strings and a list of maps with label/category.
     * Prioritizes the map form (extracts both label and category).
     */
    private List<ParsedManifest.Tag> parseTags(Map<String, Object> exampleMap) {
        Object tagsObj = exampleMap.get("tags");
        if (!(tagsObj instanceof List<?> tagsList)) {
            return List.of();
        }
        List<ParsedManifest.Tag> result = new ArrayList<>();
        for (Object tag : tagsList) {
            if (tag instanceof Map<?, ?> tagMap) {
                Object label = tagMap.get("label");
                Object category = tagMap.get("category");
                if (label != null) {
                    result.add(new ParsedManifest.Tag(
                            label.toString(),
                            category != null ? category.toString() : null
                    ));
                }
            } else if (tag != null) {
                result.add(new ParsedManifest.Tag(tag.toString(), null));
            }
        }
        return result;
    }

    /**
     * Parses the authors field — a list of maps with name and optional url.
     */
    private List<ParsedManifest.Example.Author> parseAuthors(Map<String, Object> exampleMap) {
        Object authorsObj = exampleMap.get("authors");
        if (!(authorsObj instanceof List<?> authorsList)) {
            return List.of();
        }
        List<ParsedManifest.Example.Author> result = new ArrayList<>();
        for (Object author : authorsList) {
            if (author instanceof Map<?, ?> authorMap) {
                Object name = authorMap.get("name");
                Object url = authorMap.get("url");
                if (name != null) {
                    result.add(new ParsedManifest.Example.Author(
                            name.toString(),
                            url != null ? url.toString() : null
                    ));
                }
            }
        }
        return result;
    }
}
