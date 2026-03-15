package com.dissertation.ingestionservice.platform;

import org.springframework.stereotype.Component;

@Component
public class PlatformDetector {

    public PlatformType detect(String url) {
        if (url == null || url.isBlank()) {
            return PlatformType.UNKNOWN;
        }

        String u = url.toLowerCase();

        if (u.contains("youtube.com") || u.contains("youtu.be")) {
            return PlatformType.YOUTUBE;
        }

        return PlatformType.UNKNOWN;
    }
}
