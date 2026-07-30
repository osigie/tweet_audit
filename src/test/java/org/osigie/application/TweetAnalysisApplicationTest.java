package org.osigie.application;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.osigie.TestUtil;
import org.osigie.analysis.AnalysisClient;
import org.osigie.config.ApplicationProperties;
import org.osigie.config.Criteria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TweetAnalysisApplicationTest {


    @DisplayName("Test for valid application properties")
    @Test
    public void givenValidApplicationProperties_whenRun_thenCompleteAnalysis(@TempDir Path tempDir) throws Exception {
        Path source = tempDir.resolve("tweets.json");
        Files.writeString(source, """
                [{"tweet": {"id": "1", "full_text": "hello"}},
                {"tweet": {"id": "1", "full_text": "hello"}}]
                """);

        ApplicationProperties properties = ApplicationProperties.builder()
                .setSourcePath(source.toString())
                .setChunksPath(tempDir.resolve("chunks").toString())
                .setResultPath(tempDir.resolve("result.csv").toString())
                .setCheckpointPath(tempDir.resolve("checkpoint.txt").toString())
                .setGeminiApiKey("test-api-key")
                .setUsername("test-user")
                .setChunksMaxSizeInBytes(1)
                .setGeminiModel("test-model")
                .setCriteria(new Criteria(List.of(), false, "good tone", false))
                .build();

        AnalysisClient mockAnalysisClient = new TestUtil.MockAnalysisClient();


        new TweetAnalysisApplication(properties, mockAnalysisClient).run();

        List<String> lines = Files.readAllLines(tempDir.resolve("result.csv"));
        File chunksFolder = new File(tempDir.resolve("chunks").toString());
        File[] chunks = chunksFolder.listFiles();
        assert chunks != null;
        assertEquals(2, chunks.length);
        assertEquals(2, lines.size());
        assertTrue(lines.get(1).contains("test-user/status/1"));

    }

    @DisplayName("Test for empty source")
    @Test
    public void givenEmptySource_whenRun_thenSkippExtraction(@TempDir Path tempDir) throws Exception {

        Path source = tempDir.resolve("tweets.json");
        Files.writeString(source, """
                []
                """);

        ApplicationProperties properties = ApplicationProperties.builder()
                .setSourcePath(source.toString())
                .setChunksPath(tempDir.resolve("chunks").toString())
                .setResultPath(tempDir.resolve("result.csv").toString())
                .setCheckpointPath(tempDir.resolve("checkpoint.txt").toString())
                .setGeminiApiKey("test-api-key")
                .setUsername("test-user")
                .setChunksMaxSizeInBytes(1)
                .setGeminiModel("test-model")
                .setCriteria(new Criteria(List.of(), false, "good tone", false))
                .build();

        AnalysisClient mockAnalysisClient = new TestUtil.MockAnalysisClient();


        new TweetAnalysisApplication(properties, mockAnalysisClient).run();

        File chunksFolder = new File(tempDir.resolve("chunks").toString());
        File[] chunks = chunksFolder.listFiles();
        assert chunks != null;
        assertEquals(0, chunks.length);

    }


    @DisplayName("Test checkpoint resumption")
    @Test
    public void givenCheckpointWithValue_whenRun_thenShouldContinueFromCheckpoint(@TempDir Path tempDir) throws Exception {

        Path source = tempDir.resolve("tweets.json");
        Files.writeString(source, """
                [{"tweet": {"id": "1", "full_text": "hello"}},
                {"tweet": {"id": "1", "full_text": "hello"}},
                {"tweet": {"id": "1", "full_text": "hello"}} ]
                """);
        Path checkpointPath = tempDir.resolve("checkpoint.txt");

        Files.writeString(checkpointPath, "1,0");

        ApplicationProperties properties = ApplicationProperties.builder()
                .setSourcePath(source.toString())
                .setChunksPath(tempDir.resolve("chunks").toString())
                .setResultPath(tempDir.resolve("result.csv").toString())
                .setCheckpointPath(checkpointPath.toString())
                .setGeminiApiKey("test-api-key")
                .setUsername("test-user")
                .setGeminiModel("test-model")
                .setCriteria(new Criteria(List.of(), false, "good tone", false))
                .build();

        AnalysisClient mockAnalysisClient = new TestUtil.MockAnalysisClient();

        new TweetAnalysisApplication(properties, mockAnalysisClient).run();

        List<String> lines = Files.readAllLines(tempDir.resolve("result.csv"));
        File chunksFolder = new File(tempDir.resolve("chunks").toString());
        String checkpoint = Files.readString(checkpointPath).trim();

        File[] chunks = chunksFolder.listFiles();
        assert chunks != null;

        assertEquals(1, chunks.length);
        assertEquals(2, lines.size());
        assertEquals("3,0", checkpoint);

    }
}
