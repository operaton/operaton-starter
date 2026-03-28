package org.operaton.dev.starter.server.config;

import org.operaton.dev.starter.templates.engine.GenerationEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GenerationEngineConfig {

    private final StarterProperties properties;

    public GenerationEngineConfig(StarterProperties properties) {
        this.properties = properties;
    }

    @Bean
    public GenerationEngine generationEngine() {
        return new GenerationEngine();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = properties.cors().allowedOrigins().toArray(String[]::new);
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(origins)
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
