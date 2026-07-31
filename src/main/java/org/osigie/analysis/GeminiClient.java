package org.osigie.analysis;

import java.util.List;
import java.util.Map;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.Type;


public class GeminiClient {

    private final Client client;
    private final String model;
    private final GenerateContentConfig config;

    public GeminiClient(String model, String apiKey, String instruction) {

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Gemini model cannot be blank.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Gemini API key cannot be blank.");
        }


        if (instruction == null || instruction.isBlank()) {
            throw new IllegalArgumentException("Instruction cannot be blank.");
        }

        this.model = model;

        Content systemInstruction = Content.fromParts(Part.fromText(instruction));

        Schema schema =
                Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(Map.of("result", Schema.builder().type(Type.Known.BOOLEAN).build()))
                        .required(List.of("result"))
                        .build();

        config =
                GenerateContentConfig.builder()
                        .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0))
                        .candidateCount(1)
                        .responseMimeType("application/json")
                        .systemInstruction(systemInstruction)
                        .responseSchema(schema)
                        .build();

        client =
                Client.builder()
                        .apiKey(apiKey)
                        .httpOptions(HttpOptions.builder().timeout(30_000).build())
                        .build();
    }


    public String generateContent(String tweet) {

        GenerateContentResponse response = client.models.generateContent(model, tweet, config);
        return response.text();
    }

    public record AnalysisResult(boolean result) {
    }
}
