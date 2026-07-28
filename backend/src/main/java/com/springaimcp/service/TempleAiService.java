package com.springaimcp.service;

import com.springaimcp.model.Temple;
import com.springaimcp.repository.TempleRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class TempleAiService {

    private final ChatClient chatClient;
    private final TempleRepository templeRepository;

    public TempleAiService(ChatClient.Builder builder, TempleRepository templeRepository) {
        this.chatClient = builder.build();
        this.templeRepository = templeRepository;
    }

    public Flux<String> streamDynamicQuery(String prompt) {
        String systemPrompt = """
            You are an AI Assistant for the templeinfo PostgreSQL database.
            The database table is 'temples' with columns: id, name, moolavar, urchavar, amman_thayar, thala_virutcham, theertham, agamam_pooja, old_year, historical_name, city, district, state, singers, festival, general_information, address, phone, opening_time, speciality, prayers, thanks_giving, greatness, history, features, hf_lat, hf_lan, location, near_by_airport, near_by_railway_station, accommodation.
            Respond intelligently to user prompts by describing the relevant temples and criteria.
        """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .stream()
                .content();
    }

    public List<Temple> getAllTemples() {
        return templeRepository.findAll();
    }

    public List<Temple> search(String state, String district, String city, String keyword) {
        return templeRepository.searchByCriteria(state, district, city, keyword);
    }
}
