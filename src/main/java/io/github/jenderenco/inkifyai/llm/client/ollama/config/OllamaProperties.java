package io.github.jenderenco.inkifyai.llm.client.ollama.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm.ollama")
public record OllamaProperties(String url, String model, double temperature) {}
