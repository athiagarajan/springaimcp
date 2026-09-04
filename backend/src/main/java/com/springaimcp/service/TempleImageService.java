package com.springaimcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springaimcp.model.Temple;
import com.springaimcp.model.TempleImage;
import com.springaimcp.repository.TempleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempleImageService {

    private static final Logger log = LoggerFactory.getLogger(TempleImageService.class);

    private final TempleRepository templeRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Map<Long, List<TempleImage>> imageCache = new ConcurrentHashMap<>();

    private static final String USER_AGENT = "IndianTemplesExplorer/1.0 (https://github.com/athiagarajan/springaimcp; contact@example.com)";

    public TempleImageService(TempleRepository templeRepository, ObjectMapper objectMapper) {
        this.templeRepository = templeRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Mono<List<TempleImage>> getImagesForTemple(Long id) {
        return Mono.fromCallable(() -> {
            if (imageCache.containsKey(id)) {
                return imageCache.get(id);
            }

            Optional<Temple> templeOpt = templeRepository.findById(id);
            if (templeOpt.isEmpty()) {
                return Collections.<TempleImage>emptyList();
            }

            Temple temple = templeOpt.get();
            List<TempleImage> images = fetchImagesFromWeb(temple);

            if (!images.isEmpty()) {
                log.info("Found {} authentic photographs for temple ID {} ('{}')", images.size(), id, temple.name());
                imageCache.put(id, images);
                return images;
            }

            log.info("No web images found for temple ID {} ('{}'), providing cultural architectural representative visual", id, temple.name());
            // Do NOT cache fallback images in imageCache so subsequent retries can find real images
            return getRepresentativeImages(temple);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<TempleImage> fetchImagesFromWeb(Temple temple) {
        List<TempleImage> results = new ArrayList<>();
        Set<String> seenUrls = new HashSet<>();

        List<String> queries = generateSearchQueries(temple);
        log.debug("Searching temple images for ID {} with query candidates: {}", temple.id(), queries);

        // AVENUE 1: Direct Wikimedia Commons Search (Highest Authenticity & Quantity)
        for (String q : queries) {
            if (!isSpecificQuery(q)) continue;
            List<TempleImage> commonsImgs = searchWikimediaCommonsDirect(q);
            for (TempleImage img : commonsImgs) {
                if (seenUrls.add(img.url())) {
                    results.add(img);
                }
                if (results.size() >= 4) break;
            }
            if (results.size() >= 3) {
                return results;
            }
        }

        // AVENUE 2: Wikipedia Unified PageImages Search
        if (results.size() < 3) {
            for (String q : queries) {
                if (!isSpecificQuery(q)) continue;
                List<TempleImage> wikiImgs = searchWikipediaUnified(q);
                for (TempleImage img : wikiImgs) {
                    if (seenUrls.add(img.url())) {
                        results.add(img);
                    }
                    if (results.size() >= 4) break;
                }
                if (results.size() >= 2) {
                    break;
                }
            }
        }

        return results;
    }

    private String sanitize(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.equals("-") || trimmed.equalsIgnoreCase("none") || trimmed.equalsIgnoreCase("nil")
                || trimmed.equalsIgnoreCase("n/a") || trimmed.equalsIgnoreCase("null") || trimmed.matches("^[\\W_]+$")) {
            return "";
        }
        return trimmed;
    }

    private boolean isSpecificQuery(String q) {
        if (q == null || q.isBlank()) return false;
        String normalized = q.replaceAll("(?i)\\b(temple|mandir|kovil|shrine|sri|lord)\\b", "").replaceAll("[\\W_]+", "").trim();
        return normalized.length() >= 3;
    }

    private List<String> generateSearchQueries(Temple temple) {
        List<String> queries = new ArrayList<>();
        String rawName = sanitize(temple.name());
        String clean = rawName.replaceAll("(?i)^sri\\s+", "")
                              .replaceAll("(?i)\\btemple\\b", "")
                              .trim();
        String city = sanitize(temple.city());
        String moolavar = sanitize(temple.moolavar());
        String historical = sanitize(temple.historicalName());

        if (!clean.isBlank()) {
            if (!city.isBlank() && !clean.toLowerCase().contains(city.toLowerCase())) {
                queries.add(clean + " " + city + " Temple");
            } else {
                queries.add(clean + " Temple");
            }
        }

        if (!moolavar.isBlank()) {
            String mClean = moolavar.replaceAll("(?i)\\b(lord|sri|swami|swamy)\\b", "").trim();
            String firstM = mClean.split("[,;/]")[0].trim();
            if (!firstM.isBlank() && firstM.length() >= 3) {
                if (!city.isBlank()) {
                    queries.add(city + " " + firstM + " Temple");
                    queries.add(firstM + " Temple " + city);
                }
                queries.add(firstM + " Temple");
            }
        }

        if (!historical.isBlank() && historical.length() >= 3) {
            queries.add(historical + " Temple");
            if (!city.isBlank()) {
                queries.add(historical + " " + city);
            }
        }

        if (!clean.isBlank()) {
            queries.add(clean);
        }

        return queries;
    }

    private List<TempleImage> searchWikimediaCommonsDirect(String query) {
        List<TempleImage> list = new ArrayList<>();
        if (!isSpecificQuery(query)) return list;
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch="
                    + encoded + "&gsrnamespace=6&gsrlimit=6&prop=imageinfo&iiprop=url&iiurlwidth=960&format=json";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode pages = root.path("query").path("pages");
                if (pages.isObject()) {
                    pages.fields().forEachRemaining(entry -> {
                        JsonNode page = entry.getValue();
                        JsonNode imgInfo = page.path("imageinfo");
                        if (imgInfo.isArray() && !imgInfo.isEmpty()) {
                            String u = imgInfo.get(0).path("thumburl").asText("");
                            if (u.isBlank()) {
                                u = imgInfo.get(0).path("url").asText("");
                            }
                            String lower = u.toLowerCase();
                            if ((lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.contains(".jpg?") || lower.contains(".png?"))
                                    && !lower.endsWith(".pdf") && !lower.contains(".pdf?")) {
                                String title = page.path("title").asText("")
                                        .replace("File:", "")
                                        .replaceAll("(?i)\\.(jpg|jpeg|png)", "")
                                        .replaceAll("\\(.*?\\)", "")
                                        .replace("_", " ")
                                        .trim();
                                list.add(new TempleImage(
                                        u,
                                        title.isBlank() ? "Temple Architectural Photograph" : title,
                                        "Temple Architectural Photograph",
                                        "Wikimedia Commons"
                                ));
                            }
                        }
                    });
                }
            } else {
                log.warn("Commons search for '{}' returned HTTP {}", query, response.statusCode());
            }
        } catch (Exception e) {
            log.warn("Commons search error for '{}': {}", query, e.getMessage());
        }
        return list;
    }

    private List<TempleImage> searchWikipediaUnified(String query) {
        List<TempleImage> list = new ArrayList<>();
        if (!isSpecificQuery(query)) return list;
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch="
                    + encoded + "&gsrlimit=3&prop=pageimages|pageterms&pithumbsize=960&format=json";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode pages = root.path("query").path("pages");
                if (pages.isObject()) {
                    pages.fields().forEachRemaining(entry -> {
                        JsonNode p = entry.getValue();
                        String thumb = p.path("thumbnail").path("source").asText(null);
                        if (thumb != null && !thumb.isBlank()) {
                            String title = p.path("title").asText("Temple View");
                            String desc = "Temple Architectural Heritage";
                            JsonNode terms = p.path("terms").path("description");
                            if (terms.isArray() && !terms.isEmpty()) {
                                desc = terms.get(0).asText(desc);
                            }
                            list.add(new TempleImage(
                                    thumb,
                                    title,
                                    desc,
                                    "Wikipedia"
                            ));
                        }
                    });
                }
            } else {
                log.warn("Wikipedia search for '{}' returned HTTP {}", query, response.statusCode());
            }
        } catch (Exception e) {
            log.warn("Wikipedia unified search error for '{}': {}", query, e.getMessage());
        }
        return list;
    }

    private List<TempleImage> getRepresentativeImages(Temple temple) {
        List<TempleImage> list = new ArrayList<>();
        String moolavar = (temple.moolavar() != null) ? temple.moolavar().toLowerCase() : "";
        String name = (temple.name() != null) ? temple.name().toLowerCase() : "";
        String combined = moolavar + " " + name;

        if (combined.contains("anjaneyar") || combined.contains("hanuman") || combined.contains("maruti")) {
            list.add(new TempleImage(
                    "https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fd/Anjaneyar_Temple%2C_Namakkal.jpg/960px-Anjaneyar_Temple%2C_Namakkal.jpg",
                    "Sacred Sri Anjaneyar Shrine",
                    "Ancient South Indian temple dedicated to Lord Anjaneyar (Hanuman)",
                    "Heritage Cultural Archive"
            ));
        } else if (combined.contains("shiva") || combined.contains("ekambareswarar") || combined.contains("lingam") || combined.contains("nataraj")) {
            list.add(new TempleImage(
                    "https://thumb.wikimedia.org/wikipedia/commons/thumb/9/95/Ekambareswarar_Temple_2.jpg/960px-Ekambareswarar_Temple_2.jpg",
                    "Dravidian Rajagopuram & Temple Complex",
                    "Majestic stone Rajagopuram showcasing traditional Dravidian temple architecture",
                    "Heritage Cultural Archive"
            ));
        } else if (combined.contains("murugan") || combined.contains("subramanya") || combined.contains("idumban") || combined.contains("karthik")) {
            list.add(new TempleImage(
                    "https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c4/Arulmigu_Dhandayuthapani_Swamy_Temple_in_Palany_hill.jpg/960px-Arulmigu_Dhandayuthapani_Swamy_Temple_in_Palany_hill.jpg",
                    "Sacred Murugan Hill Temple & Gopuram",
                    "Holy hill shrine and surrounding sacred theertham",
                    "Heritage Cultural Archive"
            ));
        } else if (combined.contains("perumal") || combined.contains("vishnu") || combined.contains("ranganath") || combined.contains("venkate") || combined.contains("krishna") || combined.contains("rama")) {
            list.add(new TempleImage(
                    "https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Srirangam_Temple_Gopuram_View.jpg/960px-Srirangam_Temple_Gopuram_View.jpg",
                    "Dravidian Vaishnavite Rajagopuram",
                    "Majestic Dravidian temple tower dedicated to Lord Vishnu",
                    "Heritage Cultural Archive"
            ));
        } else {
            list.add(new TempleImage(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e4/PERUR_PATTEESHWARAR_TEMPLE.jpg/960px-PERUR_PATTEESHWARAR_TEMPLE.jpg",
                    "Sacred South Indian Temple Complex",
                    "Traditional Dravidian stone gopuram and pillared mandapam",
                    "Heritage Cultural Archive"
            ));
        }
        return list;
    }
}
