package io.github.jenderenco.inkifyai.web.controller;

import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.service.DocumentationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

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
    String markdown = documentationService.generateFromUrl(url, aiProvider);
    model.addAttribute("markdown", markdown);
    model.addAttribute("aiProvider", aiProvider);
    return "result";
  }
}
