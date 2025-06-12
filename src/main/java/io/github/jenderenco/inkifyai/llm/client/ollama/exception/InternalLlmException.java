package io.github.jenderenco.inkifyai.llm.client.ollama.exception;

public class InternalLlmException extends RuntimeException {
  public InternalLlmException(String message, String responseBody) {
    super(message + ": " + responseBody);
  }

  public InternalLlmException(String message, String responseBody, Throwable cause) {
    super(message + ": " + responseBody, cause);
  }
}
