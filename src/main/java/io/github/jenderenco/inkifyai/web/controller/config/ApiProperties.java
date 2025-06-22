package io.github.jenderenco.inkifyai.web.controller.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api")
public record ApiProperties(GenerateDocs generateDocs) {
  public record GenerateDocs(Duration timeout) {}
}
