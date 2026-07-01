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

package dev.despical.spigotsaleswebhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.despical.spigotsaleswebhook.config.AppConfig;
import dev.despical.spigotsaleswebhook.config.ConfigLoader;
import dev.despical.spigotsaleswebhook.discord.DiscordWebhookClient;
import dev.despical.spigotsaleswebhook.service.SaleMonitor;
import dev.despical.spigotsaleswebhook.spigot.SpigotScraper;
import dev.despical.spigotsaleswebhook.state.SaleStateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
public class SpigotSalesWebhookApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpigotSalesWebhookApplication.class);

    static void main(String[] args) {
        Path workingDirectory = Path.of("").toAbsolutePath();
        AppConfig config = new ConfigLoader().load(workingDirectory);
        ObjectMapper objectMapper = new ObjectMapper();
        DiscordWebhookClient webhookClient = new DiscordWebhookClient(objectMapper, config.discord());

        List<String> argList = Arrays.asList(args);
        if (argList.contains("--test-webhook")) {
            try {
                webhookClient.sendTest();

                LOGGER.info("Discord test webhook sent.");
            } catch (Exception exception) {
                LOGGER.error("Could not send Discord test webhook.", exception);
            }

            return;
        }

        SaleMonitor monitor = new SaleMonitor(
            config,
            new SpigotScraper(config.spigot()),
            webhookClient,
            new SaleStateStore(objectMapper, config.scan().stateFile())
        );

        if (argList.contains("--once")) {
            monitor.runOnce();
            return;
        }

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(
            monitor::runOnce,
            0,
            config.scan().interval().toMinutes(),
            TimeUnit.MINUTES
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();

            LOGGER.info("SpigotSalesWebhook worker stopped.");
        }));

        LOGGER.info("SpigotSalesWebhook worker started. Interval: {} minutes.", config.scan().interval().toMinutes());
    }
}
