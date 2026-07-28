package org.osigie.analysis;

import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.osigie.exception.AnalysisException;
import org.osigie.model.TweetRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GeminiAnalysisClient implements AnalysisClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiAnalysisClient.class);
    private final GeminiClient geminiClient;
    private final Gson gson = new Gson();
    private final RetryHandler retryHandler;
    private final RequestThrottler requestThrottler;

    public GeminiAnalysisClient(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
        retryHandler = RetryHandler.withDefaults();
        requestThrottler = RequestThrottler.withDefault();
    }

    @Override
    public boolean analyze(TweetRecord tweet) {
        String response = null;
        try {
            requestThrottler.acquire();

            response = retryHandler.retry(
                    () -> geminiClient.generateContent(tweet.text()),
                    this::shouldRetry
            );

            GeminiClient.AnalysisResult analysisResult =
                    gson.fromJson(response, GeminiClient.AnalysisResult.class);

            if (analysisResult == null) {
                throw new AnalysisException("Failed to generate content for tweetId: "
                        + tweet.id() + "missing (response: " + response + ")");
            }
            return analysisResult.result();

        } catch (JsonSyntaxException e) {
            throw new AnalysisException(
                    "Invalid JSON returned by Gemini: " + response + " for tweetId: " + tweet.id(),
                    e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnalysisException("Thread interrupted for tweet " + tweet.id(), e);
        } catch (Exception e) {
            throw new AnalysisException("Analysis failed for tweet " + tweet.id(), e);

        }
    }

    private boolean shouldRetry(Exception ex) {
        if (ex instanceof GenAiIOException) {
            return true;
        }

        if (ex instanceof ApiException api) {
            return switch (api.code()) {
                case 429, 500, 503, 504 -> true;
                default -> false;
            };
        }

        return false;
    }
}
