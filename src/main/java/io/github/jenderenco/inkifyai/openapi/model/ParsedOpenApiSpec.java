package io.github.jenderenco.inkifyai.openapi.model;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for parsed OpenAPI specifications. This interface defines the common methods for all
 * ParsedOpenApi implementations.
 */
public interface ParsedOpenApiSpec {
  /**
   * Get the title of the API.
   *
   * @return the title
   */
  String title();

  /**
   * Get the description of the API.
   *
   * @return an Optional containing the description, or empty if not available
   */
  Optional<String> description();

  /**
   * Get the paths defined in the API.
   *
   * @return a map of paths
   */
  Map<String, ?> paths();

  /**
   * Get the schemas defined in the API.
   *
   * @return a map of schemas
   */
  Map<String, ?> schemas();
}
