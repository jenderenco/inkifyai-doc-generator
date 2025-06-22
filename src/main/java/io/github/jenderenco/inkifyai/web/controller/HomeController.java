package io.github.jenderenco.inkifyai.web.controller;

import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import io.github.jenderenco.inkifyai.service.DocumentationService;
import io.github.jenderenco.inkifyai.web.controller.config.ApiProperties;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

/**
 * Controller for handling web requests to the application. Provides endpoints for the home page and
 * documentation generation.
 */
@Controller
public class HomeController {

  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  private final DocumentationService documentationService;
  private final LlmClientRegistry llmClientRegistry;
  private final ApiProperties apiProperties;

  /**
   * Constructs a new HomeController with the given services.
   *
   * @param documentationService the service for generating documentation
   * @param llmClientRegistry the registry of LLM clients
   */
  public HomeController(
      DocumentationService documentationService,
      LlmClientRegistry llmClientRegistry,
      ApiProperties apiProperties) {
    this.documentationService = documentationService;
    this.llmClientRegistry = llmClientRegistry;
    this.apiProperties = apiProperties;
  }

  /**
   * Handles requests to the home page.
   *
   * @param model the Spring MVC model
   * @return the name of the view to render
   */
  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("aiProviders", llmClientRegistry.getSupportedClients());
    return "home";
  }

  /**
   * Handles requests to generate documentation.
   *
   * @param url the URL of the OpenAPI specification
   * @param aiProvider the AI provider to use for generating documentation
   * @param model the Spring MVC model
   * @return a Mono containing the name of the view to render
   */
  @PostMapping("/generate-docs")
  public DeferredResult<String> generateDocs(
      @RequestParam("url") String url,
      @RequestParam(value = "aiProvider", defaultValue = "ollama") String aiProvider,
      Model model) {
    DeferredResult<String> result =
        new DeferredResult<>(apiProperties.generateDocs().timeout().toMillis());
    documentationService
        .generateFromUrl(url, aiProvider)
        .collect(Collectors.joining())
        .map(
            markdown -> {
              model.addAttribute("markdown", markdown);
              model.addAttribute("aiProvider", aiProvider);
              return "result";
            })
        .onErrorResume(e -> handleError(e, model))
        .subscribe(result::setResult, result::setErrorResult);

    return result;
  }

  /**
   * Handles errors that occur during documentation generation.
   *
   * @param e the throwable that caused the error
   * @param model the Spring MVC model
   * @return a Mono containing the name of the error view to render
   */
  private Mono<String> handleError(Throwable e, Model model) {
    return switch (e) {
      case OpenApiFetchException ex ->
          renderError(
              model,
              "Failed to fetch API specification",
              "We couldn't fetch the OpenAPI specification from the provided URL. "
                  + "Please check that the URL is correct and accessible, then try again.",
              ex);

      case IllegalArgumentException ex ->
          renderError(
              model,
              "Failed to generate documentation",
              "We encountered an issue while generating documentation: " + ex.getMessage(),
              ex);

      default ->
          renderError(
              model,
              "Unexpected error",
              "An unexpected error occurred while generating your documentation. Please try again later.",
              e);
    };
  }

  /**
   * Renders an error page with the given title and message.
   *
   * @param model the Spring MVC model
   * @param title the error title
   * @param message the error message
   * @param e the throwable that caused the error
   * @return a Mono containing the name of the error view to render
   */
  private Mono<String> renderError(Model model, String title, String message, Throwable e) {
    logger.error(title, e);
    model.addAttribute("errorTitle", title);
    model.addAttribute("errorMessage", message);
    return Mono.just("error");
  }
}
