package com.dissertation.ingestionservice.platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlatformDetectorTest {

    @Test
    void detect_returns_youtube_for_youtube_urls() {
        PlatformDetector d = new PlatformDetector();
        assertEquals(PlatformType.YOUTUBE, d.detect("https://www.youtube.com/watch?v=abc"));
        assertEquals(PlatformType.YOUTUBE, d.detect("https://youtu.be/abc"));
    }

    @Test
    void detect_returns_unknown_for_null_or_empty() {
        PlatformDetector d = new PlatformDetector();
        assertEquals(PlatformType.UNKNOWN, d.detect(null));
        assertEquals(PlatformType.UNKNOWN, d.detect("   "));
    }
}
