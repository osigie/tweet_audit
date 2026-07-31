package org.osigie.analysis;

import java.time.Duration;
import java.util.Objects;

public class RequestThrottler {
  private final long intervalNanos;
  private long lastRequestTimeNano;

  public RequestThrottler(Duration interval) {
    Objects.requireNonNull(interval, "Interval cannot be null");

    if (interval.isNegative() || interval.isZero()) {
      throw new IllegalArgumentException("Interval must be greater than or equal to zero");
    }

    this.intervalNanos = interval.toNanos();
  }

  public static RequestThrottler withDefault() {
    return new RequestThrottler(Duration.ofSeconds(1));
  }

  public synchronized void acquire() throws InterruptedException {
    long now = System.nanoTime();
    long elapsed = now - lastRequestTimeNano;

    if (elapsed < intervalNanos) {
      long remainingNanos = intervalNanos - elapsed;
      long remainingMillis = (remainingNanos + 999_999L) / 1_000_000L;
      Thread.sleep(remainingMillis);
    }

    lastRequestTimeNano = System.nanoTime();
  }
}
