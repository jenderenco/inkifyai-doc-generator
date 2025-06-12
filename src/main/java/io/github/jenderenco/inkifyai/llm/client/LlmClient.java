package io.github.jenderenco.inkifyai.llm.client;

import java.util.Optional;

public interface LlmClient {

  Optional<String> complete(String prompt);

  String providerName();

  default boolean supports(String providerName) {
    return providerName().equalsIgnoreCase(providerName);
  }
}
