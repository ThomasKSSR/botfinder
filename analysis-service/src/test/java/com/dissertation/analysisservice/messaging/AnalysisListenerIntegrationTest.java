package com.dissertation.analysisservice.messaging;

import com.dissertation.analysisservice.dto.PredictionItem;
import com.dissertation.analysisservice.ml.MlServiceClient;
import com.dissertation.analysisservice.detection.account.AccountHeuristicScorer;
import com.dissertation.contracts.events.CommentsIngestedEvent;
import com.dissertation.contracts.events.IngestedComment;
import com.dissertation.contracts.events.AnalysisCompletedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AnalysisListenerIntegrationTest {

    @Test
    void onComments_calls_ml_and_publishes_results() {
        RabbitTemplate rabbit = Mockito.mock(RabbitTemplate.class);
        MlServiceClient ml = Mockito.mock(MlServiceClient.class);
        AccountHeuristicScorer scorer = Mockito.mock(AccountHeuristicScorer.class);
        
        PredictionItem p = new PredictionItem("normal", 0.1);
        when(ml.predictSpam(Mockito.anyList())).thenReturn(List.of(p));
        when(ml.predictTroll(Mockito.anyList())).thenReturn(List.of(p));
        when(scorer.score(Mockito.any(), Mockito.anyList())).thenReturn(0.0);

        AnalysisListener listener = new AnalysisListener(rabbit, ml, scorer);

        IngestedComment c = new IngestedComment("id1", "author1", "name", "some text", Instant.now(), 0L);
        CommentsIngestedEvent event = new CommentsIngestedEvent("job-1", "test", List.of(c));

        listener.onComments(event);

        // capture published message
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbit).convertAndSend(Mockito.anyString(), Mockito.anyString(), captor.capture());

        Object sent = captor.getValue();
        assertTrue(sent instanceof AnalysisCompletedEvent, "Expected AnalysisCompletedEvent to be published");
    }

    @Test
    void onComments_labels_as_spam_when_ml_reports_spam_high_score() {
        RabbitTemplate rabbit = Mockito.mock(RabbitTemplate.class);
        MlServiceClient ml = Mockito.mock(MlServiceClient.class);
        AccountHeuristicScorer scorer = Mockito.mock(AccountHeuristicScorer.class);

        PredictionItem spamPred = new PredictionItem("spam", 0.9);
        when(ml.predictSpam(Mockito.anyList())).thenReturn(List.of(spamPred));
        when(ml.predictTroll(Mockito.anyList())).thenReturn(List.of(spamPred));
        when(scorer.score(Mockito.any(), Mockito.anyList())).thenReturn(0.0);

        AnalysisListener listener = new AnalysisListener(rabbit, ml, scorer);

        IngestedComment c = new IngestedComment("id2", "author2", "name2", "buy now", Instant.now(), 0L);
        CommentsIngestedEvent event = new CommentsIngestedEvent("job-2", "test", List.of(c));

        listener.onComments(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbit).convertAndSend(Mockito.anyString(), Mockito.anyString(), captor.capture());
        Object sent = captor.getValue();
        AnalysisCompletedEvent ace = (AnalysisCompletedEvent) sent;
        assertTrue(ace.results().size() == 1);
        assertTrue("spam".equalsIgnoreCase(ace.results().get(0).label()));
    }

    @Test
    void onComments_handles_multiple_comments() {
        RabbitTemplate rabbit = Mockito.mock(RabbitTemplate.class);
        MlServiceClient ml = Mockito.mock(MlServiceClient.class);
        AccountHeuristicScorer scorer = Mockito.mock(AccountHeuristicScorer.class);

        PredictionItem p1 = new PredictionItem("normal", 0.2);
        PredictionItem p2 = new PredictionItem("troll", 0.8);
        when(ml.predictSpam(Mockito.anyList())).thenReturn(List.of(p1, p2));
        when(ml.predictTroll(Mockito.anyList())).thenReturn(List.of(p1, p2));
        when(scorer.score(Mockito.any(), Mockito.anyList())).thenReturn(0.0);

        AnalysisListener listener = new AnalysisListener(rabbit, ml, scorer);

        IngestedComment c1 = new IngestedComment("id3", "author3", "n3", "text1", Instant.now(), 0L);
        IngestedComment c2 = new IngestedComment("id4", "author4", "n4", "text2", Instant.now(), 0L);
        CommentsIngestedEvent event = new CommentsIngestedEvent("job-3", "test", List.of(c1, c2));

        listener.onComments(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbit).convertAndSend(Mockito.anyString(), Mockito.anyString(), captor.capture());
        AnalysisCompletedEvent ace = (AnalysisCompletedEvent) captor.getValue();
        assertTrue(ace.results().size() == 2);
    }
}
