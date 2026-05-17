package com.dissertation.analysisservice.detection.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextPreprocessorTest {

    @Test
    void normalize_replaces_urls_and_trims_and_lowercases() {
        TextPreprocessor p = new TextPreprocessor();
        String out = p.normalize("  Hello HTTP://example.com!!!   ");
        assertEquals("hello <url>!!", out);
    }

    @Test
    void normalize_handles_null() {
        TextPreprocessor p = new TextPreprocessor();
        assertEquals("", p.normalize(null));
    }
}
