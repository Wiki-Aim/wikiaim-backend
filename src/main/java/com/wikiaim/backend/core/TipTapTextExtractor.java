package com.wikiaim.backend.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts plain text lines from a TipTap/BlockNote JSON document.
 *
 * <p>Iterates over the top-level {@code blocks} array and collects each
 * block's {@code data.text} value as a single line. Blocks that lack a
 * {@code data.text} field are silently skipped.
 *
 * <p>Expected input format:
 * <pre>{@code
 * {
 *   "blocks": [
 *     { "type": "heading",   "data": { "text": "Title", "level": 1 } },
 *     { "type": "paragraph", "data": { "text": "Some content..." } }
 *   ]
 * }
 * }</pre>
 *
 * @see com.wikiaim.backend.revisions.RevisionService
 */
@Slf4j
@Singleton
public class TipTapTextExtractor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Parses a TipTap/BlockNote JSON string and returns one line per text-bearing block.
     *
     * @param json a TipTap/BlockNote JSON string, or {@code null}
     * @return an unmodifiable list of text lines, never {@code null};
     *         returns an empty list if the input is {@code null}, blank, malformed,
     *         or contains no text blocks
     */
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
            log.warn("Erreur lors de l'extraction du texte TipTap", e);
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
