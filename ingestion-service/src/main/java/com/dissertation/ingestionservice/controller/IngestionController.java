package com.dissertation.ingestionservice.controller;

import com.dissertation.ingestionservice.dto.IngestRequest;
import com.dissertation.ingestionservice.dto.IngestResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IngestionController {

    @PostMapping("/ingest")
    public IngestResponse ingest(@Valid @RequestBody IngestRequest req) {
        // MOCK for now (later you’ll call YouTube API here)
        var comments = List.of(
                "Nice video!",
                "Subscribe to my channel!!!",
                "Nice video!",
                "Check my profile for free gifts http://spam.com"
        );
        return new IngestResponse("youtube", comments);
    }
}
