package io.github.jenderenco.inkifyai;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jenderenco.inkifyai.llm.client.LlmClientRegistry;
import io.github.jenderenco.inkifyai.openapi.config.OpenApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class InkifyaiDocGeneratorApplicationTests {

  @Autowired private ApplicationContext context;

  @Test
  void contextLoads() {
    // Verify that the application context loads successfully
    assertThat(context).isNotNull();
  }

  @Test
  void configurationPropertiesAreLoaded() {
    // Verify that configuration properties are loaded correctly
    OpenApiProperties openApiProperties = context.getBean(OpenApiProperties.class);
    assertThat(openApiProperties).isNotNull();
    assertThat(openApiProperties.maxRetries()).isEqualTo(3);
    assertThat(openApiProperties.validateUrl()).isTrue();
  }

  @Test
  void requiredBeansAreAvailable() {
    // Verify that essential beans are available in the context
    assertThat(context.containsBean("llmClientRegistry")).isTrue();
    assertThat(context.getBean(LlmClientRegistry.class)).isNotNull();
  }
}
