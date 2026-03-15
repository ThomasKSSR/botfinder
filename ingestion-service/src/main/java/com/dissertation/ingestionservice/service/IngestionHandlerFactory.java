package com.dissertation.ingestionservice.service;

import com.dissertation.ingestionservice.platform.PlatformType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngestionHandlerFactory {

    private final List<IngestionHandler> handlers;

    public IngestionHandlerFactory(List<IngestionHandler> handlers) {
        this.handlers = handlers;
    }

    public IngestionHandler getHandler(PlatformType platformType) {
        return handlers.stream()
                .filter(h -> h.supportedPlatform() == platformType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No handler for platform: " + platformType));
    }
}
