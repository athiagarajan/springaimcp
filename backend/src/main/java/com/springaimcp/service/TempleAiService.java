package com.springaimcp.service;

import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
public class TempleAiService {

    private final ChatClient chatClient;
    private final TempleRepository templeRepository;

    public TempleAiService(ChatClient.Builder builder, TempleRepository templeRepository) {
        this.chatClient = builder.build();
        this.templeRepository = templeRepository;
    }

    public Mono<Map<String, Object>> executeAiSearch(String prompt) {
        long startTime = System.currentTimeMillis();
        String systemPrompt = """
            You are an expert PostgreSQL SQL query generator for the 'temples' database table.

            Valid Table Columns:
            - id (bigint), name (text), moolavar (text), urchavar (text), amman_thayar (text), thala_virutcham (text), theertham (text), agamam_pooja (text), old_year (text), historical_name (text), city (text), district (text), state (text), singers (text), festival (text), general_information (text), address (text), phone (text), opening_time (text), speciality (text), prayers (text), thanks_giving (text), greatness (text), history (text), features (text), hf_lat (double precision latitude), hf_lan (double precision longitude), location (text), near_by_airport (text), near_by_railway_station (text), accommodation (text)

            CRITICAL RULES:
            1. ALWAYS start your query with 'SELECT * FROM temples'.
            2. Output ONLY raw executable SQL SELECT queries. Do NOT write markdown prose, explanations, or inline comments.
            3. Use ONLY real columns from the schema listed above. Note: hf_lat and hf_lan are ALREADY numeric double precision columns. Do NOT cast or regex hf_lat/hf_lan.
            4. Deity Matching Rules:
               - For 'murugan' / 'muruga' / 'subramaniya' / 'velappar' / 'arumugam' temples, match:
                 (LOWER(moolavar) LIKE '%muruga%' OR LOWER(name) LIKE '%muruga%' OR LOWER(moolavar) LIKE '%subramanya%' OR LOWER(name) LIKE '%subramania%' OR LOWER(moolavar) LIKE '%swaminatha%' OR LOWER(moolavar) LIKE '%velappar%' OR LOWER(moolavar) LIKE '%nayinar%')
            5. Known City Coordinates:
               - 'sivakasi' -> lat: 9.4533, lon: 77.7963
               - 'tanjore' / 'thanjavur' -> lat: 10.7870, lon: 79.1378
               - 'tiruchi' / 'trichy' -> lat: 10.7905, lon: 78.7047
               - 'madurai' -> lat: 9.9252, lon: 78.1198
               - 'chennai' -> lat: 13.0827, lon: 80.2707
            6. Distance Constraints ('within N km', 'in a radius of N km', 'close to'):
               - Extract the specified distance N (e.g. 100 km -> N=100; default N=100).
               - Convert distance to coordinate delta in degrees: delta_deg = N / 111.0 (e.g. 100 km -> 0.90 deg; 50 km -> 0.45 deg).
               - Use coordinate bounding box filter matching target location lat/lon:
                 ABS(hf_lat - target_lat) <= (N / 111.0) AND ABS(hf_lan - target_lon) <= (N / 111.0)
                 (Example for 100 km from sivakasi [9.4533, 77.7963]: ABS(hf_lat - 9.4533) <= 0.90 AND ABS(hf_lan - 77.7963) <= 0.90)
            7. Respect quantity limits specified in prompt (e.g. 'list 3', 'only 2' -> append LIMIT N).
            8. Do NOT use PostGIS functions (ST_DWithin, ST_MakePoint, ST_SetSRID).
            """;

        return Mono.fromCallable(() -> {
            long llmStart = System.currentTimeMillis();
            String rawContent = chatClient.prompt()
                    .system(systemPrompt)
                    .user(prompt)
                    .call()
                    .content();
            long llmEnd = System.currentTimeMillis();
            return Map.of(
                "rawContent", rawContent != null ? rawContent : "",
                "llmTimeMs", (llmEnd - llmStart)
            );
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(llmOutput -> {
            String rawContent = (String) llmOutput.get("rawContent");
            long llmTimeMs = (Long) llmOutput.get("llmTimeMs");

            String cleanSql = sanitizeSql(rawContent);
            List<Temple> results = List.of();
            long dbTimeMs = 0;
            String dbStatus = "Not Executed";

            if (!cleanSql.isBlank() && cleanSql.toLowerCase().startsWith("select")) {
                long dbStart = System.currentTimeMillis();
                try {
                    results = templeRepository.executeDynamicSql(cleanSql);
                    dbStatus = "SUCCESS (200 OK)";
                } catch (Exception e) {
                    dbStatus = "SQL EXECUTION FAILED: " + e.getMessage();
                    System.err.println("Execution exception on LLM SQL: " + e.getMessage());
                }
                dbTimeMs = System.currentTimeMillis() - dbStart;
            }

            long totalTimeMs = System.currentTimeMillis() - startTime;

            return Map.of(
                "rawContent", rawContent,
                "generatedSql", cleanSql.isBlank() ? "-- Query Generation Pending" : cleanSql,
                "temples", results,
                "llmTimeMs", llmTimeMs,
                "dbTimeMs", dbTimeMs,
                "totalTimeMs", totalTimeMs,
                "dbStatus", dbStatus
            );
        })
        .onErrorResume(err -> {
            System.err.println("LLM ChatClient Exception: " + err.getMessage());
            return Mono.just(Map.of(
                "rawContent", "ERROR: " + err.getMessage(),
                "generatedSql", "-- LLM Connection Error: " + err.getMessage(),
                "temples", List.of(),
                "llmTimeMs", 0L,
                "dbTimeMs", 0L,
                "totalTimeMs", System.currentTimeMillis() - startTime,
                "dbStatus", "CONNECTION ERROR: " + err.getMessage()
            ));
        });
    }

    private String sanitizeSql(String raw) {
        if (raw == null || raw.isBlank()) return "";
        // 1. Remove markdown backticks
        String clean = raw.replaceAll("```sql", "").replaceAll("```", "").trim();
        // 2. Strip single-line SQL comments (-- ...)
        StringBuilder sb = new StringBuilder();
        for (String line : clean.split("\n")) {
            int commentIdx = line.indexOf("--");
            if (commentIdx >= 0) {
                line = line.substring(0, commentIdx);
            }
            if (!line.trim().isEmpty()) {
                sb.append(line.trim()).append(" ");
            }
        }
        String sql = sb.toString().trim();
        // 3. Ensure SELECT * FROM temples is used so JDBC RowMapper has all required columns
        if (sql.toLowerCase().startsWith("select ") && !sql.toLowerCase().startsWith("select *")) {
            sql = sql.replaceAll("(?i)^select\\s+.*?\\s+from\\s+temples", "SELECT * FROM temples");
        }
        return sql;
    }

    public Flux<String> streamDynamicQuery(String prompt) {
        return executeAiSearch(prompt)
                .flatMapMany(aiResult -> {
                    String rawContent = (String) aiResult.get("rawContent");
                    String sql = (String) aiResult.get("generatedSql");
                    @SuppressWarnings("unchecked")
                    List<Temple> temples = (List<Temple>) aiResult.get("temples");
                    long llmTimeMs = (Long) aiResult.getOrDefault("llmTimeMs", 0L);
                    long dbTimeMs = (Long) aiResult.getOrDefault("dbTimeMs", 0L);
                    long totalTimeMs = (Long) aiResult.getOrDefault("totalTimeMs", 0L);
                    String dbStatus = (String) aiResult.getOrDefault("dbStatus", "OK");

                    StringBuilder log = new StringBuilder();
                    log.append("🧠 SPRING AI LLM REASONING & SQL ENGINE LOG\n");
                    log.append("═══════════════════════════════════════════════════════════════\n\n");

                    log.append("1️⃣ SYSTEM & INFERENCE ENVIRONMENT\n");
                    log.append("---------------------------------------------------------------\n");
                    log.append("   • LLM Engine       : Ollama (qwen2.5-coder:latest)\n");
                    log.append("   • Endpoint         : http://localhost:11434\n");
                    log.append("   • Database Target  : PostgreSQL ('templeinfo' db, 'temples' table - 31 columns)\n");
                    log.append("   • Input Prompt     : \"").append(prompt).append("\"\n\n");

                    log.append("2️⃣ LLM NL-TO-SQL REASONING & QUERY FORMATION\n");
                    log.append("---------------------------------------------------------------\n");
                    log.append("   • Injected Schema  : id, name, moolavar, city, district, state, address, speciality, hf_lat, hf_lan...\n");
                    log.append("   • Inference Time   : ").append(llmTimeMs).append(" ms\n");
                    log.append("   • Raw Model Output :\n");
                    log.append("     ").append(rawContent != null ? rawContent.replace("\n", "\n     ") : "N/A").append("\n");
                    log.append("   • Sanitization     : Stripped backticks, removed inline comments, enforced SELECT *\n\n");

                    log.append("3️⃣ FINAL GENERATED POSTGRESQL QUERY\n");
                    log.append("---------------------------------------------------------------\n");
                    log.append("   ").append(sql != null && !sql.isBlank() ? sql : "N/A").append("\n\n");

                    log.append("4️⃣ DATABASE EXECUTION METRICS\n");
                    log.append("---------------------------------------------------------------\n");
                    log.append("   • Execution Status : ").append(dbStatus).append("\n");
                    log.append("   • DB Query Time    : ").append(dbTimeMs).append(" ms\n");
                    log.append("   • Total Pipeline   : ").append(totalTimeMs).append(" ms\n");
                    log.append("   • Matching Records : ").append(temples != null ? temples.size() : 0).append("\n\n");

                    log.append("5️⃣ MATCHING TEMPLE DATASETS\n");
                    log.append("---------------------------------------------------------------\n");
                    if (temples != null && !temples.isEmpty()) {
                        for (int i = 0; i < temples.size(); i++) {
                            Temple t = temples.get(i);
                            log.append(String.format("   [%d] %s\n       City/District : %s, %s\n       Moolavar      : %s\n       GPS Coords    : [%s, %s]\n",
                                i + 1,
                                t.name(),
                                t.city() != null ? t.city() : "N/A",
                                t.district() != null ? t.district() : t.state(),
                                t.moolavar() != null ? t.moolavar() : "N/A",
                                t.hfLat() != null ? t.hfLat() : "N/A",
                                t.hfLan() != null ? t.hfLan() : "N/A"
                            ));
                        }
                    } else {
                        log.append("   No matching temple records found.\n");
                    }

                    return Flux.just(log.toString());
                });
    }

    public List<Temple> getAllTemples() {
        return templeRepository.findAll();
    }

    public Mono<List<Temple>> search(String keyword) {
        return executeAiSearch(keyword)
                .map(aiResult -> {
                    @SuppressWarnings("unchecked")
                    List<Temple> temples = (List<Temple>) aiResult.get("temples");
                    return temples;
                });
    }
}
