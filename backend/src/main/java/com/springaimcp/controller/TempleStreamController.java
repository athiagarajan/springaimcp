package com.springaimcp.controller;

import com.springaimcp.service.TempleAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/temples")
@Tag(name = "Streamable Temple AI APIs", description = "Server-Sent Events (SSE) streaming endpoints for dynamic queries and MCP responses")
public class TempleStreamController {

    private final TempleAiService templeAiService;

    public TempleStreamController(TempleAiService templeAiService) {
        this.templeAiService = templeAiService;
    }

    @Operation(summary = "Stream NL-to-SQL dynamic query reasoning & results",
               description = "Streams real-time reasoning tokens, generated SQL, and result sets as Server-Sent Events (SSE)")
    @GetMapping(value = "/stream/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQuery(
            @Parameter(description = "Natural language prompt or query regarding temples", required = true)
            @RequestParam String prompt) {
        return templeAiService.streamDynamicQuery(prompt);
    }
}
