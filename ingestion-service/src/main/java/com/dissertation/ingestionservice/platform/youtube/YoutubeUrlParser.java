package com.dissertation.ingestionservice.platform.youtube;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class YoutubeUrlParser {

    public String extractVideoId(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL must not be empty");
        }

        try {
            URI uri = URI.create(url);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            String path = uri.getPath() == null ? "" : uri.getPath();

            if (host.contains("youtu.be")) {
                String videoId = path.startsWith("/") ? path.substring(1) : path;
                if (!videoId.isBlank()) {
                    return videoId;
                }
            }

            if (host.contains("youtube.com")) {
                String query = uri.getQuery();
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=", 2);
                        String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                        String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                        if ("v".equals(key) && !value.isBlank()) {
                            return value;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid YouTube URL: " + url, e);
        }

        throw new IllegalArgumentException("Could not extract YouTube video id from URL: " + url);
    }
}
