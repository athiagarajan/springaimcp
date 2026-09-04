package com.springaimcp.controller;

import com.springaimcp.service.TempleAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                .thenReturn(Flux.just("Chunk1", "Chunk2"));

        Flux<ServerSentEvent<Map<String, String>>> result = streamController.streamQuery("Lord Shiva temples");

        StepVerifier.create(result)
                .assertNext(sse -> assertEquals("Chunk1", sse.data().get("text")))
                .assertNext(sse -> assertEquals("Chunk2", sse.data().get("text")))
                .assertNext(sse -> {
                    assertEquals("complete", sse.event());
                    assertEquals("[DONE]", sse.data().get("text"));
                })
                .verifyComplete();
    }
}
