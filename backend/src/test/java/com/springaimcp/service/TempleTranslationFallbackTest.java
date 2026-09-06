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

    @Test
    void testTemple418TranslationIntoAllLanguages() {
        Temple t418 = new Temple(
            418L,
            "sri Veerabadrar temple",
            "Veerabadra Swami - Rajarayudu",
            "Kalyana Veerabadrar",
            "Bhadrakali",
            null, null, null,
            "500 years old",
            null,
            "Rayachoti",
            "Kadapa",
            "Andhra Pradesh",
            null, null, null, null, null, null,
            "It is a wonder in the temple that the rays of Sun fall on Lord Veerabadra for five days in the month of March",
            null, null, null,
            "Daksha organized a yagna deliberately without inviting Lord Shiva to insult him. Angered Shiva sent Veerabadra to destroy the yagna and Daksha.",
            null,
            14.05, 78.75,
            null, null, null, null
        );

        // Tamil
        Temple ta = fallback.translate(t418, "ta");
        assertNotNull(ta);
        assertTrue(ta.name().matches(".*[\\u0B80-\\u0BFF].*"), "Tamil name should be in Tamil: " + ta.name());
        assertFalse(ta.speciality().contains("இட இச அ வொந்டெர"), "Speciality should not be phonetically spelled English: " + ta.speciality());
        assertTrue(ta.speciality().matches(".*[\\u0B80-\\u0BFF].*"), "Speciality should contain Tamil script");

        // Telugu
        Temple te = fallback.translate(t418, "te");
        assertNotNull(te);
        assertTrue(te.name().matches(".*[\\u0C00-\\u0C7F].*"), "Telugu name should be in Telugu: " + te.name());
        assertFalse(te.name().matches(".*[\\u0B80-\\u0BFF].*"), "Telugu name should not contain Tamil characters: " + te.name());
        assertTrue(te.speciality().matches(".*[\\u0C00-\\u0C7F].*"), "Speciality should contain Telugu script");
        assertFalse(te.speciality().contains("ఇట ఇస అ వొన్డెర"), "Telugu speciality should not be phonetically spelled English");

        // Hindi
        Temple hi = fallback.translate(t418, "hi");
        assertNotNull(hi);
        assertTrue(hi.name().matches(".*[\\u0900-\\u097F].*"), "Hindi name should be in Devanagari: " + hi.name());
        assertFalse(hi.name().matches(".*[\\u0B80-\\u0BFF].*"), "Hindi name should not contain Tamil characters: " + hi.name());
        assertTrue(hi.speciality().matches(".*[\\u0900-\\u097F].*"), "Speciality should contain Devanagari script");
        assertFalse(hi.speciality().contains("इट इस अ वोन्डेर"), "Hindi speciality should not be phonetically spelled English");
    }
}
