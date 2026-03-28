package org.operaton.dev.starter.server;

import org.operaton.dev.starter.server.config.StarterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StarterProperties.class)
public class StarterServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarterServerApplication.class, args);
    }
}
