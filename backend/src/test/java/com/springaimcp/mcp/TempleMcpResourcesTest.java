package com.springaimcp.mcp;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TempleMcpResourcesTest {

    @Test
    void testDescribeResource() {
        TempleMcpResources resources = new TempleMcpResources();
        Map<String, Object> desc = resources.describeResource();

        assertEquals("temple_schema_metadata", desc.get("name"));
        assertEquals("schema/temples", desc.get("path"));
        assertEquals("application/json", desc.get("mimeType"));
        assertTrue(desc.get("content").toString().contains("templeinfo"));
    }
}
