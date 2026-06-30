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

package dev.despical.spigotsaleswebhook.spigot;

import dev.despical.spigotsaleswebhook.config.AppConfig;
import dev.despical.spigotsaleswebhook.model.PluginTarget;
import dev.despical.spigotsaleswebhook.model.SpigotSale;
import dev.despical.spigotsaleswebhook.util.DateParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
public class SpigotScraper {

    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    private final String cookie;
    private final long requestDelayMs;

    public SpigotScraper(AppConfig.SpigotSettings config) {
        this.cookie = config.cookie();
        this.requestDelayMs = config.requestDelayMs();
    }

    public List<SpigotSale> scrape(PluginTarget plugin) throws IOException {
        List<SpigotSale> sales = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = page == 1 ? plugin.buyerListUrl() : plugin.buyerListUrl() + "?page=" + page;
            Document document = Jsoup.connect(url)
                .header("Cookie", cookie)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/122.0.0.0 Safari/537.36")
                .referrer("https://www.spigotmc.org/")
                .timeout(15000)
                .get();

            Elements items = document.select("li.primaryContent.memberListItem");
            if (items.isEmpty()) {
                break;
            }

            for (Element item : items) {
                SpigotSale sale = parseSale(plugin, item);

                if (sale != null) {
                    sales.add(sale);
                }
            }

            page++;
            sleepBetweenPages();
        }

        return sales;
    }

    private SpigotSale parseSale(PluginTarget plugin, Element item) {
        Element userElement = item.selectFirst("a.username");

        if (userElement == null) {
            return null;
        }

        String username = userElement.text();
        String rawText = item.text();
        String dateText = extractDateText(item, rawText);
        ZonedDateTime purchaseDate = DateParser.parse(dateText);
        PriceInfo price = parsePrice(extractBetween(rawText, username));

        return new SpigotSale(
            plugin.name(),
            plugin.buyerListUrl(),
            username,
            purchaseDate,
            price.amount(),
            price.currency()
        );
    }

    private String extractDateText(Element item, String rawText) {
        Element dateElement = item.selectFirst("span.DateTime");

        if (dateElement != null && dateElement.hasAttr("title")) {
            return dateElement.attr("title");
        }

        String[] parts = rawText.split(" Purchased| Delete");
        return parts.length > 0 ? parts[0].trim() : "";
    }

    private PriceInfo parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank() || rawPrice.equalsIgnoreCase("N/A") || rawPrice.equalsIgnoreCase("Delete")) {
            return new PriceInfo(0.0, "FREE");
        }

        Matcher matcher = PRICE_PATTERN.matcher(rawPrice);
        if (!matcher.find()) {
            return new PriceInfo(0.0, "FREE");
        }

        double amount = Double.parseDouble(matcher.group(1).replace(",", "."));
        String currency = rawPrice
            .replace(matcher.group(1), "")
            .trim()
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z\\u20AC\\u00A3$]", "");

        if (currency.contains("€") || currency.equals("EUR")) {
            currency = "EUR";
        } else if (currency.contains("$") || currency.equals("USD")) {
            currency = "USD";
        } else if (currency.contains("£") || currency.equals("GBP")) {
            currency = "GBP";
        } else if (currency.isBlank()) {
            currency = "UNKNOWN";
        }

        return new PriceInfo(amount, currency);
    }

    private String extractBetween(String text, String username) {
        int start = text.indexOf("Purchased For:");

        if (start == -1) {
            return "N/A";
        }

        start += "Purchased For:".length();
        int end = text.indexOf(username, start);

        if (end == -1) {
            end = text.length();
        }

        return text.substring(start, end).trim();
    }

    private void sleepBetweenPages() {
        try {
            Thread.sleep(requestDelayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private record PriceInfo(double amount, String currency) {
    }
}
