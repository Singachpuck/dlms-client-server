package com.imt.dlms.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class MandjetService {

    private static final List<String> EMONTX_FEED_IDS = List.of(
            "81", "82", "83", "84", "85", "86"
    );

    private static final String EMONTX_FEED_IDS_JOINED = String.join(",", EMONTX_FEED_IDS);

    private static final String BATTERY_FEED_ID = "33";

    private static final String VOLTAGE_FEED_ID = "80";

    private final String mandjetReadApiKey = System.getenv("MANDJET_READ_API_KEY");

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Integer> getEmonTxFeeds() {
        try {

            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(String.format("https://emoncms.fr/feed/fetch.json?ids=%s&apikey=%s",
                            EMONTX_FEED_IDS_JOINED, mandjetReadApiKey)))
                    .build();

            final HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }

            final JsonNode root = mapper.readTree(response.body());

            final List<Integer> powerList = new ArrayList<>();
            for (JsonNode item : root) {
                powerList.add(item.asInt());
            }
            return powerList;
        } catch (URISyntaxException | IOException | InterruptedException | RuntimeException e) {
            System.err.println("Error sending request to Emoncms: " + e.getMessage());
        }

        return EMONTX_FEED_IDS
                .stream()
                .map(id -> 0)
                .toList();
    }

    public int getBatteryPercentage() {
        try {

            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(String.format("https://emoncms.fr/feed/get.json?id=%s&field=value&apikey=%s",
                            BATTERY_FEED_ID, mandjetReadApiKey)))
                    .build();

            final HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }

            return Integer.parseInt(response.body());
        } catch (URISyntaxException | IOException | InterruptedException | RuntimeException e) {
            System.err.println("Error sending request to Emoncms: " + e.getMessage());
        }

        return 0;
    }

    public double getVoltageFeed() {
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(String.format("https://emoncms.fr/feed/get.json?id=%s&field=value&apikey=%s",
                            VOLTAGE_FEED_ID, mandjetReadApiKey)))
                    .build();

            final HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }

            return Double.parseDouble(response.body());
        } catch (URISyntaxException | IOException | InterruptedException | RuntimeException e) {
            System.err.println("Error sending request to Emoncms: " + e.getMessage());
        }

        return 0.0;
    }
}
