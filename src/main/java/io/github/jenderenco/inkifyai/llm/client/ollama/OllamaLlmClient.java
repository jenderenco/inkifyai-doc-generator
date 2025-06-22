package io.github.jenderenco.inkifyai.llm.client.ollama;

import io.github.jenderenco.inkifyai.llm.client.LlmClient;
import io.github.jenderenco.inkifyai.llm.exception.InternalLlmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Implementation of the LlmClient interface for the Ollama LLM provider. Uses Spring AI's
 * OllamaChatModel to interact with Ollama.
 */
@Service("ollamaClient")
public class OllamaLlmClient implements LlmClient {

  private static final Logger LOG = LoggerFactory.getLogger(OllamaLlmClient.class);

  private final ChatClient chatClient;

  /**
   * Constructs a new OllamaLlmClient with the given chat model.
   *
   * @param chatModel the Ollama chat model to use
   */
  public OllamaLlmClient(OllamaChatModel chatModel) {
    this.chatClient = ChatClient.builder(chatModel).build();
  }

  /**
   * Sends a prompt to the Ollama LLM and returns a stream of response chunks.
   *
   * @param prompt the prompt to send to the LLM
   * @return a Flux of response chunks from the LLM
   * @throws InternalLlmException if there's an error initiating the LLM stream
   */
  @Override
  public Flux<String> complete(String prompt) {
    try {
      return chatClient.prompt().user(prompt).stream()
          .content()
          .doOnNext(chunk -> LOG.debug("Ollama LLM response chunk: {}", chunk))
          .doOnError(e -> LOG.error("Error streaming Ollama LLM response", e));
    } catch (Exception ex) {
      LOG.error("Error initiating Ollama LLM stream", ex);
      return Flux.error(new InternalLlmException("Ollama LLM stream failed", ex));
    }
  }

  /**
   * Returns the name of the LLM provider.
   *
   * @return the provider name "ollama"
   */
  @Override
  public String providerName() {
    return "ollama";
  }
}
