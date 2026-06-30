/*
 * SpigotSalesWebhook - SpigotMC premium sales Discord webhook notifier
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.despical.spigotsaleswebhook.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.despical.spigotsaleswebhook.config.AppConfig;
import dev.despical.spigotsaleswebhook.model.SpigotSale;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
public class DiscordWebhookClient {

    private static final int MAX_EMBEDS_PER_MESSAGE = 10;
    private static final ZoneId DISPLAY_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm z");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI webhookUri;
    private final String username;

    public DiscordWebhookClient(ObjectMapper objectMapper, AppConfig.DiscordSettings config) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.webhookUri = URI.create(config.webhookUrl());
        this.username = config.username();
    }

    public void send(List<SpigotSale> sales) throws IOException, InterruptedException {
        for (int start = 0; start < sales.size(); start += MAX_EMBEDS_PER_MESSAGE) {
            int end = Math.min(start + MAX_EMBEDS_PER_MESSAGE, sales.size());
            sendChunk(sales.subList(start, end));

            Thread.sleep(350);
        }
    }

    private void sendChunk(List<SpigotSale> sales) throws IOException, InterruptedException {
        List<Map<String, Object>> embeds = new ArrayList<>();

        for (SpigotSale sale : sales) {
            embeds.add(createEmbed(sale));
        }

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("username", username);
            put("embeds", embeds);
            put("allowed_mentions", Map.of("parse", List.of()));
        }};

        HttpRequest request = HttpRequest.newBuilder(webhookUri)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Discord webhook failed with HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private Map<String, Object> createEmbed(SpigotSale sale) {
        return new LinkedHashMap<>() {{
            put("title", "New Spigot sale");
            put("url", sale.pluginUrl());
            put("color", 0x57F287);
            put("description", "**" + escapeMarkdown(sale.username()) + "** purchased **" + escapeMarkdown(sale.pluginName()) + "**");
            put("fields", List.of(
                field("Buyer", sale.username(), true),
                field("Plugin", sale.pluginName(), true),
                field("Price", formatPrice(sale), true),
                field("Purchased At", sale.purchaseDate().withZoneSameInstant(DISPLAY_ZONE).format(DATE_FORMATTER), false)
            ));

            put("footer", Map.of("text", "Spigot Purchase Webhook by Despical."));
        }};
    }

    private Map<String, Object> field(String name, String value, boolean inline) {
        return new LinkedHashMap<>() {{
            put("name", name);
            put("value", truncate(value));
            put("inline", inline);
        }};
    }

    private String formatPrice(SpigotSale sale) {
        if ("FREE".equalsIgnoreCase(sale.currency()) || sale.price() == 0.0) {
            return "FREE";
        }

        return String.format(Locale.US, "%.2f %s", sale.price(), sale.currency());
    }

    private String truncate(String value) {
        final int limit = 1024;

        if (value.length() <= limit) {
            return value;
        }

        return value.substring(0, limit - 3) + "...";
    }

    private String escapeMarkdown(String value) {
        return value.replace("*", "\\*").replace("_", "\\_").replace("`", "\\`");
    }
}
