package com.springaimcp.controller;

import com.springaimcp.model.Temple;
import com.springaimcp.service.TempleAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/temples")
@Tag(name = "Temple REST Endpoints", description = "REST APIs for querying templeinfo database records directly")
@SecurityRequirement(name = "basicAuth")
public class TempleRestController {

    private final TempleAiService templeAiService;

    public TempleRestController(TempleAiService templeAiService) {
        this.templeAiService = templeAiService;
    }

    @Operation(summary = "Get all temple records", description = "Returns all 96 temples from templeinfo database")
    @GetMapping
    public List<Temple> getAllTemples() {
        return templeAiService.getAllTemples();
    }

    @Operation(summary = "Search temples by keyword using LLM NL-to-SQL", description = "Parses prompt into PostgreSQL SQL via LLM and executes directly")
    @GetMapping("/search")
    public Mono<List<Temple>> searchTemples(@RequestParam(required = false) String keyword) {
        return templeAiService.search(keyword);
    }

    @Operation(summary = "Translate temple details to another language (Pattern A)", description = "Translates temple descriptive fields to target language (e.g. 'ta' for Tamil) via LLM pass")
    @GetMapping("/{id}/translate")
    public Mono<Temple> translateTemple(@PathVariable Long id, @RequestParam(defaultValue = "ta") String targetLang) {
        return templeAiService.translateTemple(id, targetLang);
    }
}
