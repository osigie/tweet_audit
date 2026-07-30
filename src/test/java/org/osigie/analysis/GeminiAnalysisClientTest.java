package org.osigie.analysis;

import com.google.gson.JsonSyntaxException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osigie.TestUtil;
import org.osigie.exception.AnalysisException;
import org.osigie.model.TweetRecord;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiAnalysisClientTest {

    @DisplayName("Returns true for a tweet that passes moderation")
    @Test
    public void givenTweetThatMeetsCriteria_whenAnalyze_thenReturnTrue() {

        GeminiClient mockGeminiClient = new TestUtil.MockGeminiClient("test-model", "key", "instruction");
        TweetRecord tweetRecord = new TweetRecord(1L, 5, "id-100", "pass");

        boolean result = new GeminiAnalysisClient(mockGeminiClient).analyze(tweetRecord);

        assertTrue(result);

    }

    @DisplayName("Returns false for a tweet that failed moderation")
    @Test
    public void givenTweetThatFailedCriteria_whenAnalyze_thenReturnFalse() {

        GeminiClient mockGeminiClient = new TestUtil.MockGeminiClient("test-model", "key", "instruction");
        TweetRecord tweetRecord = new TweetRecord(1L, 5, "id-100", "failed");

        boolean result = new GeminiAnalysisClient(mockGeminiClient).analyze(tweetRecord);

        assertFalse(result);

    }


    @DisplayName("Throws AnalysisException when AnalysisResult is null")
    @Test
    public void givenTweetWhenClientDidNotGenerateContent_whenAnalyze_thenThrowAnalysisException() {

        GeminiClient mockGeminiClient = new TestUtil.MockGeminiClient("test-model", "key", "instruction");
        TweetRecord tweetRecord = new TweetRecord(1L, 5, "id-100", "null");

        AnalysisException ex = assertThrows(AnalysisException.class,
                () -> new GeminiAnalysisClient(mockGeminiClient).analyze(tweetRecord));
        assertTrue(ex.getMessage().contains("Failed to generate content for tweetId:"));
    }

    @DisplayName("Throws AnalysisException when result is not a valid AnalysisResult type")
    @Test
    public void givenTweetWhenClientGenerateInvalidAnalysisResultType_whenAnalyze_thenThrowAnalysisException() {

        GeminiClient mockGeminiClient = new TestUtil.MockGeminiClient("test-model", "key", "instruction");
        TweetRecord tweetRecord = new TweetRecord(1L, 5, "id-100", "");

        AnalysisException ex = assertThrows(AnalysisException.class,
                () -> new GeminiAnalysisClient(mockGeminiClient).analyze(tweetRecord));

        assertInstanceOf(JsonSyntaxException.class, ex.getCause());
        assertTrue(ex.getMessage().contains("Invalid JSON returned by Gemini:"));

    }

}
