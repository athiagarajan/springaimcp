package com.springaimcp.mcp;

import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TempleMcpTools {

    private final TempleRepository templeRepository;

    public TempleMcpTools(TempleRepository templeRepository) {
        this.templeRepository = templeRepository;
    }

    @Tool(description = "Search temples in the PostgreSQL templeinfo database by state, district, city, or general keyword")
    public List<Temple> searchTemples(String state, String district, String city, String keyword) {
        return templeRepository.searchByCriteria(state, district, city, keyword);
    }

    @Tool(description = "Find nearby temples given latitude, longitude, and radius in kilometers")
    public List<Temple> findNearbyTemples(double latitude, double longitude, double radiusKm) {
        return templeRepository.findNearby(latitude, longitude, radiusKm);
    }

    @Tool(description = "Get full detailed record for a specific temple by ID")
    public Temple getTempleById(Long id) {
        return templeRepository.findById(id).orElse(null);
    }

    @Tool(description = "Get templeinfo database table schema definitions for dynamic query planning")
    public Map<String, String> getDatabaseSchemaInfo() {
        return Map.of(
            "tableName", "temples",
            "rowCount", "96",
            "columns", "id, name, moolavar, urchavar, amman_thayar, thala_virutcham, theertham, agamam_pooja, old_year, historical_name, city, district, state, singers, festival, general_information, address, phone, opening_time, speciality, prayers, thanks_giving, greatness, history, features, hf_lat, hf_lan, location, near_by_airport, near_by_railway_station, accommodation"
        );
    }
}
