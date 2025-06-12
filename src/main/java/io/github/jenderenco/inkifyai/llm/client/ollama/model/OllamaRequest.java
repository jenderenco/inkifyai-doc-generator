package io.github.jenderenco.inkifyai.llm.client.ollama.model;

public record OllamaRequest(String model, String prompt, boolean stream, double temperature) {}
