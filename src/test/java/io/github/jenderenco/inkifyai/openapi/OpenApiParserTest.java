package io.github.jenderenco.inkifyai.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jenderenco.inkifyai.openapi.model.ParsedOpenApiSpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

@SpringBootTest
class OpenApiParserTest {

  @Autowired private OpenApiParser openApiParser;

  @Test
  void parseSwaggerV2() throws IOException {
    // Load Swagger v2 spec from resource file
    File file = ResourceUtils.getFile("classpath:openapi/swagger-v2-spec.json");
    String swaggerV2Spec = Files.readString(file.toPath());

    // Parse the spec
    ParsedOpenApiSpec parsed = openApiParser.parse(swaggerV2Spec);

    // Verify the parsed data using AssertJ
    assertThat(parsed.title()).isEqualTo("Test API v2");
    assertThat(parsed.description().orElse(null)).isEqualTo("A test API using Swagger v2");
    assertThat(parsed.paths()).isNotEmpty();
    assertThat(parsed.schemas()).isNotEmpty();
  }

  @Test
  void parseOpenApiV3() throws IOException {
    // Load OpenAPI v3 spec from a resource file
    File file = ResourceUtils.getFile("classpath:openapi/openapi-v3-spec.json");
    String openApiV3Spec = Files.readString(file.toPath());

    // Parse the spec
    ParsedOpenApiSpec parsed = openApiParser.parse(openApiV3Spec);

    // Verify the parsed data using AssertJ
    assertThat(parsed.title()).isEqualTo("Test API v3");
    assertThat(parsed.description().orElse(null)).isEqualTo("A test API using OpenAPI v3");
    assertThat(parsed.paths()).isNotEmpty();
    assertThat(parsed.schemas()).isNotEmpty();
  }

  @Test
  void invalidSpec() throws IOException {
    // Load invalid spec from a resource file
    File file = ResourceUtils.getFile("classpath:openapi/invalid-spec.json");
    String invalidSpec = Files.readString(file.toPath());

    // Verify that an exception is thrown using AssertJ
    assertThatThrownBy(() -> openApiParser.parse(invalidSpec))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
