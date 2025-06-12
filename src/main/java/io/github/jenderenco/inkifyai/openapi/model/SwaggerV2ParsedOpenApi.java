package io.github.jenderenco.inkifyai.openapi.model;

import io.swagger.models.Info;
import io.swagger.models.Swagger;
import java.util.Map;
import java.util.Optional;

/** Implementation of ParsedOpenApiSpec for Swagger v2 specifications. */
public record SwaggerV2ParsedOpenApi(
    String title, Optional<String> description, Map<String, ?> paths, Map<String, ?> schemas)
    implements ParsedOpenApiSpec {

  /**
   * Create a SwaggerV2ParsedOpenApi from a Swagger object.
   *
   * @param swagger the Swagger object
   */
  public SwaggerV2ParsedOpenApi(Swagger swagger) {
    this(
        Optional.ofNullable(swagger.getInfo()).map(Info::getTitle).orElse("No title"),
        Optional.ofNullable(swagger.getInfo()).map(Info::getDescription),
        Optional.ofNullable(swagger.getPaths()).orElseGet(Map::of),
        Optional.ofNullable(swagger.getDefinitions()).orElseGet(Map::of));
  }
}
