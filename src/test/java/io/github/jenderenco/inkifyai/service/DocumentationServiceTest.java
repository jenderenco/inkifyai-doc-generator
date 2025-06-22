package io.github.jenderenco.inkifyai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.jenderenco.inkifyai.llm.client.LlmClient;
import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.llm.prompt.PromptService;
import io.github.jenderenco.inkifyai.openapi.OpenApiFetcher;
import io.github.jenderenco.inkifyai.openapi.OpenApiParser;
import io.github.jenderenco.inkifyai.openapi.config.OpenApiProperties;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import io.github.jenderenco.inkifyai.openapi.model.ParsedOpenApiSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DocumentationServiceTest {

  @Mock private OpenApiFetcher fetcher;
  @Mock private OpenApiParser openApiParser;
  @Mock private PromptService promptService;
  @Mock private LlmClientRegistry llmClientRegistry;
  @Mock private LlmClient llmClient;
  @Mock private ParsedOpenApiSpec parsedOpenApiSpec;
  @Mock private OpenApiProperties openApiProperties;

  @InjectMocks private DocumentationService documentationService;

  @Test
  void generateFromUrlSuccess() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    String rawSpec = "{ \"openapi\": \"3.0.0\" }";
    String prompt = "Generate documentation for this API";
    String generatedDoc = "# API Documentation\n\nThis is the documentation.";

    when(fetcher.fetch(url, openApiProperties)).thenReturn(rawSpec);
    when(openApiParser.parse(rawSpec)).thenReturn(parsedOpenApiSpec);
    when(promptService.buildPrompt(parsedOpenApiSpec)).thenReturn(prompt);
    when(llmClientRegistry.getClient(aiProvider)).thenReturn(llmClient);
    when(llmClient.complete(prompt)).thenReturn(Flux.just(generatedDoc));

    // Act & Assert
    StepVerifier.create(documentationService.generateFromUrl(url, aiProvider))
        .expectNext(generatedDoc)
        .verifyComplete();
  }

  @Test
  void generateFromUrlWithFetchException() {
    // Arrange
    String url = "https://example.com/invalid-api-docs";
    String aiProvider = "ollama";

    when(fetcher.fetch(url, openApiProperties))
        .thenThrow(new OpenApiFetchException("Failed to fetch OpenAPI specification"));

    // Act & Assert
    StepVerifier.create(documentationService.generateFromUrl(url, aiProvider))
        .expectErrorSatisfies(
            error -> {
              assertThat(error).isInstanceOf(OpenApiFetchException.class);
              assertThat(error).hasMessageContaining("Failed to fetch OpenAPI specification");
            })
        .verify();
  }

  @Test
  void generateFromUrlWithInvalidSpec() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    String rawSpec = "{ \"invalid\": \"spec\" }";

    when(fetcher.fetch(url, openApiProperties)).thenReturn(rawSpec);
    when(openApiParser.parse(rawSpec))
        .thenThrow(new IllegalArgumentException("Invalid OpenAPI specification"));

    // Act & Assert
    StepVerifier.create(documentationService.generateFromUrl(url, aiProvider))
        .expectErrorSatisfies(
            error -> {
              assertThat(error).isInstanceOf(IllegalArgumentException.class);
              assertThat(error).hasMessageContaining("Invalid OpenAPI specification");
            })
        .verify();
  }

  @Test
  void generateFromUrlWithUnsupportedAiProvider() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "unsupported";
    String rawSpec = "{ \"openapi\": \"3.0.0\" }";

    when(fetcher.fetch(url, openApiProperties)).thenReturn(rawSpec);
    when(openApiParser.parse(rawSpec)).thenReturn(parsedOpenApiSpec);
    when(llmClientRegistry.getClient(aiProvider))
        .thenThrow(new IllegalArgumentException("Unsupported AI provider"));

    // Act & Assert
    StepVerifier.create(documentationService.generateFromUrl(url, aiProvider))
        .expectErrorSatisfies(
            error -> {
              assertThat(error).isInstanceOf(IllegalArgumentException.class);
              assertThat(error).hasMessageContaining("Unsupported AI provider");
            })
        .verify();
  }

  @Test
  void generateFromUrlWithLlmFailure() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    String rawSpec = "{ \"openapi\": \"3.0.0\" }";
    String prompt = "Generate documentation for this API";

    when(fetcher.fetch(url, openApiProperties)).thenReturn(rawSpec);
    when(openApiParser.parse(rawSpec)).thenReturn(parsedOpenApiSpec);
    when(promptService.buildPrompt(parsedOpenApiSpec)).thenReturn(prompt);
    when(llmClientRegistry.getClient(aiProvider)).thenReturn(llmClient);
    when(llmClient.complete(prompt)).thenReturn(Flux.empty());

    // Act & Assert
    StepVerifier.create(documentationService.generateFromUrl(url, aiProvider))
        .expectErrorSatisfies(
            error -> {
              assertThat(error).isInstanceOf(IllegalArgumentException.class);
              assertThat(error).hasMessageContaining("Failed to generate documentation");
            })
        .verify();
  }
}
