package com.dissertation.ingestionservice.platform.youtube;

import com.dissertation.contracts.events.IngestedComment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class YoutubeClient {

    public List<IngestedComment> fetchComments(String videoId) {
        return List.of(
                new IngestedComment(
                        "yt-c1", "yt-a1", "userA", "Nice video!", Instant.now().minusSeconds(300)
                ),
                new IngestedComment(
                        "yt-c2", "yt-a2", "userB", "Nice video!", Instant.now().minusSeconds(299)
                ),
                new IngestedComment(
                        "yt-c3", "yt-a3", "userC", "Subscribe to my channel!!!", Instant.now().minusSeconds(250)
                ),
                new IngestedComment(
                        "yt-c4", "yt-a4", "userD", "Check my profile for free gifts http://spam.com", Instant.now().minusSeconds(240)
                )
        );
    }
}
