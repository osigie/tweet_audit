package org.osigie.analysis;

import java.time.Duration;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.osigie.config.Criteria;
import org.osigie.exception.AnalysisException;
import org.osigie.model.TweetRecord;

public class GeminiAnalysisClient implements AnalysisClient {
    private static final Pattern RETRY_DELAY_PATTERN = Pattern.compile("Please retry in ([\\d.]+)s");
    private static final long RETRY_BUFFER_MS = 1000;

    private final GeminiClient geminiClient;
    private final Gson gson = new Gson();
    private final RetryHandler retryHandler;
    private final RequestThrottler requestThrottler;

    //package private constructor for testing to allow mocking GeminiClient
    GeminiAnalysisClient(GeminiClient geminiClient) {
        this.geminiClient = Objects.requireNonNull(geminiClient);
        this.retryHandler =
                new RetryHandler(3, Duration.ofMillis(500), GeminiAnalysisClient::extractRetryDelay);
        this.requestThrottler = RequestThrottler.withDefault();
    }

    public static GeminiAnalysisClient fromCriteria(
            String model,
            String apiKey,
            Criteria criteria) {

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Gemini model cannot be blank.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Gemini API key cannot be blank.");
        }

        Objects.requireNonNull(criteria, "Criteria cannot be null");

        GeminiClient client = new GeminiClient(
                model,
                apiKey,
                buildInstruction(criteria)
        );
        return new GeminiAnalysisClient(client);
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

    /**
     * Analyzes a single tweet using Gemini with retry and rate limiting.
     *
     * @return true if the tweet violates any enabled moderation criterion
     */
    @Override
    public boolean analyze(TweetRecord tweet) {
        String response = null;
        try {
            requestThrottler.acquire();

            response =
                    retryHandler.retry(() -> geminiClient.generateContent(tweet.text()), this::shouldRetry);

            GeminiClient.AnalysisResult analysisResult =
                    gson.fromJson(response, GeminiClient.AnalysisResult.class);

            if (analysisResult == null) {
                throw new AnalysisException(
                        "Failed to generate content for tweetId: "
                                + tweet.id()
                                + "missing (response: "
                                + response
                                + ")");
            }
            return analysisResult.result();

        } catch (AnalysisException e) {
            throw e;
        } catch (JsonSyntaxException e) {
            throw new AnalysisException(
                    "Invalid JSON returned by Gemini: " + response + " for tweetId: " + tweet.id(), e);
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

    static String buildInstruction(Criteria criteria) {
        var sb = new StringBuilder("You moderate tweets against these rules:\n");

        if (!criteria.forbiddenWords().isEmpty()) {
            sb.append("- forbidden_words: case-insensitive match [")
                    .append(String.join(", ", criteria.forbiddenWords()))
                    .append("]\n");
        }
        if (criteria.professionalCheck()) {
            sb.append(
                    "- professional_check: true if profanity, insults, harassment, abuse, or sexually"
                            + " explicit language\n");
        }
        if (!criteria.tone().isBlank()) {
            sb.append("- tone: true if tweet tone differs from \"")
                    .append(criteria.tone())
                    .append("\"\n");
        }
        if (criteria.excludePolitics()) {
            sb.append(
                    "- exclude_politics: true if discusses politics, politicians, elections, legislation,"
                            + " or political parties\n");
        }

        sb.append("\nReturn true if ANY enabled rule is violated, else false.");
        return sb.toString();
    }
}
