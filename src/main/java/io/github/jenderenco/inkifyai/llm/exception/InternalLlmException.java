package io.github.jenderenco.inkifyai.llm.exception;

/**
 * Exception thrown when there's an internal error in the LLM client. This is a runtime exception
 * that wraps the underlying cause.
 */
public class InternalLlmException extends RuntimeException {

  /**
   * Constructs a new InternalLlmException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public InternalLlmException(String message, Throwable cause) {
    super(message, cause);
  }
}
