package com.springaimcp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI springAiMcpOpenAPI() {
        final String securitySchemeName = "basicAuth";
        return new OpenAPI()
            .info(new Info()
                .title("springaimcp backend API")
                .description("Protected streamable endpoints powered by Spring Boot 4 / Java 25, Spring AI 2.0, MCP, and PostgreSQL templeinfo database.")
                .version("v1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")));
    }
}
