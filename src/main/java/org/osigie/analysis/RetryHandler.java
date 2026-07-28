package org.osigie.analysis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class RetryHandler {
    private final int maxRetries;
    private final Duration delayMs;
    private static final Logger log = LoggerFactory.getLogger(RetryHandler.class);

    public RetryHandler(int maxRetries, Duration delayMs) {

        if (maxRetries <= 0) {
            throw new IllegalArgumentException("maxRetries must be greater than 0");
        }

        if (delayMs == null) {
            throw new IllegalArgumentException("delayMs cannot be null");
        }

        this.maxRetries = maxRetries;
        this.delayMs = delayMs;
    }


    public static RetryHandler withDefaults() {
        return new RetryHandler(3, Duration.ofMillis(500));
    }

    public <T> T retry(Callable<T> fn, Predicate<? super Exception> shouldRetry) throws Exception {

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return fn.call();
            } catch (Exception e) {

                if (!shouldRetry.test(e) || attempt == maxRetries) {
                    throw e;
                }

                log.warn("Attempt {} failed: {}. Retrying...", attempt, e.getMessage());
                try {
                    handleDelay(attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ie;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    private void handleDelay(int attempt) throws InterruptedException {
        long sleepTime = delayMs.toMillis() * (1L << (attempt - 1));
        long jitter = ThreadLocalRandom.current().nextLong(100);
        Thread.sleep(sleepTime + jitter);
    }

}
