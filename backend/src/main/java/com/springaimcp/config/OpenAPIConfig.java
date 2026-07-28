package com.springaimcp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI springAiMcpOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("springaimcp-backend API")
                .description("Streamable endpoints powered by Spring Boot 4 / Java 25, Spring AI, Model Context Protocol (MCP), and PostgreSQL templeinfo database.")
                .version("v1.0.0")
                .contact(new Contact().name("Spring AI MCP Team")));
    }
}
