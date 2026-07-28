package com.utubehub.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SpringAiMcpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listToolsShouldReturn200OK() throws Exception {
        mockMvc.perform(get("/api/v1/mcp/tools")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("syncUserSubscriptionsTool"));
    }

    @Test
    void listResourcesShouldReturn200OK() throws Exception {
        mockMvc.perform(get("/api/v1/mcp/resources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("getChannelResourceMcp"));
    }

    @Test
    void listPromptsShouldReturn200OK() throws Exception {
        mockMvc.perform(get("/api/v1/mcp/prompts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("generateRefinementPromptMcp"));
    }

    @Test
    void generatePromptShouldReturn200OK() throws Exception {
        mockMvc.perform(post("/api/v1/mcp/prompts/generate")
                .param("query", "Spring Boot MCP Tutorial")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompt").exists());
    }
}
