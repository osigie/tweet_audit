package org.osigie.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osigie.TestUtil;
import org.osigie.exception.AnalysisException;
import org.osigie.model.TweetRecord;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TweetAnalyzerTest {


    @DisplayName("Saved results and checkpoint for unanalyzed source")
    @Test
    public void givenUnanalyzedChunks_whenRun_thenShouldSaveResultAndCheckpoint() throws Exception {

        List<TweetRecord> tweets = List.of(
                new TweetRecord(1L, 0, "1", "pass"),
                new TweetRecord(2L, 1, "2", "fail")
        );

        TestUtil.MockFilehandler fileHandler =
                new TestUtil.MockFilehandler(List.of(tweets));

        TweetAnalyzer analyzer =
                new TweetAnalyzer(
                        new TestUtil.MockAnalysisClient(),
                        fileHandler,
                        2);

        analyzer.run();

        assertEquals(1, fileHandler.writtenResults().size());
        assertEquals(2, fileHandler.checkpoints().size());
        assertEquals(2, fileHandler.flushCount());

    }

    @DisplayName("Throw AnalysisException when analysis fail")
    @Test
    public void givenFailedAnalysis_whenRun_thenShouldThrowAnalysisException() throws Exception {

        List<TweetRecord> tweets = List.of(
                new TweetRecord(-1, 1, "2", "fail")
        );

        TestUtil.MockFilehandler fileHandler =
                new TestUtil.MockFilehandler(List.of(tweets));

        TweetAnalyzer analyzer =
                new TweetAnalyzer(
                        new TestUtil.MockAnalysisClient(),
                        fileHandler,
                        2);

        assertThrows(AnalysisException.class, analyzer::run);

    }

}
