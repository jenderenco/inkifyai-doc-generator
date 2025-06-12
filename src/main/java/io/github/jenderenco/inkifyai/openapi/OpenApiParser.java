package io.github.jenderenco.inkifyai.openapi;

import io.github.jenderenco.inkifyai.openapi.model.OpenApiV3ParsedOpenApi;
import io.github.jenderenco.inkifyai.openapi.model.ParsedOpenApiSpec;
import io.github.jenderenco.inkifyai.openapi.model.SwaggerV2ParsedOpenApi;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Parser for OpenAPI specifications. Supports both OpenAPI v3 and Swagger v2 formats. Creates
 * appropriate ParsedOpenApiSpec implementations based on the OpenAPI version.
 */
@Component
public class OpenApiParser {

  /**
   * Parse an OpenAPI specification string.
   *
   * @param rawSpec the raw OpenAPI specification string
   * @return a ParsedOpenApiSpec object
   * @throws IllegalArgumentException if the specification is invalid or unsupported
   */
  public ParsedOpenApiSpec parse(String rawSpec) {
    return parseV3(rawSpec)
        .or(() -> parseV2(rawSpec))
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Invalid or unsupported OpenAPI spec (tried both v2 and v3)."));
  }

  /**
   * Try to parse the specification as OpenAPI v3.
   *
   * @param rawSpec the raw OpenAPI specification string
   * @return an Optional containing the ParsedOpenApiSpec if successful, or empty if not
   */
  private Optional<ParsedOpenApiSpec> parseV3(String rawSpec) {
    SwaggerParseResult result = new OpenAPIV3Parser().readContents(rawSpec);
    return Optional.ofNullable(result.getOpenAPI()).map(OpenApiV3ParsedOpenApi::new);
  }

  /**
   * Try to parse the specification as Swagger v2.
   *
   * @param rawSpec the raw OpenAPI specification string
   * @return an Optional containing the ParsedOpenApiSpec if successful, or empty if not
   */
  private Optional<ParsedOpenApiSpec> parseV2(String rawSpec) {
    return Optional.ofNullable(new SwaggerParser().parse(rawSpec)).map(SwaggerV2ParsedOpenApi::new);
  }
}
