package io.github.jenderenco.inkifyai.llm.client.ollama;

import io.github.jenderenco.inkifyai.llm.client.LlmClient;
import io.github.jenderenco.inkifyai.llm.client.ollama.config.OllamaProperties;
import io.github.jenderenco.inkifyai.llm.client.ollama.exception.InternalLlmException;
import io.github.jenderenco.inkifyai.llm.client.ollama.model.OllamaChunk;
import io.github.jenderenco.inkifyai.llm.client.ollama.model.OllamaRequest;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service("ollamaClient")
public class OllamaLlmClient implements LlmClient {

  private static final Logger LOG = LoggerFactory.getLogger(OllamaLlmClient.class);

  private final WebClient webClient;
  private final OllamaProperties ollamaProperties;

  public OllamaLlmClient(WebClient.Builder webClientBuilder, OllamaProperties ollamaProperties) {
    this.webClient =
        webClientBuilder
            .baseUrl(ollamaProperties.url())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    this.ollamaProperties = ollamaProperties;
  }

  @Override
  public Optional<String> complete(String prompt) {
    var request =
        new OllamaRequest(ollamaProperties.model(), prompt, true, ollamaProperties.temperature());
    String result =
        webClient
            .post()
            .uri("/api/generate")
            .bodyValue(request)
            .accept(MediaType.APPLICATION_NDJSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::handleError)
            .bodyToFlux(OllamaChunk.class) // see below
            .takeUntil(OllamaChunk::done)
            .map(
                chunk -> {
                  LOG.debug("Received chunk: {}", chunk.response());
                  return chunk.response();
                })
            .collect(Collectors.joining())
            .block();

    return Optional.ofNullable(result);
  }

  @Override
  public String providerName() {
    return "ollama";
  }

  private Mono<Throwable> handleError(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .flatMap(
            body -> {
              HttpStatusCode status = response.statusCode();
              LOG.error("Ollama responded with {}: {}", status, body);
              return Mono.error(new InternalLlmException("LLM call failed [" + status + "]", body));
            });
  }
}
