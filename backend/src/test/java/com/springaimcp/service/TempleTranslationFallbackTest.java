package com.springaimcp.service;

import com.springaimcp.model.Temple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TempleTranslationFallbackTest {

    private TempleTranslationFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new TempleTranslationFallback();
    }

    @Test
    void testTeluguTranslationHasNoTamilCharacters() {
        Temple t = new Temple(
            100L,
            "sri Arunagirinathar temple",
            "Arunagirinathar",
            null, null, null, null, null,
            "500 years old",
            null,
            "Tiruvannamalai",
            "Tiruvannamalai",
            "Tamil Nadu",
            "Thirugnanasambandar, Appar, Sundarar",
            null,
            "Saint Arunagirinathar sang praises of Lord Muruga",
            "Kodangipatti, Theni",
            null, null, null, null, null, null, null, null,
            12.22, 79.07,
            "Tiruvannamalai", null, null, null
        );

        Temple result = fallback.translate(t, "te");
        assertNotNull(result);

        // Verify Telugu output contains NO Tamil script (\u0B80 to \u0BFF)
        assertFalse(result.name().matches(".*[\\u0B80-\\u0BFF].*"), "Telugu name should not contain Tamil characters: " + result.name());
        assertFalse(result.city().matches(".*[\\u0B80-\\u0BFF].*"), "Telugu city should not contain Tamil characters: " + result.city());
        assertFalse(result.district().matches(".*[\\u0B80-\\u0BFF].*"), "Telugu district should not contain Tamil characters: " + result.district());
        assertFalse(result.singers().matches(".*[\\u0B80-\\u0BFF].*"), "Telugu singers should not contain Tamil characters: " + result.singers());

        // Verify Telugu characters are present (\u0C00 to \u0C7F)
        assertTrue(result.name().matches(".*[\\u0C00-\\u0C7F].*"), "Telugu name must contain Telugu script: " + result.name());
        assertTrue(result.city().matches(".*[\\u0C00-\\u0C7F].*"), "Telugu city must contain Telugu script: " + result.city());
    }

    @Test
    void testHindiTranslationHasNoTamilCharacters() {
        Temple t = new Temple(
            100L,
            "sri Arunagirinathar temple",
            "Arunagirinathar",
            null, null, null, null, null,
            "500 years old",
            null,
            "Tiruvannamalai",
            "Tiruvannamalai",
            "Tamil Nadu",
            "Thirugnanasambandar, Appar, Sundarar",
            null,
            "Saint Arunagirinathar sang praises of Lord Muruga",
            "Kodangipatti, Theni",
            null, null, null, null, null, null, null, null,
            12.22, 79.07,
            "Tiruvannamalai", null, null, null
        );

        Temple result = fallback.translate(t, "hi");
        assertNotNull(result);

        // Verify Hindi output contains NO Tamil script (\u0B80 to \u0BFF)
        assertFalse(result.name().matches(".*[\\u0B80-\\u0BFF].*"), "Hindi name should not contain Tamil characters: " + result.name());
        assertFalse(result.city().matches(".*[\\u0B80-\\u0BFF].*"), "Hindi city should not contain Tamil characters: " + result.city());
        assertFalse(result.district().matches(".*[\\u0B80-\\u0BFF].*"), "Hindi district should not contain Tamil characters: " + result.district());
        assertFalse(result.singers().matches(".*[\\u0B80-\\u0BFF].*"), "Hindi singers should not contain Tamil characters: " + result.singers());

        // Verify Devanagari characters are present (\u0900 to \u097F)
        assertTrue(result.name().matches(".*[\\u0900-\\u097F].*"), "Hindi name must contain Devanagari script: " + result.name());
        assertTrue(result.city().matches(".*[\\u0900-\\u097F].*"), "Hindi city must contain Devanagari script: " + result.city());
    }

    @Test
    void testTamilTranslationContainsAuthenticTamilScript() {
        Temple t = new Temple(
            550L,
            "sri Anjali Varatha Anjaneyar temple",
            "Anjali Varatha Anjaneyar",
            null, null, null, null, null,
            "500 years old",
            null,
            "Chinnalapatti",
            "Dindigul",
            "Tamil Nadu",
            null, null, null, null, null, null, null, null, null, null, null, null,
            10.36, 77.97,
            null, null, null, null
        );

        Temple result = fallback.translate(t, "ta");
        assertNotNull(result);

        // Verify Tamil output contains Tamil characters (\u0B80 to \u0BFF)
        assertTrue(result.name().matches(".*[\\u0B80-\\u0BFF].*"), "Tamil name must contain Tamil script: " + result.name());
        assertEquals("சின்னாளப்பட்டி", result.city());
        assertEquals("திண்டுக்கல்", result.district());
        assertEquals("தமிழ்நாடு", result.state());
    }
}
