package com.wikiaim.backend.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class TipTapTextExtractor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public List<String> extractLines(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            JsonNode blocks = root.path("blocks");

            if (blocks.isMissingNode() || !blocks.isArray()) {
                return List.of();
            }

            List<String> lines = new ArrayList<>();
            for (JsonNode block : blocks) {
                String text = extractTextFromBlock(block);
                if (text != null && !text.isEmpty()) {
                    lines.add(text);
                }
            }
            return lines;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String extractTextFromBlock(JsonNode block) {
        JsonNode data = block.path("data");
        if (!data.isMissingNode()) {
            JsonNode text = data.path("text");
            if (!text.isMissingNode()) {
                return text.asText();
            }
        }
        return null;
    }
}
