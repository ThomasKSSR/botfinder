package com.dissertation.analysisservice.dto;

import java.util.List;

public record PredictionResponse(List<PredictionItem> predictions) {
}
