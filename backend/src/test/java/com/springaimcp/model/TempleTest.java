package com.springaimcp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TempleTest {

    @Test
    void testTempleRecordConstructorAndGetters() {
        Temple temple = new Temple(
            1L, "Sri Idumban Temple", "Idumban", "Urchavar", "Amman",
            "Virutcham", "Theertham", "Pooja", "1000 AD", "HistName",
            "Palani", "Dindigul", "Tamil Nadu", "Singers", "Thaipusam",
            "GenInfo", "Palani Hills", "04545-12345", "6AM-8PM",
            "Speciality", "Prayers", "Thanks", "Greatness", "History",
            "Features", 10.4413, 77.5275, "Location", "Coimbatore", "Palani", "Hotels"
        );

        assertEquals(1L, temple.id());
        assertEquals("Sri Idumban Temple", temple.name());
        assertEquals("Idumban", temple.moolavar());
        assertEquals("Urchavar", temple.urchavar());
        assertEquals("Amman", temple.ammanThayar());
        assertEquals("Palani", temple.city());
        assertEquals("Dindigul", temple.district());
        assertEquals("Tamil Nadu", temple.state());
        assertEquals(10.4413, temple.hfLat());
        assertEquals(77.5275, temple.hfLan());
    }
}
