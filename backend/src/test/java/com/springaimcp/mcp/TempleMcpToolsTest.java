package com.springaimcp.mcp;

import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TempleMcpToolsTest {

    private TempleRepository repository;
    private TempleMcpTools tools;

    @BeforeEach
    void setUp() {
        repository = mock(TempleRepository.class);
        tools = new TempleMcpTools(repository);
    }

    @Test
    void testSearchTemples() {
        Temple sample = new Temple(1L, "Sample", null, null, null, null, null, null, null, null, "Palani", "Dindigul", "TN", null, null, null, null, null, null, null, null, null, null, null, null, 10.0, 77.0, null, null, null, null);
        when(repository.search("Palani")).thenReturn(List.of(sample));

        List<Temple> result = tools.searchTemples("Palani");
        assertEquals(1, result.size());
        assertEquals("Palani", result.get(0).city());
    }

    @Test
    void testFindNearbyTemples() {
        when(repository.findNearby(10.0, 77.0, 5.0)).thenReturn(List.of());
        List<Temple> result = tools.findNearbyTemples(10.0, 77.0, 5.0);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTempleById() {
        Temple sample = new Temple(10L, "Test Temple", null, null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        when(repository.findById(10L)).thenReturn(Optional.of(sample));

        Temple result = tools.getTempleById(10);
        assertNotNull(result);
        assertEquals(10L, result.id());
    }
}
