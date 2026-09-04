package com.springaimcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempleAiService {

    private static final Logger log = LoggerFactory.getLogger(TempleAiService.class);

    private final TempleRepository templeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${temple.translation.gemini-api-key:}")
    private String geminiApiKey;

    private static final List<String> GEMINI_MODELS = List.of(
            "gemini-flash-latest",
            "gemini-3.1-flash-lite",
            "gemini-flash-lite-latest"
    );

    private static final Map<String, double[]> KNOWN_COORDINATES = Map.ofEntries(
            Map.entry("thanjavur", new double[]{10.7870, 79.1378}),
            Map.entry("tanjore", new double[]{10.7870, 79.1378}),
            Map.entry("madurai", new double[]{9.9252, 78.1198}),
            Map.entry("chennai", new double[]{13.0827, 80.2707}),
            Map.entry("tiruchi", new double[]{10.7905, 78.7047}),
            Map.entry("trichy", new double[]{10.7905, 78.7047}),
            Map.entry("sivakasi", new double[]{9.4533, 77.7963}),
            Map.entry("kanchipuram", new double[]{12.8342, 79.7036}),
            Map.entry("coimbatore", new double[]{11.0168, 76.9558}),
            Map.entry("palani", new double[]{10.4500, 77.5200}),
            Map.entry("chidambaram", new double[]{11.3994, 79.6934}),
            Map.entry("kumbakonam", new double[]{10.9601, 79.3845}),
            Map.entry("rameswaram", new double[]{9.2876, 79.3129}),
            Map.entry("tirunelveli", new double[]{8.7139, 77.7567}),
            Map.entry("tiruvarur", new double[]{10.7725, 79.6365}),
            Map.entry("nagapattinam", new double[]{10.7656, 79.8424})
    );

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
                    log.error("Execution exception on LLM SQL: {}", e.getMessage());
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
            log.warn("Gemini Search Exception: {}. Seamlessly executing deterministic spatial/deity engine.", err.getMessage());
            long fallbackStart = System.currentTimeMillis();
            String fallbackSql = generateDeterministicSql(prompt);
            List<Temple> fallbackResults = List.of();
            String dbStatus = "Not Executed";
            try {
                fallbackResults = templeRepository.executeDynamicSql(fallbackSql);
                dbStatus = "FALLBACK ENGINE SUCCESS (200 OK)";
            } catch (Exception e) {
                dbStatus = "FALLBACK SQL FAILED: " + e.getMessage();
                log.error("Fallback SQL execution error: {}", e.getMessage());
            }
            long dbTimeMs = System.currentTimeMillis() - fallbackStart;
            long totalTimeMs = System.currentTimeMillis() - startTime;

            return Mono.just(Map.of(
                "rawContent", "⚡ NOTICE: Google Gemini Cloud API experienced high network latency/timeout (" + err.getMessage() + ").\n"
                            + "Seamlessly activated high-precision deterministic spatial/deity SQL query engine.",
                "generatedSql", fallbackSql,
                "temples", fallbackResults,
                "llmTimeMs", 0L,
                "dbTimeMs", dbTimeMs,
                "totalTimeMs", totalTimeMs,
                "dbStatus", dbStatus
            ));
        });
    }

    public String generateDeterministicSql(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "SELECT * FROM temples ORDER BY id ASC";
        }
        String p = prompt.toLowerCase();
        List<String> conditions = new ArrayList<>();

        // 1. Quantity limit extraction
        Integer limit = null;
        java.util.regex.Matcher limitMatcher = java.util.regex.Pattern.compile("\\b(?:at best|only|top|limit|give me|first)?\\s*(\\d+)\\b").matcher(p);
        if (limitMatcher.find()) {
            try {
                limit = Integer.parseInt(limitMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        // 2. Deity matching
        if (p.contains("murugan") || p.contains("muruga") || p.contains("subramanya") || p.contains("swaminatha") || p.contains("velappar") || p.contains("karthik")) {
            conditions.add("(LOWER(moolavar) LIKE '%murug%' OR LOWER(name) LIKE '%murug%' OR LOWER(moolavar) LIKE '%subramany%' OR LOWER(name) LIKE '%subramany%')");
        } else if (p.contains("shiva") || p.contains("siva") || p.contains("nataraja") || p.contains("ekambareswarar") || p.contains("lingam")) {
            conditions.add("(LOWER(moolavar) LIKE '%shiva%' OR LOWER(moolavar) LIKE '%siva%' OR LOWER(moolavar) LIKE '%lingam%' OR LOWER(name) LIKE '%shiva%' OR LOWER(name) LIKE '%siva%')");
        } else if (p.contains("vishnu") || p.contains("perumal") || p.contains("ranganath") || p.contains("venkateswara") || p.contains("krishna") || p.contains("rama")) {
            conditions.add("(LOWER(moolavar) LIKE '%perumal%' OR LOWER(moolavar) LIKE '%vishnu%' OR LOWER(moolavar) LIKE '%ranganath%' OR LOWER(name) LIKE '%perumal%')");
        } else if (p.contains("anjaneyar") || p.contains("hanuman") || p.contains("maruti")) {
            conditions.add("(LOWER(moolavar) LIKE '%anjaneyar%' OR LOWER(moolavar) LIKE '%hanuman%' OR LOWER(name) LIKE '%anjaneyar%')");
        }

        // 3. Distance & City matching
        java.util.regex.Matcher distMatcher = java.util.regex.Pattern.compile("(?:within|near|close to|around)\\s+(\\d+)\\s*(?:km|kms)?(?:\\s+from|\\s+of)?\\s+([a-zA-Z]+)").matcher(p);
        if (distMatcher.find()) {
            double km = Double.parseDouble(distMatcher.group(1));
            String cityName = distMatcher.group(2).toLowerCase();
            double deltaDeg = km / 100.0;
            double[] coords = KNOWN_COORDINATES.get(cityName);
            if (coords == null) {
                for (Map.Entry<String, double[]> entry : KNOWN_COORDINATES.entrySet()) {
                    if (entry.getKey().contains(cityName) || cityName.contains(entry.getKey())) {
                        coords = entry.getValue();
                        break;
                    }
                }
            }
            if (coords != null) {
                conditions.add(String.format(Locale.US, "ABS(hf_lat - %.4f) <= %.4f AND ABS(hf_lan - %.4f) <= %.4f", coords[0], deltaDeg, coords[1], deltaDeg));
            }
        } else {
            for (String city : KNOWN_COORDINATES.keySet()) {
                if (p.contains(city)) {
                    conditions.add(String.format("(LOWER(city) LIKE '%%%s%%' OR LOWER(district) LIKE '%%%s%%')", city, city));
                    break;
                }
            }
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM temples");
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        sql.append(" ORDER BY id ASC");
        if (limit != null && limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }
        sql.append(";");
        return sql.toString();
    }

    private String sanitizeSql(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String clean = raw.replaceAll("```sql", "").replaceAll("```", "").trim();
        StringBuilder sb = new StringBuilder();
        for (String line : clean.split("\\n")) {
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

    private final Map<String, Temple> translationCache = new ConcurrentHashMap<>();

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
                Temple cached = translationCache.get(cacheKey);
                if (!hasEnglishResidue(cached)) {
                    return cached;
                }
            }

            Temple translated = translateWithGemini(original, lang, geminiApiKey);
            boolean isValidTranslation = translated != null 
                && !translated.name().equalsIgnoreCase(original.name())
                && !hasEnglishResidue(translated);
            if (isValidTranslation) {
                translationCache.put(cacheKey, translated);
            }
            return (translated != null) ? translated : original;
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    private boolean hasEnglishResidue(Temple temple) {
        if (temple == null) return true;
        if (temple.location() != null && temple.location().matches(".*[a-zA-Z]{4,}.*")) return true;
        if (temple.address() != null && temple.address().matches(".*[a-zA-Z]{4,}.*")) return true;
        return false;
    }

    private Temple translateWithGemini(Temple original, String targetLang, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return original;
        }

        String langName = switch (targetLang) {
            case "ta" -> "Tamil";
            case "te" -> "Telugu";
            case "hi" -> "Hindi";
            default -> "Tamil";
        };

        String scriptName = switch (targetLang) {
            case "ta" -> "Tamil script";
            case "te" -> "Telugu script";
            case "hi" -> "Devanagari script";
            default -> "Tamil script";
        };

        String deityInst = switch (targetLang) {
            case "ta" -> "Ensure authentic Tamil Saivite/Vaishnavite terminology (e.g., 'சிவன்', 'பெருமாள்', 'முருகன்', 'அம்மன்', 'விநாயகர்', 'தீர்த்தம்', 'தல விருட்சம்', 'உற்சவர்').";
            case "te" -> "Ensure authentic Telugu temple terms (e.g., 'శివుడు', 'పెరుమాళ్', 'స్వామి', 'తీర్థం', 'మూలవర్').";
            case "hi" -> "Ensure authentic Hindi devotional terms (e.g., 'शिव', 'विष्णु', 'तीर्थ', 'मूलवर').";
            default -> "";
        };

        String extraInst = switch (targetLang) {
            case "ta" -> "Use pure Tamil script without transliterated English characters.";
            case "te" -> "Use authentic Telugu script without English words.";
            case "hi" -> "Use authentic Devanagari script without English words.";
            default -> "";
        };

        try {
            String prompt = String.format(
                """
                You are a sacred temple scholar and master translator. Translate the following South Indian temple details into %s (%s).

                MANDATORY TRANSLATION RULES:
                1. ZERO ENGLISH CHARACTERS:
                   Not a single Latin/English letter (A-Z, a-z) is allowed anywhere in the output JSON values.
                2. TRANSLATE ALL PROPER NOUNS & LABELS:
                   Translate all temple names, deity names, city, district, state names into %s.
                3. DEITY ACCURACY:
                   %s
                   %s
                4. SCRIPT PURITY:
                   Every single character in all values must be in %s.
                5. FULL FIELD COVERAGE:
                   You MUST translate every field: "name", "historicalName", "city", "district", "state", "moolavar", "urchavar", "ammanThayar",
                   "thalaVirutcham", "theertham", "singers", "oldYear", "agamamPooja", "speciality", "history", "generalInformation", "address",
                   "location", "openingTime", "festival", "nearByRailwayStation", "nearByAirport", "accommodation", "prayers", "thanksGiving",
                   "greatness", "features".

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
                Location: %s
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
                "generalInformation", "address", "location", "openingTime", "festival", "nearByRailwayStation", "nearByAirport",
                "accommodation", "prayers", "thanksGiving", "greatness", "features"
                """,
                langName,
                scriptName,
                deityInst,
                extraInst,
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
                escapeJson(original.location(), 0),
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
            log.error("Gemini Flash translation failed for {}: {}", targetLang, e.getMessage());
            return original;
        }
    }

    private String callGeminiApi(String promptText, boolean jsonMode) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is not configured");
        }

        Map<String, Object> part = Map.of("text", promptText);
        Map<String, Object> contentMap = Map.of("parts", List.of(part));
        Map<String, Object> genConfig = jsonMode
                ? Map.of("temperature", 0.1, "maxOutputTokens", 8192, "responseMimeType", "application/json")
                : Map.of("temperature", 0.1, "maxOutputTokens", 4096);

        Map<String, Object> reqBody = Map.of("contents", List.of(contentMap), "generationConfig", genConfig);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(4));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));

        RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();

        String responseStr = null;
        Exception lastException = null;

        for (String model : GEMINI_MODELS) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + geminiApiKey;
            for (int attempt = 1; attempt <= 2; attempt++) {
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
                    log.warn("Gemini attempt {} with {} failed: {}", attempt, model, ex.getMessage());
                    if (attempt < 2) {
                        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    }
                }
            }
            if (responseStr != null && !responseStr.isBlank()) {
                break;
            }
        }

        if (responseStr == null || responseStr.isBlank()) {
            throw new RuntimeException("Gemini API call failed across all models: " + (lastException != null ? lastException.getMessage() : "empty response"));
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
            String cleanJson = rawJson.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            int firstBrace = cleanJson.indexOf("{");
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
            log.error("Failed to parse translated JSON: {}\nRaw content: {}", e.getMessage(), rawJson);
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
                    StringBuilder logBuilder = new StringBuilder();
                    String rawContent = (String) map.get("rawContent");
                    String sql = (String) map.get("generatedSql");
                    String dbStatus = (String) map.get("dbStatus");
                    Long llmTimeMs = (Long) map.get("llmTimeMs");
                    Long dbTimeMs = (Long) map.get("dbTimeMs");
                    Long totalTimeMs = (Long) map.get("totalTimeMs");
                    @SuppressWarnings("unchecked")
                    List<Temple> temples = (List<Temple>) map.get("temples");

                    logBuilder.append("1️⃣ INITIATING SPRING AI NATURAL LANGUAGE SEARCH PIPELINE\n");
                    logBuilder.append("---------------------------------------------------------------\n");
                    logBuilder.append("   • Provider         : Google Gemini Flash Cloud API\n");
                    logBuilder.append("   • Database Target  : PostgreSQL ('templeinfo' db, 'temples' table)\n");
                    logBuilder.append("   • Input Prompt     : \"").append(prompt).append("\"\n\n");

                    logBuilder.append("2️⃣ LLM NL-TO-SQL REASONING & QUERY FORMATION\n");
                    logBuilder.append("---------------------------------------------------------------\n");
                    logBuilder.append("   • Inference Time   : ").append(llmTimeMs != null ? llmTimeMs : 0).append(" ms\n");
                    logBuilder.append("   • Model Reasoning  :\n");
                    logBuilder.append("     ").append(rawContent != null ? rawContent.replace("\n", "\n     ") : "N/A").append("\n\n");

                    logBuilder.append("3️⃣ FINAL GENERATED POSTGRESQL QUERY\n");
                    logBuilder.append("---------------------------------------------------------------\n");
                    logBuilder.append("   ").append(sql != null && !sql.isBlank() ? sql : "N/A").append("\n\n");

                    logBuilder.append("4️⃣ DATABASE EXECUTION METRICS\n");
                    logBuilder.append("---------------------------------------------------------------\n");
                    logBuilder.append("   • Execution Status : ").append(dbStatus).append("\n");
                    logBuilder.append("   • DB Query Time    : ").append(dbTimeMs != null ? dbTimeMs : 0).append(" ms\n");
                    logBuilder.append("   • Total Pipeline   : ").append(totalTimeMs != null ? totalTimeMs : 0).append(" ms\n");
                    logBuilder.append("   • Matching Records : ").append(temples != null ? temples.size() : 0).append("\n\n");

                    logBuilder.append("5️⃣ MATCHING TEMPLE SUMMARY\n");
                    logBuilder.append("---------------------------------------------------------------\n");
                    if (temples != null && !temples.isEmpty()) {
                        for (int i = 0; i < temples.size(); i++) {
                            Temple t = temples.get(i);
                            logBuilder.append(String.format("   [%d] %s (%s, %s)\n",
                                i + 1,
                                t.name(),
                                t.city() != null ? t.city() : "N/A",
                                t.district() != null ? t.district() : (t.state() != null ? t.state() : "N/A")
                            ));
                        }
                    } else {
                        logBuilder.append("   No matching temple records found.\n");
                    }

                    return Flux.just(logBuilder.toString());
                });
    }
}
