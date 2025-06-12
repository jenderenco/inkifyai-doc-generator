package io.github.jenderenco.inkifyai.openapi;

import io.github.jenderenco.inkifyai.openapi.config.OpenApiProperties;
import io.github.jenderenco.inkifyai.openapi.exception.OpenApiFetchException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class OpenApiFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(OpenApiFetcher.class);

  private final WebClient webClient;
  private final OpenApiProperties properties;

  public OpenApiFetcher(WebClient.Builder webClientBuilder, OpenApiProperties properties) {
    this.webClient =
        webClientBuilder
            .codecs(
                configurer ->
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB max size
            .build();
    this.properties = properties;
  }

  /**
   * Fetches an OpenAPI specification from the given URL.
   *
   * @param url the URL to fetch the OpenAPI specification from
   * @return the OpenAPI specification as a string
   * @throws OpenApiFetchException if the URL is invalid or the fetch fails
   */
  @Cacheable(value = "openApiSpecs", key = "#url", condition = "#properties.cacheSize > 0")
  public String fetch(String url) {
    LOG.info("Fetching OpenAPI specification from: {}", url);

    try {
      validateUrl(url);

      return webClient
          .get()
          .uri(url)
          .retrieve()
          .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
          .bodyToMono(String.class)
          .timeout(Duration.ofMillis(properties.readTimeout()))
          .retryWhen(
              Retry.backoff(properties.maxRetries(), Duration.ofMillis(properties.retryDelay()))
                  .filter(throwable -> !(throwable instanceof OpenApiFetchException))
                  .doBeforeRetry(
                      retrySignal ->
                          LOG.warn(
                              "Retrying fetch attempt {} after error: {}",
                              retrySignal.totalRetries() + 1,
                              retrySignal.failure().getMessage())))
          .doOnSuccess(
              spec ->
                  LOG.debug(
                      "Successfully fetched OpenAPI specification ({} characters)", spec.length()))
          .doOnError(e -> LOG.error("Failed to fetch OpenAPI specification", e))
          .block();
    } catch (Exception e) {
      throw new OpenApiFetchException("Failed to fetch OpenAPI specification", e);
    }
  }

  private Mono<? extends Throwable> handleErrorResponse(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .flatMap(
            errorBody -> {
              HttpStatusCode status = response.statusCode();
              String message =
                  String.format("Error fetching OpenAPI spec: %s %s", status.value(), errorBody);
              LOG.error(message);
              return Mono.error(new OpenApiFetchException(message));
            });
  }

  private void validateUrl(String url) {
    if (!properties.validateUrl()) {
      return;
    }

    URI uri = toUri(url).orElseThrow(() -> new OpenApiFetchException("Invalid URL: " + url));
    if (!uri.isAbsolute()) {
      throw new OpenApiFetchException("URL must be absolute: " + url);
    }

    Optional.ofNullable(uri.getScheme())
        .filter(s -> s.equals("http") || s.equals("https"))
        .orElseThrow(
            () -> new OpenApiFetchException("URL must use http or https protocol: " + url));
  }

  private Optional<URI> toUri(String url) {
    try {
      return Optional.of(new URI(url));
    } catch (URISyntaxException e) {
      return Optional.empty();
    }
  }
}
