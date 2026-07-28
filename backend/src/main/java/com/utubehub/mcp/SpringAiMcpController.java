package com.utubehub.mcp;

import com.utubehub.entity.ChannelEntity;
import com.utubehub.entity.VideoEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/mcp")
@Tag(name = "Spring AI Model Context Protocol (MCP)", description = "Model Context Protocol endpoints exposing AI Tools, Resources, and Prompts via Spring AI 1.0.0")
public class SpringAiMcpController {

    private final SpringAiMcpService mcpService;

    @Autowired
    public SpringAiMcpController(SpringAiMcpService mcpService) {
        this.mcpService = mcpService;
    }

    @GetMapping("/tools")
    @Operation(summary = "List Registered MCP Tools", description = "Lists all registered Spring AI Model Context Protocol Tools")
    public ResponseEntity<List<Map<String, String>>> listTools() {
        return ResponseEntity.ok(List.of(
            Map.of(
                "name", "syncUserSubscriptionsTool",
                "description", "Model Context Protocol Tool: Syncs user YouTube subscriptions and videos using OAuth access token",
                "type", "TOOL"
            ),
            Map.of(
                "name", "searchSubscriptionsMcpTool",
                "description", "Model Context Protocol Tool: Searches indexed YouTube subscriptions and videos using natural language prompt queries",
                "type", "TOOL"
            )
        ));
    }

    @GetMapping("/resources")
    @Operation(summary = "List Registered MCP Resources", description = "Lists all registered Spring AI Model Context Protocol Data Resources")
    public ResponseEntity<List<Map<String, String>>> listResources() {
        return ResponseEntity.ok(List.of(
            Map.of(
                "uri", "mcp://utubehub/channels/{userId}",
                "name", "getChannelResourceMcp",
                "description", "Model Context Protocol Resource: Fetches YouTube channel metadata and subscriber metrics",
                "mimeType", "application/json"
            )
        ));
    }

    @GetMapping("/prompts")
    @Operation(summary = "List Registered MCP Prompts", description = "Lists all registered Spring AI Model Context Protocol Prompt Templates")
    public ResponseEntity<List<Map<String, String>>> listPrompts() {
        return ResponseEntity.ok(List.of(
            Map.of(
                "name", "generateRefinementPromptMcp",
                "description", "Model Context Protocol Prompt: Generates structured Gemini AI prompt template for natural language YouTube content refining"
            )
        ));
    }

    @PostMapping("/tools/search")
    @Operation(summary = "Execute MCP Search Tool", description = "Executes the Spring AI MCP Search Tool to query indexed subscriptions and videos")
    public ResponseEntity<List<VideoEntity>> executeSearchTool(
            @RequestParam(required = false, defaultValue = "athiagarajan@gmail.com") String userId,
            @RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(mcpService.searchSubscriptionsMcpTool(userId, query));
    }

    @GetMapping("/resources/channels")
    @Operation(summary = "Fetch MCP Channel Resource", description = "Reads the Spring AI MCP Channel Data Resource")
    public ResponseEntity<List<ChannelEntity>> fetchChannelResource(
            @RequestParam(required = false, defaultValue = "athiagarajan@gmail.com") String userId) {
        return ResponseEntity.ok(mcpService.getChannelResourceMcp(userId));
    }

    @PostMapping("/prompts/generate")
    @Operation(summary = "Generate MCP Prompt Template", description = "Generates a structured Gemini AI Prompt Template via Spring AI MCP")
    public ResponseEntity<Map<String, String>> generatePrompt(
            @RequestParam(required = false, defaultValue = "Spring Boot 3.4 microservices tutorials") String query) {
        String promptText = mcpService.generateRefinementPromptMcp(query);
        return ResponseEntity.ok(Map.of("prompt", promptText, "rawQuery", query));
    }
}
