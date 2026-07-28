package com.springaimcp.controller;

import com.springaimcp.model.Temple;
import com.springaimcp.service.TempleAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/temples")
@Tag(name = "Temple REST Endpoints", description = "REST APIs for querying templeinfo database records directly")
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

    @Operation(summary = "Search temples by state, district, city or keyword", description = "Filters temples based on matching criteria")
    @GetMapping("/search")
    public List<Temple> searchTemples(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword) {
        return templeAiService.search(state, district, city, keyword);
    }
}
