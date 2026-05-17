package com.dissertation.ingestionservice.platform.youtube;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class YoutubeUrlParserTest {

    @Test
    void extractVideoId_from_youtu_be() {
        YoutubeUrlParser p = new YoutubeUrlParser();
        assertEquals("abc123", p.extractVideoId("https://youtu.be/abc123"));
    }

    @Test
    void extractVideoId_from_watch_param() {
        YoutubeUrlParser p = new YoutubeUrlParser();
        assertEquals("xyz", p.extractVideoId("https://www.youtube.com/watch?v=xyz"));
    }

    @Test
    void extract_throws_on_invalid() {
        YoutubeUrlParser p = new YoutubeUrlParser();
        assertThrows(IllegalArgumentException.class, () -> p.extractVideoId("not a url"));
    }
}
