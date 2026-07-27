package org.osigie.analysis;

import org.osigie.io.FileHandler;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;

import java.util.List;

public class TweetAnalyzer {
    private final AnalysisClient analysisClient;
    private final FileHandler fileHandler;
    private final int batchSize;

    public TweetAnalyzer(AnalysisClient analysisClient, FileHandler fileHandler, int batchSize) {
        this.analysisClient = analysisClient;
        this.fileHandler = fileHandler;
        this.batchSize = batchSize;
    }

    public void run() throws Exception {
        do {
            List<TweetRecord> batch;
            TweetRecord current = null;

            while (!(batch = fileHandler.nextBatch(batchSize)).isEmpty()) {
                for (TweetRecord record : batch) {
                    boolean deleted = analysisClient.analyze(record);
                    fileHandler.writeResult(record, deleted);
                    current = record;
                }
                fileHandler.flush();
                fileHandler.saveCheckpoint(
                        new CheckpointEntry(
                                current.entryIndex() + 1,
                                current.segmentBaseOffset()
                        )
                );
            }
        } while (fileHandler.nextSegment());
    }
}
