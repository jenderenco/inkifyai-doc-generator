package io.github.jenderenco.inkifyai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.jenderenco.inkifyai.llm.client.LlmClient;
import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.llm.prompt.PromptService;
import io.github.jenderenco.inkifyai.openapi.OpenApiFetcher;
import io.github.jenderenco.inkifyai.openapi.OpenApiParser;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import io.github.jenderenco.inkifyai.openapi.model.ParsedOpenApiSpec;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentationServiceTest {

  @Mock private OpenApiFetcher fetcher;
  @Mock private OpenApiParser openApiParser;
  @Mock private PromptService promptService;
  @Mock private LlmClientRegistry llmClientRegistry;
  @Mock private LlmClient llmClient;
  @Mock private ParsedOpenApiSpec parsedOpenApiSpec;

  @InjectMocks private DocumentationService documentationService;

  @Test
  void testGenerateFromUrlSuccess() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    String rawSpec = "{ \"openapi\": \"3.0.0\" }";
    String prompt = "Generate documentation for this API";
    String generatedDoc = "# API Documentation\n\nThis is the documentation.";

    when(fetcher.fetch(url)).thenReturn(rawSpec);
    when(openApiParser.parse(rawSpec)).thenReturn(parsedOpenApiSpec);
    when(promptService.buildPrompt(parsedOpenApiSpec)).thenReturn(prompt);
    when(llmClientRegistry.getClient(aiProvider)).thenReturn(llmClient);
    when(llmClient.complete(prompt)).thenReturn(Optional.of(generatedDoc));

    // Act
    String result = documentationService.generateFromUrl(url, aiProvider);

    // Assert
    assertThat(result).isEqualTo(generatedDoc);
  }

  @Test
  void testGenerateFromUrlWithFetchException() {
    // Arrange
    String url = "https://example.com/invalid-api-docs";
    String aiProvider = "ollama";

    when(fetcher.fetch(url))
        .thenThrow(new OpenApiFetchException("Failed to fetch OpenAPI specification"));

    // Act & Assert
    assertThatThrownBy(() -> documentationService.generateFromUrl(url, aiProvider))
        .isInstanceOf(OpenApiFetchException.class)
        .hasMessageContaining("Failed to fetch OpenAPI specification");
  }

  @Test
  void testGenerateFromUrlWithInvalidSpec() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    String rawSpec = "{ \"invalid\": \"spec\" }";

    when(fetcher.fetch(url)).thenReturn(rawSpec);
    when(openApiParser.parse(rawSpec))
        .thenThrow(new IllegalArgumentException("Invalid OpenAPI specification"));

    // Act & Assert
    assertThatThrownBy(() -> documentationService.generateFromUrl(url, aiProvider))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid OpenAPI specification");
  }

  @Test
  void testGenerateFromUrlWithUnsupportedAiProvider() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "unsupported";
    String rawSpec = "{ \"openapi\": \"3.0.0\" }";

    when(fetcher.fetch(url)).thenReturn(rawSpec);
    when(openApiParser.parse(rawSpec)).thenReturn(parsedOpenApiSpec);
    when(llmClientRegistry.getClient(aiProvider))
        .thenThrow(new IllegalArgumentException("Unsupported AI provider"));

    // Act & Assert
    assertThatThrownBy(() -> documentationService.generateFromUrl(url, aiProvider))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported AI provider");
  }

  @Test
  void testGenerateFromUrlWithLlmFailure() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    String rawSpec = "{ \"openapi\": \"3.0.0\" }";
    String prompt = "Generate documentation for this API";

    when(fetcher.fetch(url)).thenReturn(rawSpec);
    when(openApiParser.parse(rawSpec)).thenReturn(parsedOpenApiSpec);
    when(promptService.buildPrompt(parsedOpenApiSpec)).thenReturn(prompt);
    when(llmClientRegistry.getClient(aiProvider)).thenReturn(llmClient);
    when(llmClient.complete(prompt)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> documentationService.generateFromUrl(url, aiProvider))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Failed to generate documentation");
  }
}
