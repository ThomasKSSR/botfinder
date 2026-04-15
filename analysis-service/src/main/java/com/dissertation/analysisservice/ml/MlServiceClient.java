package com.dissertation.analysisservice.ml;

import com.dissertation.analysisservice.dto.PredictionItem;
import com.dissertation.analysisservice.dto.PredictionResponse;
import com.dissertation.analysisservice.dto.TextBatchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class MlServiceClient {

    private final RestClient restClient;
    private final String baseUrl;

    public MlServiceClient(RestClient.Builder builder,
                           @Value("${ml.service.base-url}") String baseUrl) {
        this.restClient = builder.build();
        this.baseUrl = baseUrl;
    }

    public List<PredictionItem> predictSpam(List<String> texts) {
        PredictionResponse response = restClient.post()
                .uri(baseUrl + "/predict/spam")
                .body(new TextBatchRequest(texts))
                .retrieve()
                .body(PredictionResponse.class);

        if (response == null || response.predictions() == null) {
            return Collections.emptyList();
        }

        return response.predictions();
    }

    public List<PredictionItem> predictTroll(List<String> texts) {
        PredictionResponse response = restClient.post()
                .uri(baseUrl + "/predict/troll")
                .body(new TextBatchRequest(texts))
                .retrieve()
                .body(PredictionResponse.class);

        if (response == null || response.predictions() == null) {
            return Collections.emptyList();
        }

        return response.predictions();
    }
}
