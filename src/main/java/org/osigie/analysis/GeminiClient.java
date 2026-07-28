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

        String instruction = String.format("""
                You are a tweet moderation engine.
                
                Current moderation criteria:
                - forbidden_words: %s
                - professional_check: %s
                - tone: %s
                - exclude_politics: %s
                
                Interpretation:
                - forbidden_words: match case-insensitively.
                - professional_check: if enabled, return true if the tweet contains profanity,
                  insults, harassment, abusive language, sexually explicit language,
                  or is otherwise unsuitable for a professional public account.
                - tone: if provided, return true if the tweet's overall tone differs
                  significantly from the allowed tone(s).
                - exclude_politics: if enabled, return true if the tweet discusses politics,
                  politicians, governments, elections, legislation or political parties.
                
                Ignore disabled criteria.
                
                Return true if ANY enabled criterion is violated.
                Return false otherwise.
                """, String.join(",", criteria.forbiddenWords()), criteria.professionalCheck(), criteria.tone(), criteria.excludePolitics());


        Content systemInstruction = Content.fromParts(Part.fromText(instruction));


        Schema schema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of("result", Schema.builder().type(Type.Known.BOOLEAN).build()))
                .required(List.of("result"))
                .build();

        config = GenerateContentConfig.builder()
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

    public String generateContent(String tweet) {

        GenerateContentResponse response =
                client.models.generateContent(model, tweet, config);
        return response.text();

    }


    public record AnalysisResult(boolean result) {
    }


}
