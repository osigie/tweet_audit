package org.osigie.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationPropertiesLoaderTest {


    @DisplayName("Returns application properties with loaded criteria")
    @Test
    public void givenCompleteEnvironment_whenLoad_thenReturnApplicationProperties(@TempDir Path tempDir) throws Exception {

        Path criteriaPath = tempDir.resolve("config.json");
        Files.writeString(criteriaPath, """

                {
                  "criteria": {
                    "forbidden_words": [
                      "crypto",
                      "NFT",
                      "hustlegrindset"
                    ],
                    "professional_check": true,
                    "tone": "respectful and thoughtful",
                    "exclude_politics": true
                  }
                }
                """);

        Map<String, String> env = Map.of(
                "GEMINI_API_KEY", "gemini-api-key",
                "GEMINI_MODEL", "gemini-model",
                "X_USERNAME", "osigie"
        );

        ApplicationProperties properties = ApplicationPropertiesLoader.load(env, criteriaPath);

        assertEquals(properties.getGeminiApiKey(), env.get("GEMINI_API_KEY"));
        assertEquals(properties.getGeminiModel(), env.get("GEMINI_MODEL"));
        assertEquals(properties.getUsername(), env.get("X_USERNAME"));
        assertTrue(properties.getCriteria().excludePolitics());
        assertTrue(properties.getCriteria().professionalCheck());
        assertEquals(List.of(
                "crypto",
                "NFT",
                "hustlegrindset"
        ), properties.getCriteria().forbiddenWords());

    }
}
