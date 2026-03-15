package com.dissertation.ingestionservice.service;

import com.dissertation.contracts.events.CommentsIngestedEvent;
import com.dissertation.ingestionservice.platform.PlatformDetector;
import com.dissertation.ingestionservice.platform.PlatformType;
import org.springframework.stereotype.Service;

@Service
public class IngestionOrchestrator {

    private final PlatformDetector platformDetector;
    private final IngestionHandlerFactory handlerFactory;

    public IngestionOrchestrator(PlatformDetector platformDetector, IngestionHandlerFactory handlerFactory) {
        this.platformDetector = platformDetector;
        this.handlerFactory = handlerFactory;
    }

    public CommentsIngestedEvent ingest(String jobId, String url) {
        PlatformType platform = platformDetector.detect(url);

        if (platform == PlatformType.UNKNOWN) {
            throw new IllegalArgumentException("Unsupported platform for URL: " + url);
        }

        var handler = handlerFactory.getHandler(platform);
        var comments = handler.ingest(url);

        return new CommentsIngestedEvent(jobId, platform.name().toLowerCase(), comments);
    }
}
