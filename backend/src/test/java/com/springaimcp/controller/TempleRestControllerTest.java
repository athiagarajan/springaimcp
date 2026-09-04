package com.springaimcp.controller;

import com.springaimcp.model.Temple;
import com.springaimcp.service.TempleAiService;
import com.springaimcp.service.TempleImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TempleRestControllerTest {

    private TempleAiService templeAiService;
    private TempleImageService templeImageService;
    private TempleRestController restController;

    @BeforeEach
    void setUp() {
        templeAiService = Mockito.mock(TempleAiService.class);
        templeImageService = Mockito.mock(TempleImageService.class);
        restController = new TempleRestController(templeAiService, templeImageService);
    }

    @Test
    void testGetAllTemples() {
        Temple sample = new Temple(1L, "Sample", "Moolavar", null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, 10.0, 77.0, null, null, null, null);
        when(templeAiService.getAllTemples()).thenReturn(List.of(sample));

        List<Temple> result = restController.getAllTemples();
        assertEquals(1, result.size());
        assertEquals("Sample", result.get(0).name());
    }

    @Test
    void testSearchTemples() {
        Temple sample = new Temple(1L, "Searched", "Moolavar", null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, 10.0, 77.0, null, null, null, null);
        when(templeAiService.search(anyString())).thenReturn(Mono.just(List.of(sample)));

        Mono<List<Temple>> resultMono = restController.searchTemples("murugan");
        List<Temple> result = resultMono.block();
        assertEquals(1, result.size());
        assertEquals("Searched", result.get(0).name());
    }
}
