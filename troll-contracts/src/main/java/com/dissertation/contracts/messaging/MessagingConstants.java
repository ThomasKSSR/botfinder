package com.dissertation.contracts.messaging;

public final class MessagingConstants {
    private MessagingConstants() {}

    public static final String EXCHANGE = "troll.exchange";

    public static final String RK_ANALYSIS_REQUESTED = "analysis.requested";
    public static final String RK_COMMENTS_INGESTED  = "comments.ingested";
    public static final String RK_ANALYSIS_COMPLETED = "analysis.completed";

    public static final String Q_INGESTION = "q.ingestion";
    public static final String Q_ANALYSIS  = "q.analysis";
    public static final String Q_GATEWAY_RESULTS = "q.gateway.results";
}
