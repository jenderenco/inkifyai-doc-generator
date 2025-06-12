package io.github.jenderenco.inkifyai.service;

import io.github.jenderenco.inkifyai.llm.client.LlmClient;
import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.llm.prompt.PromptService;
import io.github.jenderenco.inkifyai.openapi.OpenApiFetcher;
import io.github.jenderenco.inkifyai.openapi.OpenApiParser;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import io.github.jenderenco.inkifyai.openapi.model.ParsedOpenApiSpec;
import org.springframework.stereotype.Service;

/** Service for generating documentation from OpenAPI specifications. */
@Service
public class DocumentationService {

  private final OpenApiFetcher fetcher;
  private final OpenApiParser openApiParser;
  private final PromptService promptService;
  private final LlmClientRegistry llmClientRegistry;

  public DocumentationService(
      OpenApiFetcher fetcher,
      OpenApiParser openApiParser,
      PromptService promptService,
      LlmClientRegistry llmClientRegistry) {
    this.fetcher = fetcher;
    this.openApiParser = openApiParser;
    this.promptService = promptService;
    this.llmClientRegistry = llmClientRegistry;
  }

  /**
   * Generate documentation from an OpenAPI specification URL using the specified AI provider.
   *
   * @param openApiUrl the URL of the OpenAPI specification
   * @param aiProvider the AI provider to use for generating documentation
   * @return the generated documentation
   * @throws OpenApiFetchException if the OpenAPI specification cannot be fetched
   * @throws IllegalArgumentException if the OpenAPI specification is invalid, the AI provider is
   *     not supported, or the LLM fails to generate documentation
   */
  public String generateFromUrl(String openApiUrl, String aiProvider) {
    String rawSpec = fetcher.fetch(openApiUrl);
    ParsedOpenApiSpec parsed = openApiParser.parse(rawSpec);
    String prompt = promptService.buildPrompt(parsed);

    LlmClient llmClient = llmClientRegistry.getClient(aiProvider);
    return llmClient
        .complete(prompt)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Failed to generate documentation from the OpenAPI specification"));
  }
}
