package com.dissertation.ingestionservice.platform.youtube;

import com.dissertation.contracts.events.IngestedComment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class YoutubeClient {

    private final RestClient restClient;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.base-url}")
    private String baseUrl;

    public YoutubeClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public List<IngestedComment> fetchComments(String videoId) {
        Map<String, Object> response = restClient.get()
                .uri(baseUrl + "/commentThreads?part=snippet&videoId={videoId}&maxResults=20&key={key}",
                        videoId, apiKey)
                .retrieve()
                .body(Map.class);

        List<IngestedComment> result = new ArrayList<>();
        if (response == null || !response.containsKey("items")) {
            return result;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        for (Map<String, Object> item : items) {
            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
            if (snippet == null) {
                continue;
            }

            Map<String, Object> topLevelComment = (Map<String, Object>) snippet.get("topLevelComment");
            if (topLevelComment == null) {
                continue;
            }

            String commentId = (String) topLevelComment.get("id");
            Map<String, Object> topSnippet = (Map<String, Object>) topLevelComment.get("snippet");
            if (topSnippet == null) {
                continue;
            }

            String authorName = (String) topSnippet.get("authorDisplayName");
            String authorId = null;
            Map<String, Object> authorChannelId = (Map<String, Object>) topSnippet.get("authorChannelId");
            if (authorChannelId != null) {
                authorId = (String) authorChannelId.get("value");
            }

            Number likeCountNumber = (Number) topSnippet.get("likeCount");
            long likeCount = likeCountNumber != null ? likeCountNumber.longValue() : 0L;

            String text = (String) topSnippet.get("textDisplay");
            String publishedAtRaw = (String) topSnippet.get("publishedAt");
            Instant publishedAt = publishedAtRaw != null ? Instant.parse(publishedAtRaw) : null;

            result.add(new IngestedComment(
                    commentId,
                    authorId,
                    authorName,
                    text,
                    publishedAt,
                    likeCount
            ));
        }

        return result;
    }
}
