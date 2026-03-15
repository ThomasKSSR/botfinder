package com.dissertation.ingestionservice.service;

import com.dissertation.contracts.events.IngestedComment;
import com.dissertation.ingestionservice.platform.PlatformType;

import java.util.List;

public interface IngestionHandler {
    PlatformType supportedPlatform();
    List<IngestedComment> ingest(String url);
}
