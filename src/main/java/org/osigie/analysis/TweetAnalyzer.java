package org.osigie.analysis;

import java.io.IOException;
import java.util.List;

import org.osigie.exception.AnalysisException;
import org.osigie.io.FileHandler;
import org.osigie.model.CheckpointEntry;
import org.osigie.model.TweetRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TweetAnalyzer {
  private final AnalysisClient analysisClient;
  private final FileHandler fileHandler;
  private final int batchSize;
  private static final Logger LOG = LoggerFactory.getLogger(TweetAnalyzer.class);

  public TweetAnalyzer(AnalysisClient analysisClient, FileHandler fileHandler, int batchSize) {
    this.analysisClient = analysisClient;
    this.fileHandler = fileHandler;
    this.batchSize = batchSize;
  }

  /** Runs the full analysis pipeline across all segments with checkpointing. */
  public void run()  throws IOException {
    LOG.info("Starting tweet analysis.");
    long analyzedTweetsForDelete = 0;
    long analyzedTweets = 0;

    do {
      LOG.info("Processing segment.");
      List<TweetRecord> batch;
      batch = fileHandler.nextBatch(batchSize);
      while (!batch.isEmpty()) {

        LOG.info("Processing batch of {} tweet(s).", batch.size());

        for (TweetRecord record : batch) {
          boolean shouldDelete;
          try {
            shouldDelete = analysisClient.analyze(record);
            LOG.info("Tweet {} analysed. Should delete: {}", record.id(), shouldDelete);

          } catch (AnalysisException e) {
            LOG.error(
                "Analysis failed for tweet {} (segment={}, entry={}): {}",
                record.id(),
                record.segmentBaseOffset(),
                record.entryIndex(),
                e.getMessage());
            throw e;
          }

          if (shouldDelete) {
            analyzedTweetsForDelete++;
            fileHandler.writeResult(record);
          }

          CheckpointEntry checkpoint =
              new CheckpointEntry(record.entryIndex() + 1, record.segmentBaseOffset());
          fileHandler.saveCheckpoint(checkpoint);
          fileHandler.flush();

          LOG.info(
              "Checkpoint saved: nextEntryIndex={}, segmentBaseOffset={}",
              checkpoint.entryIndex(),
              checkpoint.segmentBaseOffset());

          analyzedTweets++;
        }
        batch = fileHandler.nextBatch(batchSize);
      }
    } while (fileHandler.nextSegment());

    if (analyzedTweets == 0) {
      LOG.info("Analysis skipped. All tweets have already been analysed.");
      return;
    }

    LOG.info(
        "Tweet analysis completed successfully. Analysed {} tweet(s) and tweets up for deletes"
            + ": {}.",
        analyzedTweets,
        analyzedTweetsForDelete);
  }
}
