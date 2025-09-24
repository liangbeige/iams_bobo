package com.iams.manage.domain;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {

    public static String getLabelById(String jsonString, long directoryId) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);

        return findLabelById(rootNode, directoryId);
    }

    private static String findLabelById(JsonNode node, long directoryId) {
        if (node.isArray()) {
            for (JsonNode item : node) {
                String label = findLabelById(item, directoryId);
                if (label != null) {
                    return label;
                }
            }
        } else if (node.isObject()) {
            long id = node.path("id").asLong();
            if (id == directoryId) {
                return node.path("label").asText();
            }

            JsonNode childrenNode = node.path("children");
            if (!childrenNode.isMissingNode()) {
                String label = findLabelById(childrenNode, directoryId);
                if (label != null) {
                    return label;
                }
            }
        }
        return null;
    }
}

