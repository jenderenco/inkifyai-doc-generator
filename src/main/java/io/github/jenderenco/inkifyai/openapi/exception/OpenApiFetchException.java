package io.github.jenderenco.inkifyai.openapi.exception;

/** Exception thrown when there is an error fetching an OpenAPI specification. */
public class OpenApiFetchException extends RuntimeException {

  /**
   * Constructs a new OpenApiFetchException with the specified detail message.
   *
   * @param message the detail message
   */
  public OpenApiFetchException(String message) {
    super(message);
  }

  /**
   * Constructs a new OpenApiFetchException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public OpenApiFetchException(String message, Throwable cause) {
    super(message, cause);
  }
}
