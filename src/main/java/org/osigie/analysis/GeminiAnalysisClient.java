package org.osigie.analysis;

import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.osigie.exception.AnalysisException;
import org.osigie.model.TweetRecord;

import java.time.Duration;
import java.util.regex.Pattern;


public class GeminiAnalysisClient implements AnalysisClient {
    private static final Pattern RETRY_DELAY_PATTERN = Pattern.compile("Please retry in ([\\d.]+)s");
    private static final long RETRY_BUFFER_MS = 1000;

    private final GeminiClient geminiClient;
    private final Gson gson = new Gson();
    private final RetryHandler retryHandler;
    private final RequestThrottler requestThrottler;

    public GeminiAnalysisClient(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
        this.retryHandler = new RetryHandler(3, Duration.ofMillis(500), GeminiAnalysisClient::extractRetryDelay);
        this.requestThrottler = RequestThrottler.withDefault();
    }

    private static Duration extractRetryDelay(Exception ex) {
        if (ex instanceof ApiException api && api.code() == 429) {
            String msg = ex.getMessage();
            if (msg != null) {
                var matcher = RETRY_DELAY_PATTERN.matcher(msg);
                if (matcher.find()) {
                    double seconds = Double.parseDouble(matcher.group(1));
                    return Duration.ofMillis((long) (seconds * 1000) + RETRY_BUFFER_MS);
                }
            }
        }
        return null;
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
