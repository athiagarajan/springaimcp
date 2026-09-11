package com.springaimcp.config;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileConfigurationTest {

    @Nested
    @SpringBootTest
    @ActiveProfiles("local")
    class LocalProfileTest {

        @MockBean
        private ChatClient.Builder chatClientBuilder;

        @MockBean
        private JdbcTemplate jdbcTemplate;

        @Autowired
        private Environment environment;

        @Test
        void testLocalProfileProperties() {
            String url = environment.getProperty("spring.datasource.url");
            String username = environment.getProperty("spring.datasource.username");
            String port = environment.getProperty("server.port");

            assertThat(url).isNotNull();
            assertThat(url).contains("dthiagar-ubuntu");
            assertThat(url).contains("5432");
            assertThat(url).contains("sslmode=disable");
            assertThat(username).isEqualTo("templeinfo_user");
            assertThat(port).isEqualTo("8080");
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("render")
    class RenderProfileTest {

        @MockBean
        private ChatClient.Builder chatClientBuilder;

        @MockBean
        private JdbcTemplate jdbcTemplate;

        @Autowired
        private Environment environment;

        @Test
        void testRenderProfileProperties() {
            String url = environment.getProperty("spring.datasource.url");
            String username = environment.getProperty("spring.datasource.username");

            assertThat(url).isNotNull();
            assertThat(url).contains("templeinfo-db.cnwoi0c6ort7.us-east-2.rds.amazonaws.com");
            assertThat(url).contains("sslmode=require");
            assertThat(username).isEqualTo("postgres");
        }
    }
}