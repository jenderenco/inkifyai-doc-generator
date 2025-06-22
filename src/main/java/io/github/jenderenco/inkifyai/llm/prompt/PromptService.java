package io.github.jenderenco.inkifyai.llm.prompt;

import io.github.jenderenco.inkifyai.openapi.model.ParsedOpenApiSpec;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

/**
 * Service for building prompts for LLMs based on OpenAPI specifications. This service loads a
 * prompt template from the classpath and fills it with data from a ParsedOpenApiSpec to create a
 * complete prompt for the LLM.
 */
@Service
public class PromptService {

  private static final Logger LOG = LoggerFactory.getLogger(PromptService.class);

  private final PromptTemplate promptTemplate;

  /**
   * Constructs a new PromptService. Loads the prompt template from the classpath during
   * initialization.
   */
  public PromptService() {
    this.promptTemplate = new PromptTemplate(loadPromptTemplate());
  }

  /**
   * Builds a prompt for the LLM based on the given OpenAPI specification.
   *
   * @param api the parsed OpenAPI specification
   * @return the complete prompt for the LLM
   */
  public String buildPrompt(ParsedOpenApiSpec api) {
    Map<String, Object> templateVars =
        Map.of(
            "title", api.title(),
            "description", api.description().orElse("This API has no description."),
            "endpoints", summarizeEndpoints(api.paths()),
            "schemas", summarizeSchemas(api.schemas()));

    return promptTemplate.render(templateVars);
  }

  /**
   * Loads the prompt template from the classpath.
   *
   * @return the prompt template as a string
   * @throws IllegalStateException if the template cannot be loaded
   */
  private String loadPromptTemplate() {
    try {
      ClassPathResource resource =
          new ClassPathResource("prompts/gitbook-documentation-template.txt");
      return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      LOG.error("Failed to load prompt template from classpath", ex);
      throw new IllegalStateException("Unable to load prompt template", ex);
    }
  }

  /**
   * Summarizes the endpoints from the OpenAPI specification.
   *
   * @param pathsRaw the map of paths from the OpenAPI specification
   * @return a string representation of the endpoints
   */
  private String summarizeEndpoints(Map<String, ?> pathsRaw) {
    return pathsRaw.entrySet().stream()
        .map(entry -> formatPath(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining("\n"));
  }

  /**
   * Formats a path and its operations.
   *
   * @param path the path string
   * @param pathObj the path object (either Swagger v2 Path or OpenAPI v3 PathItem)
   * @return a formatted string representation of the path and its operations
   */
  private String formatPath(String path, Object pathObj) {
    StringJoiner joiner = new StringJoiner("\n", "- `" + path + "`\n", "");

    if (pathObj instanceof Path v2Path) {
      v2Path
          .getOperationMap()
          .forEach(
              (method, operation) ->
                  joiner.add(
                      formatOperation(
                          method.name(), operation.getOperationId(), operation.getSummary())));
    } else if (pathObj instanceof PathItem v3Path) {
      handleV3Operation(joiner, "GET", v3Path.getGet());
      handleV3Operation(joiner, "POST", v3Path.getPost());
      handleV3Operation(joiner, "PUT", v3Path.getPut());
      handleV3Operation(joiner, "DELETE", v3Path.getDelete());
      handleV3Operation(joiner, "PATCH", v3Path.getPatch());
      handleV3Operation(joiner, "HEAD", v3Path.getHead());
      handleV3Operation(joiner, "OPTIONS", v3Path.getOptions());
    } else {
      LOG.warn("Unknown path object type: {}", pathObj.getClass().getName());
      joiner.add("  - Unable to process this endpoint type");
    }

    return joiner.toString();
  }

  /**
   * Handles an OpenAPI v3 operation.
   *
   * @param joiner the string joiner to add the formatted operation to
   * @param method the HTTP method
   * @param op the operation object
   */
  private void handleV3Operation(
      StringJoiner joiner, String method, io.swagger.v3.oas.models.Operation op) {
    Optional.ofNullable(op)
        .map(o -> formatOperation(method, o.getOperationId(), o.getSummary()))
        .ifPresent(joiner::add);
  }

  /**
   * Formats an operation.
   *
   * @param method the HTTP method
   * @param operationId the operation ID
   * @param summary the operation summary
   * @return a formatted string representation of the operation
   */
  private String formatOperation(String method, String operationId, String summary) {
    String id = Optional.ofNullable(operationId).orElse("UnnamedOperation");
    String desc = Optional.ofNullable(summary).orElse("No summary available.");
    return "  - **" + method + " " + id + "**: " + desc;
  }

  /**
   * Summarizes the schemas from the OpenAPI specification.
   *
   * @param schemasRaw the map of schemas from the OpenAPI specification
   * @return a string representation of the schemas
   */
  private String summarizeSchemas(Map<String, ?> schemasRaw) {
    return schemasRaw.entrySet().stream()
        .map(entry -> formatSchema(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining("\n"));
  }

  /**
   * Formats a schema.
   *
   * @param name the schema name
   * @param schemaObj the schema object (either Swagger v2 Model or OpenAPI v3 Schema)
   * @return a formatted string representation of the schema
   */
  private String formatSchema(String name, Object schemaObj) {
    String description =
        Optional.ofNullable(
                switch (schemaObj) {
                  case Model model -> model.getDescription();
                  case Schema<?> schema -> schema.getDescription();
                  default -> {
                    LOG.warn("Unknown schema object type: {}", schemaObj.getClass().getName());
                    yield null;
                  }
                })
            .orElse("No description available.");

    return "- `" + name + "`: " + description;
  }
}
