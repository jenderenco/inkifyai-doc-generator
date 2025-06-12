package io.github.jenderenco.inkifyai.openapi;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.jenderenco.inkifyai.openapi.config.OpenApiProperties;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class OpenApiFetcherTest {

  @Mock private WebClient.Builder webClientBuilder;

  @Mock private WebClient webClient;

  @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  private OpenApiFetcher fetcher;

  @BeforeEach
  void setUp() {
    // Create properties with minimal retries for faster tests
    OpenApiProperties properties = new OpenApiProperties(1000, 1000, 1, 10, true, 10, 1000);

    // Setup WebClient mock chain
    when(webClientBuilder.codecs(org.mockito.ArgumentMatchers.any())).thenReturn(webClientBuilder);
    when(webClientBuilder.build()).thenReturn(webClient);

    fetcher = new OpenApiFetcher(webClientBuilder, properties);
  }

  @Test
  void testValidateUrlWithInvalidUrl() {
    // Test with invalid URL
    assertThatThrownBy(() -> fetcher.fetch("invalid-url"))
        .isInstanceOf(OpenApiFetchException.class)
        .hasMessageContaining("Failed to fetch OpenAPI specification");

    // Test with non-http URL
    assertThatThrownBy(() -> fetcher.fetch("ftp://example.com/api"))
        .isInstanceOf(OpenApiFetchException.class)
        .hasMessageContaining("Failed to fetch OpenAPI specification");
  }
}
