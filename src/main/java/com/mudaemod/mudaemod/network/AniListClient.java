package com.mudaemod.mudaemod.network;

import com.mudaemod.mudaemod.MudaeMod;
import com.mudaemod.mudaemod.data.Character;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class AniListClient {

    private static final String ANILIST_URL = "https://graphql.anilist.co";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Random RANDOM = new Random();

    // AniList tiene ~170k personajes; usamos un ID aleatorio en ese rango
    private static final int MAX_CHARACTER_ID = 170000;

    public static CompletableFuture<Optional<Character>> rollRandom(boolean femaleFirst) {
        return CompletableFuture.supplyAsync(() -> {
            // Intentamos hasta 5 veces para encontrar un personaje válido con imagen
            for (int attempt = 0; attempt < 5; attempt++) {
                int id = RANDOM.nextInt(MAX_CHARACTER_ID) + 1;
                Optional<Character> result = fetchCharacter(id);
                if (result.isPresent()) return result;
            }
            return Optional.empty();
        });
    }

    private static Optional<Character> fetchCharacter(int id) {
        String query = """
                {
                  "query": "query ($id: Int) { Character(id: $id) { id name { full } image { large } favourites media(sort: POPULARITY_DESC, perPage: 1) { nodes { title { romaji } } } } }",
                  "variables": { "id": %d }
                }
                """.formatted(id);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANILIST_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(query))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return Optional.empty();

            return parseCharacter(response.body(), id);

        } catch (IOException | InterruptedException e) {
            MudaeMod.LOGGER.warn("AniList request failed for id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<Character> parseCharacter(String json, int id) {
        // Parseamos manualmente para no necesitar dependencia de gson/jackson
        if (json.contains("\"Character\":null")) return Optional.empty();

        String name = extractJson(json, "\"full\":\"", "\"");
        String image = extractJson(json, "\"large\":\"", "\"");
        String anime = extractJson(json, "\"romaji\":\"", "\"");
        String favouritesStr = extractJson(json, "\"favourites\":", ",");
        if (favouritesStr == null) favouritesStr = extractJson(json, "\"favourites\":", "}");

        if (name == null || name.isEmpty()) return Optional.empty();
        if (image == null || image.contains("default.jpg")) return Optional.empty();

        int favourites = 0;
        try { if (favouritesStr != null) favourites = Integer.parseInt(favouritesStr.trim()); } catch (NumberFormatException ignored) {}
        int kakera = Math.max(20, Math.min(2000, favourites / 50 + 20));

        return Optional.of(new Character(id, name, anime != null ? anime : "Original", image, kakera));
    }

    private static String extractJson(String json, String startMarker, String endMarker) {
        int start = json.indexOf(startMarker);
        if (start == -1) return null;
        start += startMarker.length();
        int end = json.indexOf(endMarker, start);
        if (end == -1) return null;
        return json.substring(start, end);
    }
}
