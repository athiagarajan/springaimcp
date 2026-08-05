package com.springaimcp.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class OllamaConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(60000);
            factory.setReadTimeout(300000);
            builder.requestFactory(factory);
        };
    }

    @Bean
    public RestClient.Builder restClientBuilder(RestClientCustomizer customizer) {
        RestClient.Builder builder = RestClient.builder();
        customizer.customize(builder);
        return builder;
    }
}
