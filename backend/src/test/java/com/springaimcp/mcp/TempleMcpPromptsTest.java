package com.springaimcp.mcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TempleMcpPromptsTest {

    @Test
    void testBuildTempleQueryPrompt() {
        TempleMcpPrompts prompts = new TempleMcpPrompts();
        String result = prompts.buildTempleQueryPrompt("Palani temples");

        assertTrue(result.contains("User Query: Palani temples"));
        assertTrue(result.contains("templeinfo PostgreSQL database"));
    }
}
