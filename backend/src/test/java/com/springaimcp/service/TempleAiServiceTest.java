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

    @Test
    void testTranslateTempleEnglishReturnsOriginalImmediately() {
        Temple t = new Temple(1L, "Temple 1", "Moolavar", null, null, null, null, null, null, null, "City", "District", "State", null, null, null, null, null, null, null, null, null, null, null, null, 10.0, 77.0, null, null, null, null);
        when(templeRepository.executeDynamicSql("SELECT * FROM temples WHERE id = 1")).thenReturn(List.of(t));

        Temple result = templeAiService.translateTemple(1L, "en").block();
        assertNotNull(result);
        assertEquals("Temple 1", result.name());
    }

    @Test
    void testTranslateTempleNotFoundReturnsNull() {
        when(templeRepository.executeDynamicSql("SELECT * FROM temples WHERE id = 999")).thenReturn(List.of());

        Temple result = templeAiService.translateTemple(999L, "ta").block();
        assertNull(result);
    }

    @Test
    void testTranslateTempleTamilTranslatesCorrectly() {
        Temple t = new Temple(550L, "sri Anjali Varatha Anjaneyar temple", "Anjali Varatha Anjaneyar", null, null, null, null, null, "500 years old", null, "Chinnalapatti", "Dindigul", "Tamil Nadu", null, null, null, null, null, null, null, null, null, null, null, null, 10.36, 77.97, null, null, null, null);
        when(templeRepository.executeDynamicSql("SELECT * FROM temples WHERE id = 550")).thenReturn(List.of(t));

        Temple result = templeAiService.translateTemple(550L, "ta").block();
        assertNotNull(result);
        assertNotEquals("sri Anjali Varatha Anjaneyar temple", result.name());
        assertTrue(result.name().contains("ஸ்ரீ") || result.name().contains("திருக்கோயில்"));
        assertEquals("சின்னாளப்பட்டி", result.city());
        assertEquals("திண்டுக்கல்", result.district());
        assertEquals("தமிழ்நாடு", result.state());
    }

    @Test
    void testTranslateTempleTeluguTranslatesCorrectly() {
        Temple t = new Temple(550L, "sri Anjali Varatha Anjaneyar temple", "Anjali Varatha Anjaneyar", null, null, null, null, null, "500 years old", null, "Chinnalapatti", "Dindigul", "Tamil Nadu", null, null, null, null, null, null, null, null, null, null, null, null, 10.36, 77.97, null, null, null, null);
        when(templeRepository.executeDynamicSql("SELECT * FROM temples WHERE id = 550")).thenReturn(List.of(t));

        Temple result = templeAiService.translateTemple(550L, "te").block();
        assertNotNull(result);
        assertNotEquals("sri Anjali Varatha Anjaneyar temple", result.name());
        assertTrue(result.name().contains("శ్రీ") || result.name().contains("ఆలయం"));
        assertEquals("చిన్నాలపట్టి", result.city());
    }

    @Test
    void testTranslateTempleHindiTranslatesCorrectly() {
        Temple t = new Temple(550L, "sri Anjali Varatha Anjaneyar temple", "Anjali Varatha Anjaneyar", null, null, null, null, null, "500 years old", null, "Chinnalapatti", "Dindigul", "Tamil Nadu", null, null, null, null, null, null, null, null, null, null, null, null, 10.36, 77.97, null, null, null, null);
        when(templeRepository.executeDynamicSql("SELECT * FROM temples WHERE id = 550")).thenReturn(List.of(t));

        Temple result = templeAiService.translateTemple(550L, "hi").block();
        assertNotNull(result);
        assertNotEquals("sri Anjali Varatha Anjaneyar temple", result.name());
        assertTrue(result.name().contains("श्री") || result.name().contains("मंदिर"));
        assertEquals("चिन्नालपट्टी", result.city());
    }
}
