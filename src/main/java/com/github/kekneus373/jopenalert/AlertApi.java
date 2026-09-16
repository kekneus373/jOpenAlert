package com.github.kekneus373.jopenalert;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AlertApi {
    private static final Pattern STATE_ENTRY = Pattern.compile("\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"\\s*:\\s*(true|false)");
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final URI statusUri = URI.create(Config.ALERTS_STATUS_URL);
    private final URI mapUri = URI.create(Config.MAP_URL);

    public Map<String, Boolean> fetchStates() throws IOException, InterruptedException {
        String body = get(statusUri);
        var states = new LinkedHashMap<String, Boolean>();
        Matcher matcher = STATE_ENTRY.matcher(body);
        while (matcher.find()) {
            states.put(unescape(matcher.group(1)), Boolean.parseBoolean(matcher.group(2)));
        }
        if (states.isEmpty()) {
            throw new IOException("The alerts response contained no states");
        }
        return states;
    }

    public Map<String, Boolean> fetchRegions() throws IOException, InterruptedException {
        return fetchStates();
    }

    public byte[] fetchMapPng() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(mapUri).timeout(Duration.ofSeconds(20)).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Map request returned HTTP " + response.statusCode());
        }
        return response.body();
    }

    private String get(URI uri) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Alerts request returned HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static String unescape(String value) {
        return value.replace("\\\\", "\\").replace("\\\"", "\"");
    }
}