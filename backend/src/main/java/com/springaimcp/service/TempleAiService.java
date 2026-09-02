package com.springaimcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
public class TempleAiService {

    private final TempleRepository templeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${temple.translation.gemini-api-key:}")
    private String geminiApiKey;

    public TempleAiService(TempleRepository templeRepository) {
        this.templeRepository = templeRepository;
    }

    public List<Temple> getAllTemples() {
        return templeRepository.executeDynamicSql("SELECT * FROM temples ORDER BY id ASC");
    }

    public Mono<List<Temple>> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Mono.just(getAllTemples());
        }

        return executeAiSearch(keyword)
                .map(resultMap -> {
                    @SuppressWarnings("unchecked")
                    List<Temple> temples = (List<Temple>) resultMap.getOrDefault("temples", List.of());
                    return temples;
                });
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
            3. Use ONLY real columns from the schema listed above. Note: hf_lat and hf_lan are double precision columns.
            4. Deity Matching Rules:
               - For 'murugan' / 'muruga' / 'subramaniya' / 'swaminatha' / 'velappar' temples, match:
                 (LOWER(moolavar) LIKE '%murug%' OR LOWER(moolavar) LIKE '%subramany%' OR LOWER(moolavar) LIKE '%swaminath%' OR LOWER(name) LIKE '%murug%' OR LOWER(name) LIKE '%swaminath%')
            5. Known City Coordinates:
               - 'sivakasi' -> lat: 9.4533, lon: 77.7963
               - 'tanjore' / 'thanjavur' -> lat: 10.7870, lon: 79.1378
               - 'tiruchi' / 'trichy' -> lat: 10.7905, lon: 78.7047
               - 'madurai' -> lat: 9.9252, lon: 78.1198
               - 'chennai' -> lat: 13.0827, lon: 80.2707
            6. Distance Constraints ('within N km', 'near', 'close to'):
               - Convert distance N km to coordinate delta degrees: delta_deg = N / 100.0 (e.g. 100 km -> 1.0 deg; 50 km -> 0.5 deg).
               - Use bounding box filter:
                 ABS(hf_lat - target_lat) <= (N / 100.0) AND ABS(hf_lan - target_lon) <= (N / 100.0)
                 (Example for 100 km from tanjore [10.7870, 79.1378]: ABS(hf_lat - 10.7870) <= 1.0 AND ABS(hf_lan - 79.1378) <= 1.0)
            7. Respect quantity limits specified in prompt (e.g. 'at best 2', 'only 2' -> append LIMIT 2).
            8. Do NOT use PostGIS functions (ST_DWithin, ST_MakePoint).
            """;

        return Mono.fromCallable(() -> {
            long llmStart = System.currentTimeMillis();
            String fullPrompt = systemPrompt + "\nUser Prompt: " + prompt;
            String rawContent = callGeminiApi(fullPrompt, false);
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
            System.err.println("Gemini Search Exception: " + err.getMessage());
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
        String clean = raw.replaceAll("```sql", "").replaceAll("```", "").trim();
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
        String res = sb.toString().trim();

        // Automatic SQL Syntax Repair
        long openParens = res.chars().filter(ch -> ch == '(').count();
        long closeParens = res.chars().filter(ch -> ch == ')').count();
        while (closeParens < openParens) {
            res += ")";
            closeParens++;
        }

        String sql = res;
        if (sql.toLowerCase().startsWith("select ") && !sql.toLowerCase().startsWith("select *")) {
            sql = sql.replaceAll("(?i)^select\\s+.*?\\s+from\\s+temples", "SELECT * FROM temples");
        }
        return sql;
    }

    private final Map<String, Temple> translationCache = new java.util.concurrent.ConcurrentHashMap<>();

    public Mono<Temple> translateTemple(Long id, String targetLang) {
        return Mono.fromCallable(() -> {
            String lang = (targetLang != null && !targetLang.isBlank()) ? targetLang.toLowerCase().trim() : "en";
            String sql = "SELECT * FROM temples WHERE id = " + id;
            List<Temple> found = templeRepository.executeDynamicSql(sql);
            if (found.isEmpty()) {
                return null;
            }
            Temple original = found.get(0);
            if ("en".equals(lang)) {
                return original;
            }

            String cacheKey = id + "_" + lang;
            if (translationCache.containsKey(cacheKey)) {
                return translationCache.get(cacheKey);
            }

            Temple translated = translateWithGemini(original, lang, geminiApiKey);
            boolean isValidTranslation = translated != null 
                && !translated.name().equalsIgnoreCase(original.name());
            if (isValidTranslation) {
                translationCache.put(cacheKey, translated);
            }
            return (translated != null) ? translated : original;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    private Temple translateWithGemini(Temple original, String targetLang, String apiKey) {
        try {
            String langName;
            String scriptName;
            String scriptExample;
            switch (targetLang.toLowerCase()) {
                case "te" -> {
                    langName = "Telugu";
                    scriptName = "Telugu script (తెలుగు లిపి)";
                    scriptExample = "Transliterate all names and codes into Telugu characters (e.g. 'పి.ఎల్.ఎన్.ఐ' for PLNI, 'మధురై' for Madurai, 'ఇడుంబన్' for Idumban). Never include Tamil or Latin letters.";
                }
                case "hi" -> {
                    langName = "Hindi";
                    scriptName = "Devanagari script (हिन्दी / देवनागरी लिपि)";
                    scriptExample = "Transliterate all names and codes into Devanagari characters (e.g. 'पी.एल.एन.आई' for PLNI, 'मदुरै' for Madurai). Never leave Latin letters.";
                }
                case "ta" -> {
                    langName = "Tamil";
                    scriptName = "Tamil script (தமிழ் எழுத்துக்கள்)";
                    scriptExample = "Transliterate all names and codes into Tamil characters (e.g. 'பி.எல்.என்.ஐ' for PLNI, 'மதுரை' for Madurai). Never leave Latin letters.";
                }
                default -> {
                    langName = "Tamil";
                    scriptName = "Tamil script (தமிழ் எழுத்துக்கள்)";
                    scriptExample = "Transliterate all names and codes into Tamil characters.";
                }
            }

            String prompt = String.format("""
                You are an expert English to %s translator specializing in Indian temples, Hindu traditions, and cultural heritage.
                Translate ALL the following temple details into fluent, authentic %s.

                STRICT TRANSLATION MANDATES:
                1. 100%% %s ONLY: Every single field must be translated or transliterated entirely into %s.
                2. ZERO ENGLISH RESIDUE: Do NOT leave ANY English words, phrases, timings, codes, or Latin characters in ANY value. %s
                3. SCRIPT PURITY: Every character must be strictly in %s. Do NOT mix characters from any other language or script.
                4. FULL COMPLETENESS: Every single requested key must be present with complete translated text in the returned JSON.

                Temple details to translate:
                Name: %s
                Historical Name: %s
                City: %s
                District: %s
                State: %s
                Moolavar: %s
                Urchavar: %s
                Amman / Thayar: %s
                Thala Virutcham: %s
                Theertham: %s
                Singers: %s
                Old Year: %s
                Agamam / Pooja: %s
                Speciality: %s
                History: %s
                General Information: %s
                Address: %s
                Opening Time: %s
                Festival: %s
                Nearest Railway Station: %s
                Nearest Airport: %s
                Accommodation: %s
                Prayers: %s
                Thanks Giving: %s
                Greatness: %s
                Features: %s

                CRITICAL INSTRUCTIONS:
                Return ONLY a valid JSON object with the exact keys:
                "name", "historicalName", "city", "district", "state", "moolavar", "urchavar", "ammanThayar",
                "thalaVirutcham", "theertham", "singers", "oldYear", "agamamPooja", "speciality", "history",
                "generalInformation", "address", "openingTime", "festival", "nearByRailwayStation", "nearByAirport",
                "accommodation", "prayers", "thanksGiving", "greatness", "features"
                """,
                langName,
                scriptName,
                langName,
                scriptName,
                scriptExample,
                scriptName,
                escapeJson(original.name(), 0),
                escapeJson(original.historicalName(), 0),
                escapeJson(original.city(), 0),
                escapeJson(original.district(), 0),
                escapeJson(original.state(), 0),
                escapeJson(original.moolavar(), 0),
                escapeJson(original.urchavar(), 0),
                escapeJson(original.ammanThayar(), 0),
                escapeJson(original.thalaVirutcham(), 0),
                escapeJson(original.theertham(), 0),
                escapeJson(original.singers(), 0),
                escapeJson(original.oldYear(), 0),
                escapeJson(original.agamamPooja(), 0),
                escapeJson(original.speciality(), 0),
                escapeJson(original.history(), 0),
                escapeJson(original.generalInformation(), 0),
                escapeJson(original.address(), 0),
                escapeJson(original.openingTime(), 0),
                escapeJson(original.festival(), 0),
                escapeJson(original.nearByRailwayStation(), 0),
                escapeJson(original.nearByAirport(), 0),
                escapeJson(original.accommodation(), 0),
                escapeJson(original.prayers(), 0),
                escapeJson(original.thanksGiving(), 0),
                escapeJson(original.greatness(), 0),
                escapeJson(original.features(), 0)
            );

            String jsonText = callGeminiApi(prompt, true);
            return parseTranslatedTemple(original, jsonText);
        } catch (Exception e) {
            System.err.println("Gemini Flash translation failed for " + targetLang + ": " + e.getMessage());
            return original;
        }
    }

    private String callGeminiApi(String promptText, boolean jsonMode) {
        Map<String, Object> part = Map.of("text", promptText);
        Map<String, Object> contentMap = Map.of("parts", List.of(part));
        Map<String, Object> genConfig = jsonMode
                ? Map.of("temperature", 0.1, "maxOutputTokens", 8192, "responseMimeType", "application/json")
                : Map.of("temperature", 0.1, "maxOutputTokens", 4096);

        Map<String, Object> reqBody = Map.of("contents", List.of(contentMap), "generationConfig", genConfig);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent?key=" + geminiApiKey;
        org.springframework.web.client.RestClient restClient = org.springframework.web.client.RestClient.create();

        String responseStr = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                responseStr = restClient.post()
                        .uri(url)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(reqBody)
                        .retrieve()
                        .body(String.class);
                if (responseStr != null && !responseStr.isBlank()) {
                    break;
                }
            } catch (Exception ex) {
                lastException = ex;
                System.err.println("Gemini Flash attempt " + attempt + " failed: " + ex.getMessage());
                if (attempt < 3) {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }
        }

        if (responseStr == null || responseStr.isBlank()) {
            throw new RuntimeException("Gemini Flash API call failed after 3 attempts: " + (lastException != null ? lastException.getMessage() : "empty response"));
        }

        try {
            JsonNode respNode = objectMapper.readTree(responseStr);
            return respNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini API JSON response: " + e.getMessage(), e);
        }
    }

    private String escapeJson(String val, int maxLen) {
        if (val == null) return "";
        String clean = val.replace("\"", "\\\"").replace("\n", " ");
        if (maxLen > 0 && clean.length() > maxLen) {
            return clean.substring(0, maxLen) + "...";
        }
        return clean;
    }

    private Temple parseTranslatedTemple(Temple original, String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return original;
        }
        try {
            String cleanJson = rawJson.replaceAll("```json", "").replaceAll("```", "").trim();
            int firstBrace = cleanJson.indexOf('{');
            if (firstBrace != -1) {
                cleanJson = cleanJson.substring(firstBrace);
            }
            if (!cleanJson.endsWith("}")) {
                cleanJson = cleanJson + "\"}";
            }
            JsonNode root = objectMapper.readTree(cleanJson);

            return new Temple(
                original.id(),
                getTextOrDefault(root, "name", original.name()),
                getTextOrDefault(root, "moolavar", original.moolavar()),
                getTextOrDefault(root, "urchavar", original.urchavar()),
                getTextOrDefault(root, "ammanThayar", original.ammanThayar()),
                getTextOrDefault(root, "thalaVirutcham", original.thalaVirutcham()),
                getTextOrDefault(root, "theertham", original.theertham()),
                getTextOrDefault(root, "agamamPooja", original.agamamPooja()),
                getTextOrDefault(root, "oldYear", original.oldYear()),
                getTextOrDefault(root, "historicalName", original.historicalName()),
                getTextOrDefault(root, "city", original.city()),
                getTextOrDefault(root, "district", original.district()),
                getTextOrDefault(root, "state", original.state()),
                getTextOrDefault(root, "singers", original.singers()),
                getTextOrDefault(root, "festival", original.festival()),
                getTextOrDefault(root, "generalInformation", original.generalInformation()),
                getTextOrDefault(root, "address", original.address()),
                original.phone(),
                getTextOrDefault(root, "openingTime", original.openingTime()),
                getTextOrDefault(root, "speciality", original.speciality()),
                getTextOrDefault(root, "prayers", original.prayers()),
                getTextOrDefault(root, "thanksGiving", original.thanksGiving()),
                getTextOrDefault(root, "greatness", original.greatness()),
                getTextOrDefault(root, "history", original.history()),
                getTextOrDefault(root, "features", original.features()),
                original.hfLat(),
                original.hfLan(),
                getTextOrDefault(root, "location", original.location()),
                getTextOrDefault(root, "nearByAirport", original.nearByAirport()),
                getTextOrDefault(root, "nearByRailwayStation", original.nearByRailwayStation()),
                getTextOrDefault(root, "accommodation", original.accommodation())
            );
        } catch (Exception e) {
            System.err.println("Failed to parse translated JSON: " + e.getMessage() + "\nRaw content: " + rawJson);
            return original;
        }
    }

    private String getTextOrDefault(JsonNode node, String fieldName, String defaultValue) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull() && !node.get(fieldName).asText().isBlank()) {
            return node.get(fieldName).asText();
        }
        return defaultValue;
    }

    public Flux<String> streamDynamicQuery(String prompt) {
        return executeAiSearch(prompt)
                .flatMapMany(map -> {
                    StringBuilder log = new StringBuilder();
                    String rawContent = (String) map.get("rawContent");
                    String sql = (String) map.get("generatedSql");
                    String dbStatus = (String) map.get("dbStatus");
                    Long llmTimeMs = (Long) map.get("llmTimeMs");
                    Long dbTimeMs = (Long) map.get("dbTimeMs");
                    Long totalTimeMs = (Long) map.get("totalTimeMs");
                    @SuppressWarnings("unchecked")
                    List<Temple> temples = (List<Temple>) map.get("temples");

                    log.append("1️⃣ INITIATING SPRING AI NATURAL LANGUAGE SEARCH PIPELINE\n");
                    log.append("---------------------------------------------------------------\n");
                    log.append("   • Provider         : Google Gemini Flash Cloud API\n");
                    log.append("   • Database Target  : PostgreSQL ('templeinfo' db, 'temples' table)\n");
                    log.append("   • Input Prompt     : \"").append(prompt).append("\"\n\n");

                    log.append("2️⃣ LLM NL-TO-SQL REASONING & QUERY FORMATION\n");
                    log.append("---------------------------------------------------------------\n");
                    log.append("   • Inference Time   : ").append(llmTimeMs != null ? llmTimeMs : 0).append(" ms\n");
                    log.append("   • Raw Model Output :\n");
                    log.append("     ").append(rawContent != null ? rawContent.replace("\n", "\n     ") : "N/A").append("\n\n");

                    log.append("3️⃣ FINAL GENERATED POSTGRESQL QUERY\n");
                    log.append("---------------------------------------------------------------\n");
                    log.append("   ").append(sql != null && !sql.isBlank() ? sql : "N/A").append("\n\n");

                    log.append("4️⃣ DATABASE EXECUTION METRICS\n");
                    log.append("---------------------------------------------------------------\n");
                    log.append("   • Execution Status : ").append(dbStatus).append("\n");
                    log.append("   • DB Query Time    : ").append(dbTimeMs != null ? dbTimeMs : 0).append(" ms\n");
                    log.append("   • Total Pipeline   : ").append(totalTimeMs != null ? totalTimeMs : 0).append(" ms\n");
                    log.append("   • Matching Records : ").append(temples != null ? temples.size() : 0).append("\n\n");

                    log.append("5️⃣ MATCHING TEMPLE SUMMARY\n");
                    log.append("---------------------------------------------------------------\n");
                    if (temples != null && !temples.isEmpty()) {
                        for (int i = 0; i < temples.size(); i++) {
                            Temple t = temples.get(i);
                            log.append(String.format("   [%d] %s (%s, %s)\n",
                                i + 1,
                                t.name(),
                                t.city() != null ? t.city() : "N/A",
                                t.district() != null ? t.district() : (t.state() != null ? t.state() : "N/A")
                            ));
                        }
                    } else {
                        log.append("   No matching temple records found.\n");
                    }

                    return Flux.just(log.toString());
                });
    }
}
