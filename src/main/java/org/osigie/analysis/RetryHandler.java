package org.osigie.analysis;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryHandler {
  private final int maxRetries;
  private final Duration fallbackDelay;
  private final Function<Exception, Duration> delayProvider;
  private static final Logger LOG = LoggerFactory.getLogger(RetryHandler.class);

  public RetryHandler(int maxRetries, Duration fallbackDelay) {
    this(maxRetries, fallbackDelay, null);
  }

  public RetryHandler(
      int maxRetries, Duration fallbackDelay, Function<Exception, Duration> delayProvider) {
    if (maxRetries <= 0) {
      throw new IllegalArgumentException("maxRetries must be greater than 0");
    }
    if (fallbackDelay == null) {
      throw new IllegalArgumentException("fallbackDelay cannot be null");
    }
    this.maxRetries = maxRetries;
    this.fallbackDelay = fallbackDelay;
    this.delayProvider = delayProvider;
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

        Duration customDelay = delayProvider != null ? delayProvider.apply(e) : null;
        Duration delay = customDelay != null ? customDelay : exponentialBackoff(attempt);

        LOG.warn(
            "Attempt {}/{} failed: {}. Retrying in ~{}ms...",
            attempt,
            maxRetries,
            e.getMessage(),
            delay.toMillis());

        try {
          Thread.sleep(delay.toMillis());
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw ie;
        }
      }
    }
    throw new IllegalStateException("Unreachable");
  }

  private Duration exponentialBackoff(int attempt) {
    long sleepTime = fallbackDelay.toMillis() * (1L << (attempt - 1));
    long jitter = ThreadLocalRandom.current().nextLong(fallbackDelay.toMillis());
    return Duration.ofMillis(sleepTime + jitter);
  }
}
