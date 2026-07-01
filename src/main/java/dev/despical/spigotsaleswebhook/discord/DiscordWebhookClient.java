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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
public class DiscordWebhookClient {

    private static final int MAX_EMBEDS_PER_MESSAGE = 10;
    private static final int EMBED_COLOR = 0x57F287;
    private static final ZoneId DISPLAY_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter FOOTER_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI webhookUri;
    private final String username;
    private final String avatarUrl;

    public DiscordWebhookClient(ObjectMapper objectMapper, AppConfig.DiscordSettings config) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.webhookUri = URI.create(config.webhookUrl());
        this.username = config.username();
        this.avatarUrl = config.avatarUrl();
    }

    public void send(List<SpigotSale> sales) throws IOException, InterruptedException {
        for (int start = 0; start < sales.size(); start += MAX_EMBEDS_PER_MESSAGE) {
            int end = Math.min(start + MAX_EMBEDS_PER_MESSAGE, sales.size());
            sendChunk(sales.subList(start, end));

            Thread.sleep(350);
        }
    }

    public void sendTest() throws IOException, InterruptedException {
        send(List.of(new SpigotSale(
            "Example Plugin",
            "https://www.spigotmc.org/",
            "Despical",
            "https://github.com/Despical",
            ZonedDateTime.now(DISPLAY_ZONE),
            0.0,
            "FREE"
        )));
    }

    private void sendChunk(List<SpigotSale> sales) throws IOException, InterruptedException {
        List<Map<String, Object>> embeds = new ArrayList<>();

        for (SpigotSale sale : sales) {
            embeds.add(createEmbed(sale));
        }

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("username", username);
            if (avatarUrl != null && !avatarUrl.isBlank()) {
                put("avatar_url", avatarUrl);
            }

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
            put("color", EMBED_COLOR);
            put("description", formatSaleMessage(sale));
            put("footer", Map.of("text", "SpigotMC \u2022 " + formatPurchaseDate(sale)));
        }};
    }

    private String formatSaleMessage(SpigotSale sale) {
        return formatBuyer(sale) + " has purchased [`" + escapeInlineCode(sale.pluginName()) + "`](" + sale.pluginUrl() + ") for `" + formatPrice(sale) + "`.";
    }

    private String formatBuyer(SpigotSale sale) {
        String username = "`" + escapeInlineCode(sale.username()) + "`";

        if (sale.userProfileUrl() == null || sale.userProfileUrl().isBlank()) {
            return username;
        }

        return "[" + username + "](" + sale.userProfileUrl() + ")";
    }

    private String formatPrice(SpigotSale sale) {
        if ("FREE".equalsIgnoreCase(sale.currency()) || sale.price() == 0.0) {
            return "FREE";
        }

        return String.format(Locale.US, "%.2f %s", sale.price(), sale.currency());
    }

    private String formatPurchaseDate(SpigotSale sale) {
        return sale.purchaseDate().withZoneSameInstant(DISPLAY_ZONE).format(FOOTER_DATE_FORMATTER);
    }

    private String escapeInlineCode(String value) {
        return value.replace("`", "'");
    }
}



