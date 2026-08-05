package com.springaimcp.mcp;

import com.springaimcp.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@McpResource(name = "temple_schema_metadata", path = "schema/temples", mimeType = "application/json")
public class TempleMcpResources {

    public String getTempleSchemaMetadata() {
        return """
            {
              "database": "templeinfo",
              "table": "temples",
              "total_columns": 31,
              "key_fields": ["id", "name", "city", "district", "state", "moolavar", "festival", "hf_lat", "hf_lan"]
            }
            """;
    }

    public Map<String, Object> describeResource() {
        return Map.of(
            "name", "temple_schema_metadata",
            "path", "schema/temples",
            "mimeType", "application/json",
            "content", getTempleSchemaMetadata()
        );
    }
}
