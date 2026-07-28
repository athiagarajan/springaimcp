package com.springaimcp.mcp;

import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class TempleMcpToolsTest {

    private TempleRepository templeRepository;
    private TempleMcpTools mcpTools;

    @BeforeEach
    void setUp() {
        templeRepository = Mockito.mock(TempleRepository.class);
        mcpTools = new TempleMcpTools(templeRepository);
    }

    @Test
    void testSearchTemples() {
        Temple sample = new Temple(1L, "Sample Temple", "Deity", null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, 10.0, 77.0, null, null, null, null);
        when(templeRepository.searchByCriteria(anyString(), anyString(), anyString(), anyString())).thenReturn(List.of(sample));

        List<Temple> result = mcpTools.searchTemples("State", "District", "City", "Keyword");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sample Temple", result.get(0).name());
    }

    @Test
    void testFindNearbyTemples() {
        Temple sample = new Temple(1L, "Nearby Temple", "Deity", null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, 10.44, 77.52, null, null, null, null);
        when(templeRepository.findNearby(anyDouble(), anyDouble(), anyDouble())).thenReturn(List.of(sample));

        List<Temple> result = mcpTools.findNearbyTemples(10.44, 77.52, 10.0);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetTempleByIdFoundAndNotFound() {
        Temple sample = new Temple(494L, "sri Idumban temple", "Idumban", null, null, null, null, null, null, null, "Palani", "Dindigul", "Tamil Nadu", null, null, null, null, null, null, null, null, null, null, null, null, 10.44, 77.52, null, null, null, null);
        when(templeRepository.findById(494L)).thenReturn(Optional.of(sample));
        when(templeRepository.findById(999L)).thenReturn(Optional.empty());

        Temple found = mcpTools.getTempleById(494L);
        assertNotNull(found);
        assertEquals("sri Idumban temple", found.name());

        Temple notFound = mcpTools.getTempleById(999L);
        assertNull(notFound);
    }

    @Test
    void testGetDatabaseSchemaInfo() {
        Map<String, String> schema = mcpTools.getDatabaseSchemaInfo();
        assertNotNull(schema);
        assertEquals("temples", schema.get("tableName"));
        assertEquals("96", schema.get("rowCount"));
        assertTrue(schema.get("columns").contains("moolavar"));
    }
}
