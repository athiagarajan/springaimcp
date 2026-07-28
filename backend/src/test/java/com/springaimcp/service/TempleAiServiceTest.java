package com.springaimcp.service;

import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TempleAiServiceTest {

    private TempleRepository templeRepository;
    private TempleAiService templeAiService;

    @BeforeEach
    void setUp() {
        templeRepository = Mockito.mock(TempleRepository.class);
        templeAiService = new TempleAiService(null, templeRepository);
    }

    @Test
    void testGetAllTemples() {
        Temple t = new Temple(1L, "Temple 1", "Moolavar", null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, 10.0, 77.0, null, null, null, null);
        when(templeRepository.findAll()).thenReturn(List.of(t));

        List<Temple> result = templeAiService.getAllTemples();
        assertEquals(1, result.size());
        assertEquals("Temple 1", result.get(0).name());
    }

    @Test
    void testSearch() {
        Temple t = new Temple(1L, "Searched Temple", "Moolavar", null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, 10.0, 77.0, null, null, null, null);
        when(templeRepository.searchByCriteria(anyString(), anyString(), anyString(), anyString())).thenReturn(List.of(t));

        List<Temple> result = templeAiService.search("TN", "Dindigul", "Palani", "Idumban");
        assertEquals(1, result.size());
        assertEquals("Searched Temple", result.get(0).name());
    }
}
