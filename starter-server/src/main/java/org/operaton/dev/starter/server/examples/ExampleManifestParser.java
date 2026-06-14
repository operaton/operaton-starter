package org.operaton.dev.starter.server.examples;

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
                            ParsedManifest.Example example = parseExample(exampleMap);
                            examples.add(example);
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
     *
     * @param exampleMap the example map
     * @return a parsed Example
     * @throws ManifestRejected if path validation fails
     */
    private ParsedManifest.Example parseExample(Map<String, Object> exampleMap)
            throws ManifestRejected {
        String name = getStringValue(exampleMap, "name", "");
        String description = getStringValue(exampleMap, "description", "");
        String path = getStringValue(exampleMap, "path", "");

        // Validate path
        validatePath(path);

        @SuppressWarnings("unchecked")
        List<String> tags = exampleMap.get("tags") instanceof List<?> tagsList ?
                tagsList.stream()
                        .map(Object::toString)
                        .toList() :
                List.of();

        return new ParsedManifest.Example(name, description, path, tags);
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
}
