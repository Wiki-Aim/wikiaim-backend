package com.wikiaim.backend.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TipTapTextExtractorTest {

    private TipTapTextExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TipTapTextExtractor();
    }

    @Test
    void shouldExtractTextFromBlocks() {
        String json = """
            {
                "time": 1709280000000,
                "blocks": [
                    {"id": "1", "type": "paragraph", "data": {"text": "Premier paragraphe"}},
                    {"id": "2", "type": "heading", "data": {"text": "Un titre"}},
                    {"id": "3", "type": "paragraph", "data": {"text": "Deuxième paragraphe"}}
                ]
            }
            """;

        List<String> lines = extractor.extractLines(json);

        assertEquals(3, lines.size());
        assertEquals("Premier paragraphe", lines.get(0));
        assertEquals("Un titre", lines.get(1));
        assertEquals("Deuxième paragraphe", lines.get(2));
    }

    @Test
    void shouldReturnEmptyListForEmptyBlocks() {
        String json = """
            {"time": 1709280000000, "blocks": []}
            """;

        List<String> lines = extractor.extractLines(json);

        assertTrue(lines.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForNull() {
        assertTrue(extractor.extractLines(null).isEmpty());
        assertTrue(extractor.extractLines("").isEmpty());
        assertTrue(extractor.extractLines("   ").isEmpty());
    }
}
