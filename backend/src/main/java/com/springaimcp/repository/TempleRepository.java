package com.springaimcp.repository;

import com.springaimcp.model.Temple;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final List<String> STOP_WORDS = List.of(
        "find", "temple", "temples", "in", "only", "the", "a", "an", "of", "them", "list",
        "show", "me", "near", "at", "best", "by", "for", "with", "is", "are", "what",
        "where", "which", "how", "many", "give", "tell", "get", "search", "locate",
        "display", "top", "good", "famous", "some"
    );

    public List<Temple> findAll() {
        return jdbcTemplate.query("SELECT * FROM temples ORDER BY id ASC", rowMapper);
    }

    public Optional<Temple> findById(Long id) {
        List<Temple> results = jdbcTemplate.query("SELECT * FROM temples WHERE id = ?", rowMapper, id);
        return results.stream().findFirst();
    }

    public List<Temple> search(String query) {
        return searchByCriteria(null, null, null, query);
    }

    public List<Temple> executeDynamicSql(String sql) {
        if (sql == null || sql.isBlank()) {
            return new ArrayList<>();
        }
        String cleanSql = sql.replaceAll("```sql", "").replaceAll("```", "").trim();
        if (!cleanSql.toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Only SELECT queries are permitted.");
        }
        return jdbcTemplate.query(cleanSql, rowMapper);
    }

    public List<Temple> searchByCriteria(String state, String district, String city, String keyword) {
        Integer limit = extractLimitFromPrompt(keyword);

        if (keyword == null || keyword.isBlank()) {
            return executeQuery("SELECT * FROM temples WHERE 1=1 ", new ArrayList<>(), state, district, city, limit);
        }

        String[] tokens = keyword.toLowerCase().split("\\s+");
        List<String> validTokens = new ArrayList<>();
        for (String t : tokens) {
            String clean = t.replaceAll("[^a-z0-9]", "");
            if (!clean.isBlank() && !STOP_WORDS.contains(clean) && !clean.matches("\\d+")) {
                validTokens.add(clean);
            }
        }

        if (validTokens.isEmpty()) {
            return executeQuery("SELECT * FROM temples WHERE (LOWER(name) LIKE LOWER(?) OR LOWER(city) LIKE LOWER(?) OR LOWER(district) LIKE LOWER(?)) ",
                    List.of("%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"), state, district, city, limit);
        }

        // 1st Attempt: Strict AND matching on extracted key domain tokens
        List<Temple> results = executeTokenSearch(validTokens, true, state, district, city, limit);

        // 2nd Attempt: Fall back to flexible OR matching if strict AND yields no matches
        if (results.isEmpty() && validTokens.size() > 1) {
            results = executeTokenSearch(validTokens, false, state, district, city, limit);
        }

        if (limit != null && limit > 0 && results.size() > limit) {
            results = results.subList(0, limit);
        }

        return results;
    }

    private Integer extractLimitFromPrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) return null;
        String lower = prompt.toLowerCase();
        Pattern p = Pattern.compile("(?:only|top|first|limit|show|list|bring)?\\s*(\\d+)\\s*(?:of them|temples|records|items|results|best)?");
        Matcher m = p.matcher(lower);
        while (m.find()) {
            String numStr = m.group(1);
            if (numStr != null) {
                try {
                    int val = Integer.parseInt(numStr);
                    if (val > 0 && val <= 50) {
                        return val;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private List<Temple> executeTokenSearch(List<String> tokens, boolean isAndMatch, String state, String district, String city, Integer limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM temples WHERE 1=1 AND (");
        List<Object> params = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) {
                sql.append(isAndMatch ? " AND " : " OR ");
            }
            sql.append("(LOWER(name) LIKE LOWER(?) OR LOWER(moolavar) LIKE LOWER(?) OR LOWER(city) LIKE LOWER(?) OR LOWER(district) LIKE LOWER(?) OR LOWER(state) LIKE LOWER(?) OR LOWER(speciality) LIKE LOWER(?) OR LOWER(history) LIKE LOWER(?) OR LOWER(general_information) LIKE LOWER(?)) ");
            String kw = "%" + tokens.get(i) + "%";
            for (int k = 0; k < 8; k++) {
                params.add(kw);
            }
        }
        sql.append(") ");

        return executeQuery(sql.toString(), params, state, district, city, limit);
    }

    private List<Temple> executeQuery(String baseSql, List<Object> params, String state, String district, String city, Integer limit) {
        StringBuilder sql = new StringBuilder(baseSql);
        List<Object> allParams = new ArrayList<>(params);

        if (state != null && !state.isBlank()) {
            sql.append("AND LOWER(state) LIKE LOWER(?) ");
            allParams.add("%" + state + "%");
        }
        if (district != null && !district.isBlank()) {
            sql.append("AND LOWER(district) LIKE LOWER(?) ");
            allParams.add("%" + district + "%");
        }
        if (city != null && !city.isBlank()) {
            sql.append("AND LOWER(city) LIKE LOWER(?) ");
            allParams.add("%" + city + "%");
        }

        sql.append("ORDER BY id ASC ");
        if (limit != null && limit > 0) {
            sql.append("LIMIT ").append(limit).append(" ");
        }

        return jdbcTemplate.query(sql.toString(), rowMapper, allParams.toArray());
    }

    public List<Temple> findNearby(double lat, double lng, double radiusKm) {
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
