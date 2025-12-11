package org.cftoolsuite.cfapp.util;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Simple adapter that provides backward-compatible support for basic JsonPath expressions
 * using Jackson 3's native JsonNode navigation.
 *
 * <p><strong>Limitations:</strong> This adapter only supports simple property access patterns
 * like {@code $.property} or {@code $.parent.child}. Complex JsonPath queries with filters,
 * wildcards, or array operations are not supported.</p>
 *
 * <p><strong>Supported syntax:</strong></p>
 * <ul>
 *   <li>{@code $.property-name} - extracts a root-level property</li>
 *   <li>{@code $.parent.child} - extracts nested properties</li>
 * </ul>
 *
 * <p><strong>Not supported:</strong></p>
 * <ul>
 *   <li>Filters: {@code $[?(@.price < 10)]}</li>
 *   <li>Wildcards: {@code $.store.book[*].author}</li>
 *   <li>Array slicing: {@code $[0:5]}</li>
 * </ul>
 */
public class SimpleJsonPathAdapter {

    private final ObjectMapper mapper;

    public SimpleJsonPathAdapter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Extracts a JSON fragment from the input using a simple JsonPath-like expression.
     *
     * @param jsonData the JSON string to query
     * @param pathExpression the path expression (e.g., "$.property-name")
     * @return the extracted fragment as a JSON string
     * @throws IllegalArgumentException if the path expression is invalid or unsupported
     */
    public String extractFragment(String jsonData, String pathExpression) throws JacksonException {
        if (pathExpression == null || pathExpression.isBlank()) {
            return jsonData;
        }

        // Parse the JSON data
        JsonNode root = mapper.readTree(jsonData);

        // Convert JsonPath expression to property path
        String propertyPath = convertJsonPathToPropertyPath(pathExpression);

        // Navigate to the target node
        JsonNode target = navigateToNode(root, propertyPath);

        if (target == null || target.isMissingNode()) {
            throw new IllegalArgumentException("Path not found: " + pathExpression);
        }

        // Convert the result back to JSON string
        return mapper.writeValueAsString(target);
    }

    /**
     * Converts a JsonPath expression to a simple property path.
     * Supports: $.property or $.parent.child
     */
    private String convertJsonPathToPropertyPath(String jsonPathExpression) {
        if (!jsonPathExpression.startsWith("$.")) {
            throw new IllegalArgumentException(
                "Unsupported JsonPath expression: " + jsonPathExpression +
                ". Only simple property access starting with '$.' is supported.");
        }

        // Remove the $. prefix
        return jsonPathExpression.substring(2);
    }

    /**
     * Navigates through the JSON tree using dot-separated property names.
     */
    private JsonNode navigateToNode(JsonNode root, String propertyPath) {
        JsonNode current = root;

        // Split by dots and navigate through the path
        String[] properties = propertyPath.split("\\.");
        for (String property : properties) {
            if (current == null || current.isMissingNode()) {
                return null;
            }
            // Handle hyphenated property names (common in JSON)
            current = current.get(property);
        }

        return current;
    }
}
