package org.osigie.analysis;

import org.osigie.exception.AnalysisException;
import org.osigie.io.FileHandler;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.info("Starting tweet analysis.");
        long analyzedTweetsForDelete = 0;
        long analyzedTweets = 0;

        do {
            log.info("Processing segment.");
            List<TweetRecord> batch;
            while (!(batch = fileHandler.nextBatch(batchSize)).isEmpty()) {

                log.debug("Processing batch of {} tweet(s).", batch.size());

                for (TweetRecord record : batch) {
                    boolean deleted;
                    try {
                        deleted = analysisClient.analyze(record);
                    } catch (AnalysisException e) {
                        log.error(
                                "Analysis failed for tweet {} (segment={}, entry={}): {}",
                                record.id(),
                                record.segmentBaseOffset(),
                                record.entryIndex(),
                                e.getMessage()
                        );
                        throw e;
                    }

                    fileHandler.writeResult(record, deleted);
                    if (deleted) {
                        analyzedTweetsForDelete++;
                    }
                    analyzedTweets++;
                }
                fileHandler.flush();
                TweetRecord lastRecord = batch.getLast();

                CheckpointEntry checkpoint = new CheckpointEntry(
                        lastRecord.entryIndex() + 1,
                        lastRecord.segmentBaseOffset()
                );
                fileHandler.saveCheckpoint(checkpoint);

                log.debug(
                        "Completed batch of {} tweet(s). Checkpoint saved: nextEntryIndex={}, segmentBaseOffset={}",
                        batch.size(),
                        checkpoint.entryIndex(),
                        checkpoint.segmentBaseOffset()
                );
            }
        } while (fileHandler.nextSegment());

        if (analyzedTweets == 0) {
            log.info("Analysis skipped. All tweets have already been analysed.");
            return;
        }

        log.info("Tweet analysis completed successfully. Analysed {} tweet(s) and tweets up for deletes: {}.", analyzedTweets, analyzedTweetsForDelete);

    }
}
