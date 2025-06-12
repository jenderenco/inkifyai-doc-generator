package io.github.jenderenco.inkifyai.llm.prompt;

import io.github.jenderenco.inkifyai.openapi.model.ParsedOpenApiSpec;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

  private static final Logger LOG = LoggerFactory.getLogger(PromptService.class);

  public String buildPrompt(ParsedOpenApiSpec api) {
    return """
                You are an expert API technical writer. Your task is to generate high-quality GitBook documentation in Markdown format based on the OpenAPI specification provided.

                ## API Title
                %s

                ## API Description
                %s

                ## Endpoints
                %s

                ## Data Models (Schemas)
                %s

                Guidelines:
                - Use Markdown syntax with consistent headings.
                - Use code blocks for JSON examples or request/response payloads.
                - Use tables for parameters and schema fields where applicable.
                - Do not use OpenAPI or Swagger-specific jargon in the text.
                - Group endpoints by common path prefix if possible.
                - Highlight optional vs required parameters.
                - Provide practical, human-readable descriptions for each endpoint and model.
                - Include HTTP method and endpoint name prominently.
                - Be concise, but informative.
                - Avoid stating 'No description' or '(no ID)' – infer a descriptive name when missing.

                Example Format:
                ### `GET /users`
                Get a list of users.

                ### `POST /users`
                Create a new user.

                ### Schema: `User`
                A representation of a system user including ID, name, and email.
                """
        .formatted(
            api.title(),
            api.description().orElse("This API has no description."),
            summarizeEndpoints(api.paths()),
            summarizeSchemas(api.schemas()));
  }

  private String summarizeEndpoints(Map<String, ?> pathsRaw) {
    return pathsRaw.entrySet().stream()
        .map(entry -> formatPath(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining("\n"));
  }

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

  private void handleV3Operation(
      StringJoiner joiner, String method, io.swagger.v3.oas.models.Operation op) {
    Optional.ofNullable(op)
        .map(o -> formatOperation(method, o.getOperationId(), o.getSummary()))
        .ifPresent(joiner::add);
  }

  private String formatOperation(String method, String operationId, String summary) {
    String id = Optional.ofNullable(operationId).orElse("UnnamedOperation");
    String desc = Optional.ofNullable(summary).orElse("No summary available.");
    return "  - **" + method + " " + id + "**: " + desc;
  }

  private String summarizeSchemas(Map<String, ?> schemasRaw) {
    return schemasRaw.entrySet().stream()
        .map(entry -> formatSchema(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining("\n"));
  }

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
