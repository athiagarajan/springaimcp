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
        templeAiService = new TempleAiService(templeRepository);
    }

    @Test
    void testGetAllTemples() {
        Temple t = new Temple(1L, "Temple 1", "Moolavar", null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, 10.0, 77.0, null, null, null, null);
        when(templeRepository.executeDynamicSql(anyString())).thenReturn(List.of(t));

        List<Temple> result = templeAiService.getAllTemples();
        assertEquals(1, result.size());
        assertEquals("Temple 1", result.get(0).name());
    }

    @Test
    void testGenerateDeterministicSqlForMuruganDistance() {
        String sql = templeAiService.generateDeterministicSql("2 murugan temple within 150 km from thanjavur");
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT * FROM temples"));
        assertTrue(sql.contains("murug"));
        assertTrue(sql.contains("ABS(hf_lat - 10.7870) <= 1.5000"));
        assertTrue(sql.contains("ABS(hf_lan - 79.1378) <= 1.5000"));
        assertTrue(sql.contains("LIMIT 2"));
    }

    @Test
    void testGenerateDeterministicSqlForShiva() {
        String sql = templeAiService.generateDeterministicSql("shiva temples in madurai");
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT * FROM temples"));
        assertTrue(sql.contains("siva") || sql.contains("shiva"));
        assertTrue(sql.contains("madurai"));
    }
}
