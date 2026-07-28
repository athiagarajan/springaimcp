package com.springaimcp.repository;

import com.springaimcp.model.Temple;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TempleRepository {

    private final JdbcTemplate jdbcTemplate;

    public TempleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Temple> rowMapper = (rs, rowNum) -> new Temple(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("moolavar"),
        rs.getString("urchavar"),
        rs.getString("amman_thayar"),
        rs.getString("thala_virutcham"),
        rs.getString("theertham"),
        rs.getString("agamam_pooja"),
        rs.getString("old_year"),
        rs.getString("historical_name"),
        rs.getString("city"),
        rs.getString("district"),
        rs.getString("state"),
        rs.getString("singers"),
        rs.getString("festival"),
        rs.getString("general_information"),
        rs.getString("address"),
        rs.getString("phone"),
        rs.getString("opening_time"),
        rs.getString("speciality"),
        rs.getString("prayers"),
        rs.getString("thanks_giving"),
        rs.getString("greatness"),
        rs.getString("history"),
        rs.getString("features"),
        rs.getObject("hf_lat") != null ? rs.getDouble("hf_lat") : null,
        rs.getObject("hf_lan") != null ? rs.getDouble("hf_lan") : null,
        rs.getString("location"),
        rs.getString("near_by_airport"),
        rs.getString("near_by_railway_station"),
        rs.getString("accommodation")
    );

    public List<Temple> findAll() {
        return jdbcTemplate.query("SELECT * FROM temples ORDER BY id ASC", rowMapper);
    }

    public Optional<Temple> findById(Long id) {
        List<Temple> results = jdbcTemplate.query("SELECT * FROM temples WHERE id = ?", rowMapper, id);
        return results.stream().findFirst();
    }

    public List<Temple> searchByCriteria(String state, String district, String city, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT * FROM temples WHERE 1=1 ");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (state != null && !state.isBlank()) {
            sql.append("AND LOWER(state) LIKE LOWER(?) ");
            params.add("%" + state + "%");
        }
        if (district != null && !district.isBlank()) {
            sql.append("AND LOWER(district) LIKE LOWER(?) ");
            params.add("%" + district + "%");
        }
        if (city != null && !city.isBlank()) {
            sql.append("AND LOWER(city) LIKE LOWER(?) ");
            params.add("%" + city + "%");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(name) LIKE LOWER(?) OR LOWER(moolavar) LIKE LOWER(?) OR LOWER(speciality) LIKE LOWER(?)) ");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append("ORDER BY id ASC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public List<Temple> findNearby(double lat, double lng, double radiusKm) {
        // Haversine formula query in SQL
        String sql = """
            SELECT *, (
                6371 * acos(
                    cos(radians(?)) * cos(radians(hf_lat)) *
                    cos(radians(hf_lan) - radians(?)) +
                    sin(radians(?)) * sin(radians(hf_lat))
                )
            ) AS distance
            FROM temples
            WHERE hf_lat IS NOT NULL AND hf_lan IS NOT NULL
            ORDER BY distance ASC
            LIMIT 50
        """;
        List<Temple> allNearby = jdbcTemplate.query(sql, rowMapper, lat, lng, lat);
        return allNearby.stream()
            .filter(t -> t.hfLat() != null && t.hfLan() != null)
            .toList();
    }
}
