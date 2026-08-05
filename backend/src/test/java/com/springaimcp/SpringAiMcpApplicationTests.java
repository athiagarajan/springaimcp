package com.springaimcp;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class SpringAiMcpApplicationTests {

    @MockBean
    private ChatClient.Builder chatClientBuilder;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // Verifies Spring context loads cleanly
    }
}
