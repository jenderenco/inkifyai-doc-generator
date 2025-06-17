package io.github.jenderenco.inkifyai.openapi;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.jenderenco.inkifyai.openapi.config.OpenApiProperties;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class OpenApiFetcherTest {

  // Create properties with minimal retries for faster tests
  private static final OpenApiProperties PROPERTIES =
      new OpenApiProperties(1000, 1000, 1, 10, true, 10, 1000);

  @Mock private WebClient.Builder webClientBuilder;

  @Mock private WebClient webClient;

  @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  private OpenApiFetcher fetcher;

  @BeforeEach
  void setUp() {
    // Setup WebClient mock chain
    when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
    when(webClientBuilder.build()).thenReturn(webClient);

    fetcher = new OpenApiFetcher(webClientBuilder);
  }

  @ParameterizedTest
  @ValueSource(strings = {"invalid-url", "ftp://example.com/api"})
  void validateUrlWithInvalidUrl(String url) {
    assertThatThrownBy(() -> fetcher.fetch(url, PROPERTIES))
        .isInstanceOf(OpenApiFetchException.class)
        .hasMessageContaining("Failed to fetch OpenAPI specification");
  }
}
