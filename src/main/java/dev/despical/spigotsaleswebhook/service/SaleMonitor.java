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

package dev.despical.spigotsaleswebhook.service;

import dev.despical.spigotsaleswebhook.config.AppConfig;
import dev.despical.spigotsaleswebhook.discord.DiscordWebhookClient;
import dev.despical.spigotsaleswebhook.model.PluginTarget;
import dev.despical.spigotsaleswebhook.model.SpigotSale;
import dev.despical.spigotsaleswebhook.spigot.SpigotScraper;
import dev.despical.spigotsaleswebhook.state.SaleState;
import dev.despical.spigotsaleswebhook.state.SaleStateStore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
@RequiredArgsConstructor
public class SaleMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaleMonitor.class);

    private final AppConfig config;
    private final SpigotScraper scraper;
    private final DiscordWebhookClient webhookClient;
    private final SaleStateStore stateStore;

    public void runOnce() {
        LOGGER.info("Starting Spigot sales scan...");

        SaleState state = stateStore.load();

        List<SpigotSale> scrapedSales = scrapeAllPlugins();
        List<SpigotSale> newSales = scrapedSales.stream()
            .filter(sale -> !state.seenSalesFor(sale.pluginName()).contains(sale.buyerKey()))
            .sorted(Comparator.comparing(SpigotSale::purchaseDate))
            .toList();

        Map<String, Set<String>> updatedKeys = mergeSeenSales(state, scrapedSales);
        if (!state.initialized() && !config.scan().notifyExistingOnFirstRun()) {
            stateStore.save(state.markInitialized(updatedKeys));

            LOGGER.info("First run completed. Recorded {} existing buyers without sending Discord messages.", countKeys(updatedKeys));
            return;
        }

        if (newSales.isEmpty()) {
            stateStore.save(state.markInitialized(updatedKeys));

            LOGGER.info("Scan completed. No new sales found.");
            return;
        }

        try {
            webhookClient.send(newSales);
            stateStore.save(state.markInitialized(updatedKeys));

            LOGGER.info("Scan completed. Sent {} new sales to Discord.", newSales.size());
        } catch (Exception exception) {
            LOGGER.error("Could not send Discord webhook. New sales will be retried on the next scan.", exception);
        }
    }

    private List<SpigotSale> scrapeAllPlugins() {
        List<SpigotSale> sales = new ArrayList<>();

        for (PluginTarget plugin : config.spigot().plugins()) {
            try {
                LOGGER.info("Fetching buyers for {}.", plugin.name());

                List<SpigotSale> pluginSales = scraper.scrape(plugin);
                sales.addAll(pluginSales);

                LOGGER.info("Fetched {} buyers for {}.", pluginSales.size(), plugin.name());
            } catch (Exception exception) {
                LOGGER.error("Failed to fetch buyers for {}.", plugin.name(), exception);
            }
        }

        return sales;
    }

    private Map<String, Set<String>> mergeSeenSales(SaleState state, List<SpigotSale> scrapedSales) {
        Map<String, Set<String>> updatedKeys = new LinkedHashMap<>();
        state.seenSalesByPlugin().forEach((pluginName, keys) -> updatedKeys.put(pluginName, new LinkedHashSet<>(keys)));

        for (SpigotSale sale : scrapedSales) {
            updatedKeys.computeIfAbsent(sale.pluginName(), _ -> new LinkedHashSet<>()).add(sale.buyerKey());
        }

        return updatedKeys;
    }

    private long countKeys(Map<String, Set<String>> keysByPlugin) {
        return keysByPlugin.values().stream().mapToLong(Set::size).sum();
    }
}
