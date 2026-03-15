package com.dissertation.ingestionservice.platform.youtube;

import com.dissertation.contracts.events.IngestedComment;
import com.dissertation.ingestionservice.platform.PlatformType;
import com.dissertation.ingestionservice.service.IngestionHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class YouTubeIngestionHandler implements IngestionHandler {

    private final YoutubeUrlParser urlParser;
    private final YoutubeClient youtubeClient;

    public YouTubeIngestionHandler(YoutubeUrlParser urlParser, YoutubeClient youtubeClient) {
        this.urlParser = urlParser;
        this.youtubeClient = youtubeClient;
    }

    @Override
    public PlatformType supportedPlatform() {
        return PlatformType.YOUTUBE;
    }

    @Override
    public List<IngestedComment> ingest(String url) {
        String videoId = urlParser.extractVideoId(url);
        return youtubeClient.fetchComments(videoId);
    }
}
