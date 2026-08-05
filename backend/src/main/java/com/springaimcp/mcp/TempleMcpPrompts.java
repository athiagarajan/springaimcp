package com.springaimcp.mcp;

import org.springframework.stereotype.Component;

@Component
public class TempleMcpPrompts {

    public String buildTempleQueryPrompt(String userPrompt) {
        return """
            You are a expert assistant specialized in ancient Indian temples.
            Access the templeinfo PostgreSQL database using the registered temple tools to answer the user query:
            
            User Query: %s
            
            Synthesize detailed answers with temple history, moolavar, festivals, and GPS coordinates where available.
            """.formatted(userPrompt);
    }
}
