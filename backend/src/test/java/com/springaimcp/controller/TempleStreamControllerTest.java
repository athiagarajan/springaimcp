package com.springaimcp.controller;

import com.springaimcp.service.TempleAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TempleStreamControllerTest {

    private TempleAiService templeAiService;
    private TempleStreamController streamController;

    @BeforeEach
    void setUp() {
        templeAiService = Mockito.mock(TempleAiService.class);
        streamController = new TempleStreamController(templeAiService);
    }

    @Test
    void testStreamQuery() {
        when(templeAiService.streamDynamicQuery(anyString()))
                .thenReturn(Flux.just("Chunk1", "Chunk2", "Chunk3"));

        Flux<String> result = streamController.streamQuery("Lord Shiva temples");

        StepVerifier.create(result)
                .expectNext("Chunk1")
                .expectNext("Chunk2")
                .expectNext("Chunk3")
                .verifyComplete();
    }
}
