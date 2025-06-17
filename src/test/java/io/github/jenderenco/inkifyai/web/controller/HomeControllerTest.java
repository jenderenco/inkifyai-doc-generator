package io.github.jenderenco.inkifyai.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import io.github.jenderenco.inkifyai.service.DocumentationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

  @Mock private DocumentationService documentationService;
  @Mock private LlmClientRegistry llmClientRegistry;
  @Mock private Model model;

  @InjectMocks private HomeController homeController;

  @Test
  void homeEndpoint() {
    // Arrange
    List<String> supportedClients = List.of("ollama", "openai");
    when(llmClientRegistry.getSupportedClients()).thenReturn(supportedClients);
    when(model.addAttribute("aiProviders", supportedClients)).thenReturn(model);

    // Act
    String viewName = homeController.home(model);

    // Assert
    assertThat(viewName).isEqualTo("home");
  }

  @Test
  void generateDocsEndpoint() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    String generatedMarkdown = "# Generated Documentation\n\nThis is a test.";

    when(documentationService.generateFromUrl(url, aiProvider)).thenReturn(generatedMarkdown);
    when(model.addAttribute("markdown", generatedMarkdown)).thenReturn(model);
    when(model.addAttribute("aiProvider", aiProvider)).thenReturn(model);

    // Act
    String viewName = homeController.generateDocs(url, aiProvider, model);

    // Assert
    assertThat(viewName).isEqualTo("result");
  }

  @Test
  void generateDocsWithDefaultAiProvider() {
    // Arrange
    String url = "https://example.com/api-docs";
    String defaultAiProvider = "ollama";
    String generatedMarkdown = "# Generated Documentation\n\nThis is a test.";

    when(documentationService.generateFromUrl(url, defaultAiProvider))
        .thenReturn(generatedMarkdown);
    when(model.addAttribute("markdown", generatedMarkdown)).thenReturn(model);
    when(model.addAttribute("aiProvider", defaultAiProvider)).thenReturn(model);

    // Act
    String viewName = homeController.generateDocs(url, defaultAiProvider, model);

    // Assert
    assertThat(viewName).isEqualTo("result");
  }

  @Test
  void generateDocsWithOpenApiFetchException() {
    // Arrange
    String url = "https://example.com/invalid-url";
    String aiProvider = "ollama";
    OpenApiFetchException exception =
        new OpenApiFetchException("Failed to fetch OpenAPI spec", new RuntimeException());

    when(documentationService.generateFromUrl(url, aiProvider)).thenThrow(exception);
    when(model.addAttribute(eq("errorTitle"), anyString())).thenReturn(model);
    when(model.addAttribute(eq("errorMessage"), anyString())).thenReturn(model);

    // Act
    String viewName = homeController.generateDocs(url, aiProvider, model);

    // Assert
    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute(eq("errorTitle"), eq("Failed to fetch API specification"));
    verify(model).addAttribute(eq("errorMessage"), anyString());
  }

  @Test
  void generateDocsWithIllegalArgumentException() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "invalid-provider";
    IllegalArgumentException exception = new IllegalArgumentException("Unsupported AI provider");

    when(documentationService.generateFromUrl(url, aiProvider)).thenThrow(exception);
    when(model.addAttribute(eq("errorTitle"), anyString())).thenReturn(model);
    when(model.addAttribute(eq("errorMessage"), anyString())).thenReturn(model);

    // Act
    String viewName = homeController.generateDocs(url, aiProvider, model);

    // Assert
    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute(eq("errorTitle"), eq("Failed to generate documentation"));
    verify(model).addAttribute(eq("errorMessage"), anyString());
  }

  @Test
  void generateDocsWithUnexpectedException() {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    RuntimeException exception = new RuntimeException("Unexpected error");

    when(documentationService.generateFromUrl(url, aiProvider)).thenThrow(exception);
    when(model.addAttribute(eq("errorTitle"), anyString())).thenReturn(model);
    when(model.addAttribute(eq("errorMessage"), anyString())).thenReturn(model);

    // Act
    String viewName = homeController.generateDocs(url, aiProvider, model);

    // Assert
    assertThat(viewName).isEqualTo("error");
    verify(model).addAttribute(eq("errorTitle"), eq("Unexpected error"));
    verify(model).addAttribute(eq("errorMessage"), anyString());
  }
}
