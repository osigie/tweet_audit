package org.osigie.analysis;

import org.osigie.exception.AnalysisException;
import org.osigie.io.FileHandler;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class TweetAnalyzer {
    private final AnalysisClient analysisClient;
    private final FileHandler fileHandler;
    private final int batchSize;
    private static final Logger log = LoggerFactory.getLogger(TweetAnalyzer.class);

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
                    boolean deleted;
                    try {
                        deleted = analysisClient.analyze(record);
                    } catch (AnalysisException e) {
                        log.warn(
                                "Failed to analyse tweet {}: {}",
                                record.entryIndex(),
                                e.getMessage()
                        );
                        throw new IOException("Failed to analyse tweet " + record.entryIndex(), e);
                    }

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
