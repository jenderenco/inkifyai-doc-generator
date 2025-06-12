package io.github.jenderenco.inkifyai.openapi.model;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import java.util.Optional;

/** Implementation of ParsedOpenApiSpec for OpenAPI v3 specifications. */
public record OpenApiV3ParsedOpenApi(
    String title, Optional<String> description, Map<String, ?> paths, Map<String, ?> schemas)
    implements ParsedOpenApiSpec {

  /**
   * Create an OpenApiV3ParsedOpenApi from an OpenAPI object.
   *
   * @param openAPI the OpenAPI object
   */
  public OpenApiV3ParsedOpenApi(OpenAPI openAPI) {
    this(
        extractTitle(openAPI),
        extractDescription(openAPI),
        extractPaths(openAPI),
        extractSchemas(openAPI));
  }

  private static String extractTitle(OpenAPI openAPI) {
    return Optional.ofNullable(openAPI.getInfo()).map(Info::getTitle).orElse("No title");
  }

  private static Optional<String> extractDescription(OpenAPI openAPI) {
    return Optional.ofNullable(openAPI.getInfo()).map(Info::getDescription);
  }

  private static Map<String, PathItem> extractPaths(OpenAPI openAPI) {
    return Optional.ofNullable(openAPI.getPaths()).map(Map::copyOf).orElseGet(Map::of);
  }

  private static Map<String, Schema> extractSchemas(OpenAPI openAPI) {
    return Optional.ofNullable(openAPI.getComponents())
        .map(Components::getSchemas)
        .orElseGet(Map::of);
  }
}
