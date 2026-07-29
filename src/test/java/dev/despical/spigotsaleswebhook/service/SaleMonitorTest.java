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

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.despical.spigotsaleswebhook.config.AppConfig;
import dev.despical.spigotsaleswebhook.discord.DiscordWebhookClient;
import dev.despical.spigotsaleswebhook.model.PluginTarget;
import dev.despical.spigotsaleswebhook.model.SpigotSale;
import dev.despical.spigotsaleswebhook.spigot.SpigotAuthenticationException;
import dev.despical.spigotsaleswebhook.spigot.SpigotScraper;
import dev.despical.spigotsaleswebhook.state.SaleStateStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SaleMonitorTest {

    @TempDir
    Path tempDirectory;

    @Test
    void sendsOneWarningUntilSpigotAuthenticationRecovers() {
        Path stateFile = tempDirectory.resolve("seen-sales.json");
        PluginTarget plugin = new PluginTarget(
            "Advanced Parkour",
            "https://www.spigotmc.org/resources/advanced-parkour.123/buyers"
        );
        AppConfig config = new AppConfig(
            new AppConfig.DiscordSettings("https://example.com/webhook", "Spigot Sales", ""),
            new AppConfig.SpigotSettings("", 0, List.of(plugin)),
            new AppConfig.ScanSettings(Duration.ofMinutes(1), false, stateFile)
        );
        AuthenticationFailingScraper scraper = new AuthenticationFailingScraper(config.spigot());
        RecordingWebhookClient webhookClient = new RecordingWebhookClient(config.discord());
        SaleMonitor monitor = new SaleMonitor(
            config,
            scraper,
            webhookClient,
            new SaleStateStore(new ObjectMapper(), stateFile)
        );

        monitor.runOnce();
        monitor.runOnce();

        assertEquals(1, webhookClient.authenticationWarningCount);
        assertFalse(Files.exists(stateFile));

        scraper.authenticationFails = false;
        monitor.runOnce();

        scraper.authenticationFails = true;
        monitor.runOnce();

        assertEquals(2, webhookClient.authenticationWarningCount);
    }

    private static class AuthenticationFailingScraper extends SpigotScraper {

        private boolean authenticationFails = true;

        private AuthenticationFailingScraper(AppConfig.SpigotSettings config) {
            super(config);
        }

        @Override
        public List<SpigotSale> scrape(PluginTarget plugin) throws IOException {
            if (authenticationFails) {
                throw new SpigotAuthenticationException("Test authentication failure");
            }

            return List.of();
        }
    }

    private static class RecordingWebhookClient extends DiscordWebhookClient {

        private int authenticationWarningCount;

        private RecordingWebhookClient(AppConfig.DiscordSettings config) {
            super(new ObjectMapper(), config);
        }

        @Override
        public void sendAuthenticationWarning() {
            authenticationWarningCount++;
        }
    }
}
