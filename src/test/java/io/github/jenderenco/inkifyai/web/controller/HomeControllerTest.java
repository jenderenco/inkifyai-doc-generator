package io.github.jenderenco.inkifyai.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import io.github.jenderenco.inkifyai.service.DocumentationService;
import io.github.jenderenco.inkifyai.web.controller.config.ApiProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DocumentationService documentationService;

  @MockitoBean private LlmClientRegistry llmClientRegistry;

  @MockitoBean private ApiProperties apiProperties;

  @Test
  void homeEndpoint() throws Exception {
    // Arrange
    List<String> supportedClients = List.of("ollama", "openai");
    when(llmClientRegistry.getSupportedClients()).thenReturn(supportedClients);

    // Act & Assert
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("home"))
        .andExpect(model().attribute("aiProviders", supportedClients));
  }

  @Test
  void generateDocsEndpoint() throws Exception {
    // Arrange
    String url = "https://example.com/api-docs";
    String aiProvider = "ollama";
    String markdown = "# Generated Documentation\n\nThis is a test.";

    ApiProperties.GenerateDocs generateDocs = new ApiProperties.GenerateDocs(Duration.ofSeconds(1));
    when(apiProperties.generateDocs()).thenReturn(generateDocs);
    when(documentationService.generateFromUrl(url, aiProvider)).thenReturn(Flux.just(markdown));

    MvcResult mvcResult =
        mockMvc
            .perform(post("/generate-docs").param("url", url).param("aiProvider", aiProvider))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

    // Act & Assert
    mockMvc
        .perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk())
        .andExpect(view().name("result"))
        .andExpect(model().attribute("markdown", markdown))
        .andExpect(model().attribute("aiProvider", aiProvider));
  }

  @Test
  void generateDocsWithDefaultAiProvider() throws Exception {
    // Arrange
    String url = "https://example.com/api-docs";
    String markdown = "# Default Provider Response";

    ApiProperties.GenerateDocs generateDocs = new ApiProperties.GenerateDocs(Duration.ofSeconds(1));
    when(apiProperties.generateDocs()).thenReturn(generateDocs);
    when(documentationService.generateFromUrl(url, "ollama")).thenReturn(Flux.just(markdown));

    MvcResult mvcResult =
        mockMvc
            .perform(post("/generate-docs").param("url", url)) // no aiProvider param, uses default
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

    // Act & Assert
    mockMvc
        .perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk())
        .andExpect(view().name("result"))
        .andExpect(model().attribute("markdown", markdown))
        .andExpect(model().attribute("aiProvider", "ollama"));
  }

  @Test
  void generateDocsWithOpenApiFetchException() throws Exception {
    // Arrange
    String url = "https://example.com/invalid";
    OpenApiFetchException ex = new OpenApiFetchException("Failed to fetch spec");

    ApiProperties.GenerateDocs generateDocs = new ApiProperties.GenerateDocs(Duration.ofSeconds(1));
    when(apiProperties.generateDocs()).thenReturn(generateDocs);
    when(documentationService.generateFromUrl(eq(url), any())).thenReturn(Flux.error(ex));

    MvcResult mvcResult =
        mockMvc
            .perform(post("/generate-docs").param("url", url))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

    // Act & Assert
    mockMvc
        .perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk())
        .andExpect(view().name("error"))
        .andExpect(model().attribute("errorTitle", "Failed to fetch API specification"))
        .andExpect(model().attributeExists("errorMessage"));
  }

  @Test
  void generateDocsWithIllegalArgumentException() throws Exception {
    // Arrange
    String url = "https://example.com/api-docs";
    IllegalArgumentException ex = new IllegalArgumentException("Invalid provider");

    ApiProperties.GenerateDocs generateDocs = new ApiProperties.GenerateDocs(Duration.ofSeconds(1));
    when(apiProperties.generateDocs()).thenReturn(generateDocs);
    when(documentationService.generateFromUrl(eq(url), any())).thenReturn(Flux.error(ex));

    MvcResult mvcResult =
        mockMvc
            .perform(post("/generate-docs").param("url", url).param("aiProvider", "invalid"))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

    // Act & Assert
    mockMvc
        .perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk())
        .andExpect(view().name("error"))
        .andExpect(model().attribute("errorTitle", "Failed to generate documentation"))
        .andExpect(model().attributeExists("errorMessage"));
  }

  @Test
  void generateDocsWithUnexpectedException() throws Exception {
    // Arrange
    String url = "https://example.com/api-docs";

    ApiProperties.GenerateDocs generateDocs = new ApiProperties.GenerateDocs(Duration.ofSeconds(1));
    when(apiProperties.generateDocs()).thenReturn(generateDocs);
    when(documentationService.generateFromUrl(eq(url), any()))
        .thenReturn(Flux.error(new RuntimeException("Unexpected")));

    MvcResult mvcResult =
        mockMvc
            .perform(post("/generate-docs").param("url", url).param("aiProvider", "ollama"))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

    // Act & Assert
    mockMvc
        .perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk())
        .andExpect(view().name("error"))
        .andExpect(model().attribute("errorTitle", "Unexpected error"))
        .andExpect(model().attributeExists("errorMessage"));
  }
}
