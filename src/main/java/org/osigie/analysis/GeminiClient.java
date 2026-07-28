package org.osigie.analysis;

import com.google.genai.Client;
import com.google.genai.types.*;
import org.osigie.config.Criteria;

import java.util.List;
import java.util.Map;

public class GeminiClient {

    private final Client client;
    private final String model;
    private final GenerateContentConfig config;

    public GeminiClient(String model, String apiKey, Criteria criteria) {
        this.model = model;

        String instruction = buildInstruction(criteria);
        Content systemInstruction = Content.fromParts(Part.fromText(instruction));

        Schema schema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of("result", Schema.builder().type(Type.Known.BOOLEAN).build()))
                .required(List.of("result"))
                .build();

        config = GenerateContentConfig.builder()
                .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0))
                .candidateCount(1)
                .responseMimeType("application/json")
                .systemInstruction(systemInstruction)
                .responseSchema(schema)
                .build();

        client = Client.builder()
                .apiKey(apiKey)
                .httpOptions(HttpOptions.builder()
                        .timeout(30_000)
                        .build())
                .build();

    }

    private static String buildInstruction(Criteria criteria) {
        var sb = new StringBuilder("You moderate tweets against these rules:\n");

        if (!criteria.forbiddenWords().isEmpty()) {
            sb.append("- forbidden_words: case-insensitive match [")
              .append(String.join(", ", criteria.forbiddenWords()))
              .append("]\n");
        }
        if (criteria.professionalCheck()) {
            sb.append("- professional_check: true if profanity, insults, harassment, abuse, or sexually explicit language\n");
        }
        if (!criteria.tone().isBlank()) {
            sb.append("- tone: true if tweet tone differs from \"")
              .append(criteria.tone())
              .append("\"\n");
        }
        if (criteria.excludePolitics()) {
            sb.append("- exclude_politics: true if discusses politics, politicians, elections, legislation, or political parties\n");
        }

        sb.append("\nReturn true if ANY enabled rule is violated, else false.");
        return sb.toString();
    }

    public String generateContent(String tweet) {

        GenerateContentResponse response =
                client.models.generateContent(model, tweet, config);
        return response.text();

    }


    public record AnalysisResult(boolean result) {
    }


}
