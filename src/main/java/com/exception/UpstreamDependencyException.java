package com.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
public class UpstreamDependencyException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UpstreamDependencyException(String message) {
    super(message);
  }

  public UpstreamDependencyException(String message, Throwable cause) {
    super(message, cause);
  }
}
