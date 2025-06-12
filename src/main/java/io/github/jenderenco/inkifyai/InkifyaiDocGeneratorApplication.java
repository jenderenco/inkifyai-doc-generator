package io.github.jenderenco.inkifyai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InkifyaiDocGeneratorApplication {

  public static void main(String[] args) {
    SpringApplication.run(InkifyaiDocGeneratorApplication.class, args);
  }
}
