package io.github.jenderenco.inkifyai.service;

import io.github.jenderenco.inkifyai.llm.client.LlmClient;
import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.llm.prompt.PromptService;
import io.github.jenderenco.inkifyai.openapi.OpenApiFetcher;
import io.github.jenderenco.inkifyai.openapi.OpenApiParser;
import io.github.jenderenco.inkifyai.openapi.config.OpenApiProperties;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import io.github.jenderenco.inkifyai.openapi.model.ParsedOpenApiSpec;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/** Service for generating documentation from OpenAPI specifications. */
@Service
public class DocumentationService {

  private final OpenApiFetcher fetcher;
  private final OpenApiParser openApiParser;
  private final PromptService promptService;
  private final LlmClientRegistry llmClientRegistry;
  private final OpenApiProperties openApiProperties;

  public DocumentationService(
      OpenApiFetcher fetcher,
      OpenApiParser openApiParser,
      PromptService promptService,
      LlmClientRegistry llmClientRegistry,
      OpenApiProperties openApiProperties) {
    this.fetcher = fetcher;
    this.openApiParser = openApiParser;
    this.promptService = promptService;
    this.llmClientRegistry = llmClientRegistry;
    this.openApiProperties = openApiProperties;
  }

  /**
   * Generate documentation from an OpenAPI specification URL using the specified AI provider.
   *
   * @param openApiUrl the URL of the OpenAPI specification
   * @param aiProvider the AI provider to use for generating documentation
   * @return the generated documentation
   * @throws OpenApiFetchException if the OpenAPI specification cannot be fetched
   * @throws IllegalArgumentException if the OpenAPI specification is invalid or the AI provider is
   *     not supported
   */
  public Flux<String> generateFromUrl(String openApiUrl, String aiProvider) {
    return Flux.defer(
        () -> {
          String rawSpec = fetcher.fetch(openApiUrl, openApiProperties);
          ParsedOpenApiSpec parsed = openApiParser.parse(rawSpec);
          String prompt = promptService.buildPrompt(parsed);
          LlmClient client = llmClientRegistry.getClient(aiProvider);

          return client
              .complete(prompt)
              .switchIfEmpty(
                  Flux.error(new IllegalArgumentException("Failed to generate documentation")));
        });
  }
}
