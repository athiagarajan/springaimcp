package com.springaimcp.config;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

// @Configuration
public class OllamaConfig {

    @Bean
    @Primary
    public OllamaApi ollamaApi(WebClient.Builder webClientBuilder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(600000); // 10 minutes (600,000ms) read timeout for local CPU inference

        RestClient.Builder restClientBuilder = RestClient.builder().requestFactory(factory);
        return new OllamaApi("http://localhost:11434", restClientBuilder, webClientBuilder);
    }

    @Bean
    @Primary
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder()
                        .model("qwen2.5-coder:latest")
                        .temperature(0.0)
                        .numPredict(80)
                        .build())
                .build();
    }
}
