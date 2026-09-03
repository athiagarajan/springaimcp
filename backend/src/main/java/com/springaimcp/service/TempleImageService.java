package com.springaimcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springaimcp.model.Temple;
import com.springaimcp.model.TempleImage;
import com.springaimcp.repository.TempleRepository;
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

            if (images.isEmpty()) {
                images = getRepresentativeImages(temple);
            }

            if (!images.isEmpty()) {
                imageCache.put(id, images);
            }
            return images;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<TempleImage> fetchImagesFromWeb(Temple temple) {
        List<TempleImage> results = new ArrayList<>();
        Set<String> seenUrls = new HashSet<>();

        // Generate query variations
        List<String> searchCandidates = new ArrayList<>();
        if (temple.name() != null && !temple.name().isBlank()) {
            String cleanName = temple.name().replaceAll("(?i)^sri\\s+", "").trim();
            if (temple.city() != null && !temple.city().isBlank()) {
                searchCandidates.add(cleanName + " " + temple.city());
            }
            searchCandidates.add(cleanName + " Temple");
            searchCandidates.add(cleanName);
        }
        if (temple.historicalName() != null && !temple.historicalName().isBlank()) {
            searchCandidates.add(temple.historicalName().trim() + " Temple");
        }

        String foundTitle = null;
        for (String q : searchCandidates) {
            foundTitle = searchWikipediaTitle(q);
            if (foundTitle != null) {
                break;
            }
        }

        if (foundTitle != null) {
            // 1. Fetch main page image
            TempleImage mainImg = fetchWikipediaSummaryImage(foundTitle);
            if (mainImg != null && seenUrls.add(mainImg.url())) {
                results.add(mainImg);
            }

            // 2. Fetch up to 3 additional gallery images from Wikimedia Commons
            List<TempleImage> commonsImgs = fetchWikimediaCommonsGallery(foundTitle);
            for (TempleImage img : commonsImgs) {
                if (results.size() >= 4) break;
                if (seenUrls.add(img.url())) {
                    results.add(img);
                }
            }
        }

        return results;
    }

    private String searchWikipediaTitle(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + encoded + "&srlimit=2&format=json";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode searchArr = root.path("query").path("search");
                if (searchArr.isArray() && !searchArr.isEmpty()) {
                    return searchArr.get(0).path("title").asText(null);
                }
            }
        } catch (Exception e) {
            // Non-fatal, try next candidate
        }
        return null;
    }

    private TempleImage fetchWikipediaSummaryImage(String title) {
        try {
            String encoded = URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8);
            String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encoded;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String imgUrl = null;
                if (root.has("originalimage") && root.get("originalimage").has("source")) {
                    imgUrl = root.get("originalimage").get("source").asText();
                } else if (root.has("thumbnail") && root.get("thumbnail").has("source")) {
                    imgUrl = root.get("thumbnail").get("source").asText();
                }

                if (imgUrl != null && !imgUrl.isBlank()) {
                    String desc = root.path("description").asText("Main Temple Architecture");
                    return new TempleImage(
                            imgUrl,
                            root.path("title").asText(title),
                            desc,
                            "Wikipedia"
                    );
                }
            }
        } catch (Exception e) {
            // Non-fatal
        }
        return null;
    }

    private List<TempleImage> fetchWikimediaCommonsGallery(String title) {
        List<TempleImage> gallery = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=" + encoded + "&gsrnamespace=6&gsrlimit=6&prop=imageinfo&iiprop=url&format=json";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(5))
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
                            String u = imgInfo.get(0).path("url").asText("");
                            String lower = u.toLowerCase();
                            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
                                String cleanTitle = page.path("title").asText("")
                                        .replace("File:", "")
                                        .replaceAll("(?i)\\.(jpg|jpeg|png)", "")
                                        .replace("_", " ");
                                gallery.add(new TempleImage(
                                        u,
                                        cleanTitle,
                                        "Architectural & Sculptural Heritage View",
                                        "Wikimedia Commons"
                                ));
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            // Non-fatal
        }
        return gallery;
    }

    private List<TempleImage> getRepresentativeImages(Temple temple) {
        List<TempleImage> list = new ArrayList<>();
        String moolavar = (temple.moolavar() != null) ? temple.moolavar().toLowerCase() : "";

        if (moolavar.contains("shiva") || moolavar.contains("ekambareswarar") || moolavar.contains("lingam")) {
            list.add(new TempleImage(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/06/Ekambareswarar5.jpg/960px-Ekambareswarar5.jpg",
                    "Dravidian Rajagopuram & Temple Complex",
                    "Majestic stone Rajagopuram showcasing traditional Dravidian temple architecture",
                    "Heritage Cultural Archive"
            ));
            list.add(new TempleImage(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Brihadisvara_Temple_during_Maha_Shivaratri-WUS03611_%28edit%29.jpg/960px-Brihadisvara_Temple_during_Maha_Shivaratri-WUS03611_%28edit%29.jpg",
                    "Sacred Vimanam & Sanctum Sanctorum",
                    "Ancient stone sanctum dedicated to Lord Shiva",
                    "Heritage Cultural Archive"
            ));
        } else if (moolavar.contains("murugan") || moolavar.contains("subramanya") || moolavar.contains("idumban")) {
            list.add(new TempleImage(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/7/76/Palanihills.JPG/960px-Palanihills.JPG",
                    "Sacred Murugan Hill Temple & Gopuram",
                    "Holy hill shrine and surrounding sacred theertham",
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
