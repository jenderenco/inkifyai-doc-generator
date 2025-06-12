package io.github.jenderenco.inkifyai.llm.client;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Registry for LLM clients. This class manages all available LlmClients and provides methods to get
 * the appropriate client for a given provider name.
 */
@Component
public class LlmClientRegistry {

  private final List<LlmClient> clients;

  public LlmClientRegistry(List<LlmClient> clients) {
    this.clients = clients;
  }

  /**
   * Get the LLM client for the specified provider name.
   *
   * @param providerName the provider name
   * @return the LLM client for the specified provider
   * @throws IllegalArgumentException if no provider supports the given name
   */
  public LlmClient getClient(String providerName) {
    return clients.stream()
        .filter(provider -> provider.supports(providerName))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unsupported AI provider: " + providerName));
  }

  /**
   * Get the list of supported client names.
   *
   * @return list of supported client names
   */
  public List<String> getSupportedClients() {
    return clients.stream().map(LlmClient::providerName).toList();
  }
}
