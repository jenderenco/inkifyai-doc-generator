package io.github.jenderenco.inkifyai.llm.client;

import reactor.core.publisher.Flux;

/**
 * Interface for LLM (Large Language Model) clients. Implementations of this interface provide
 * access to different LLM providers.
 */
public interface LlmClient {

  /**
   * Sends a prompt to the LLM and returns a stream of response chunks.
   *
   * @param prompt the prompt to send to the LLM
   * @return a Flux of response chunks from the LLM
   */
  Flux<String> complete(String prompt);

  /**
   * Returns the name of the LLM provider.
   *
   * @return the provider name
   */
  String providerName();

  /**
   * Checks if this client supports the given provider name.
   *
   * @param providerName the provider name to check
   * @return true if this client supports the given provider name, false otherwise
   */
  default boolean supports(String providerName) {
    return providerName().equalsIgnoreCase(providerName);
  }
}
