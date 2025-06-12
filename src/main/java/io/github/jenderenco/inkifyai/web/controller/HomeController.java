package io.github.jenderenco.inkifyai.web.controller;

import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import io.github.jenderenco.inkifyai.service.DocumentationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  private final DocumentationService documentationService;
  private final LlmClientRegistry llmClientRegistry;

  public HomeController(
      DocumentationService documentationService, LlmClientRegistry llmClientRegistry) {
    this.documentationService = documentationService;
    this.llmClientRegistry = llmClientRegistry;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("aiProviders", llmClientRegistry.getSupportedClients());
    return "home";
  }

  @PostMapping("/generate-docs")
  public String generateDocs(
      @RequestParam("url") String url,
      @RequestParam(value = "aiProvider", defaultValue = "ollama") String aiProvider,
      Model model) {
    try {
      String markdown = documentationService.generateFromUrl(url, aiProvider);
      model.addAttribute("markdown", markdown);
      model.addAttribute("aiProvider", aiProvider);
      return "result";
    } catch (OpenApiFetchException e) {
      logger.error("Failed to fetch OpenAPI specification from URL: {}", url, e);
      model.addAttribute("errorTitle", "Failed to fetch API specification");
      model.addAttribute(
          "errorMessage",
          "We couldn't fetch the OpenAPI specification from the provided URL. "
              + "Please check that the URL is correct and accessible, then try again.");
      return "error";
    } catch (IllegalArgumentException e) {
      logger.error("Error generating documentation: {}", e.getMessage(), e);
      model.addAttribute("errorTitle", "Failed to generate documentation");
      model.addAttribute(
          "errorMessage",
          "We encountered an issue while generating documentation: " + e.getMessage());
      return "error";
    } catch (Exception e) {
      logger.error("Unexpected error generating documentation", e);
      model.addAttribute("errorTitle", "Unexpected error");
      model.addAttribute(
          "errorMessage",
          "An unexpected error occurred while generating your documentation. "
              + "Please try again later.");
      return "error";
    }
  }
}
