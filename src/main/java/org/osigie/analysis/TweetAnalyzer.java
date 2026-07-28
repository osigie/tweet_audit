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

                log.info("Processing batch of {} tweet(s).", batch.size());

                for (TweetRecord record : batch) {
                    boolean shouldDelete;
                    try {
                        shouldDelete = analysisClient.analyze(record);
                        log.info("Tweet {} analysed. Should delete: {}", record.id(), shouldDelete);

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

                    if (shouldDelete) {
                        analyzedTweetsForDelete++;
                        fileHandler.writeResult(record);
                    }

                    CheckpointEntry checkpoint = new CheckpointEntry(
                            record.entryIndex() + 1,
                            record.segmentBaseOffset()
                    );
                    fileHandler.saveCheckpoint(checkpoint);
                    fileHandler.flush();

                    log.info(
                            "Checkpoint saved: nextEntryIndex={}, segmentBaseOffset={}",
                            checkpoint.entryIndex(),
                            checkpoint.segmentBaseOffset()
                    );

                    analyzedTweets++;
                }

            }
        } while (fileHandler.nextSegment());

        if (analyzedTweets == 0) {
            log.info("Analysis skipped. All tweets have already been analysed.");
            return;
        }

        log.info("Tweet analysis completed successfully. Analysed {} tweet(s) and tweets up for deletes: {}.", analyzedTweets, analyzedTweetsForDelete);

    }
}
