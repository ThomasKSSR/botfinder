package com.dissertation.ingestionservice.messaging;

import com.dissertation.contracts.events.AnalysisRequestedEvent;
import com.dissertation.contracts.events.CommentsIngestedEvent;
import com.dissertation.ingestionservice.service.IngestionOrchestrator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IngestionListenerIntegrationTest {

    @Test
    void onRequested_sends_ingested_event_to_rabbit() {
        RabbitTemplate rabbit = Mockito.mock(RabbitTemplate.class);
        IngestionOrchestrator orchestrator = Mockito.mock(IngestionOrchestrator.class);

        CommentsIngestedEvent sample = new CommentsIngestedEvent("job-1", "test", java.util.List.of());
        when(orchestrator.ingest(Mockito.eq("job-1"), Mockito.eq("http://x"), Mockito.anyInt())).thenReturn(sample);

        IngestionListener l = new IngestionListener(rabbit, orchestrator);

        AnalysisRequestedEvent req = new AnalysisRequestedEvent("job-1", "http://x", Instant.now(), Integer.valueOf(10));
        l.onRequested(req);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbit).convertAndSend(Mockito.anyString(), Mockito.anyString(), captor.capture());
        Object sent = captor.getValue();
        assertEquals(CommentsIngestedEvent.class, sent.getClass());
    }

    @Test
    void onRequested_uses_default_maxComments_when_null() {
        RabbitTemplate rabbit = Mockito.mock(RabbitTemplate.class);
        IngestionOrchestrator orchestrator = Mockito.mock(IngestionOrchestrator.class);

        CommentsIngestedEvent sample = new CommentsIngestedEvent("job-2", "test", java.util.List.of());
        when(orchestrator.ingest(Mockito.eq("job-2"), Mockito.eq("http://y"), Mockito.eq(100))).thenReturn(sample);

        IngestionListener l = new IngestionListener(rabbit, orchestrator);

        AnalysisRequestedEvent req = new AnalysisRequestedEvent("job-2", "http://y", Instant.now(), null);
        l.onRequested(req);

        // verify orchestrator called with default 100
        verify(orchestrator).ingest(Mockito.eq("job-2"), Mockito.eq("http://y"), Mockito.eq(100));
    }
}