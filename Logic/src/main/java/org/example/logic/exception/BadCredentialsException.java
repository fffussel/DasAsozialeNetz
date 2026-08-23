package org.example.logic.exception;

public class BadCredentialsException extends RuntimeException {
  public BadCredentialsException(String message) {
    super(message);
  }

  public BadCredentialsException(String message, Throwable cause) {
  }
}
