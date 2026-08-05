package com.springaimcp.mcp;

import com.springaimcp.mcp.annotation.McpTool;
import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TempleMcpTools {

    private final TempleRepository templeRepository;

    public TempleMcpTools(TempleRepository templeRepository) {
        this.templeRepository = templeRepository;
    }

    @McpTool(name = "search_temples", description = "Search temples in the PostgreSQL templeinfo database by state, district, city, deity or keyword")
    public List<Temple> searchTemples(String query) {
        return templeRepository.search(query);
    }

    @McpTool(name = "find_nearby_temples", description = "Find nearby temples given latitude, longitude, and radius in kilometers")
    public List<Temple> findNearbyTemples(double lat, double lon, double radiusKm) {
        return templeRepository.findNearby(lat, lon, radiusKm);
    }

    @McpTool(name = "get_temple_by_id", description = "Get full detailed record for a specific temple by ID")
    public Temple getTempleById(int id) {
        return templeRepository.findById(Long.valueOf(id)).orElse(null);
    }
}
