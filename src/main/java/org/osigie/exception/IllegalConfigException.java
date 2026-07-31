package org.osigie.exception;

public class IllegalConfigException extends RuntimeException {
  public IllegalConfigException(String message) {
    super(message);
  }

  public IllegalConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
