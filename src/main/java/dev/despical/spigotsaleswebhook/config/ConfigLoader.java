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

package dev.despical.spigotsaleswebhook.config;

import dev.despical.spigotsaleswebhook.config.AppConfig.DiscordSettings;
import dev.despical.spigotsaleswebhook.config.AppConfig.ScanSettings;
import dev.despical.spigotsaleswebhook.config.AppConfig.SpigotSettings;
import dev.despical.spigotsaleswebhook.model.PluginTarget;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
public class ConfigLoader {

    public AppConfig load(Path workingDirectory) {
        YamlConfig config = YamlConfig.loadExternalOrResource(workingDirectory.resolve("config.yml"), "config.yml");

        return new AppConfig(
            bindDiscord(config),
            bindSpigot(config),
            bindScan(config, workingDirectory)
        );
    }

    private DiscordSettings bindDiscord(YamlConfig config) {
        String webhookUrl = config.getString("discord.webhook-url");
        String username = config.getString("discord.username");
        String avatarUrl = config.getString("discord.avatar-url");
        return new DiscordSettings(webhookUrl, username, avatarUrl);
    }

    private SpigotSettings bindSpigot(YamlConfig config) {
        String cookie = config.getString("spigot.cookie");
        long requestDelayMs = config.getLong("spigot.request-delay-ms");

        List<PluginTarget> plugins = config.mapList("spigot.plugins").stream()
            .map(plugin -> new PluginTarget(
                stringValue(plugin, "name"),
                stringValue(plugin, "buyer-list-url")
            ))
            .filter(plugin -> !plugin.name().isBlank() && !plugin.buyerListUrl().isBlank())
            .toList();

        if (plugins.isEmpty()) {
            throw new IllegalStateException("spigot.plugins must contain at least one plugin in config.yml.");
        }

        return new SpigotSettings(cookie, requestDelayMs, plugins);
    }

    private ScanSettings bindScan(YamlConfig config, Path workingDirectory) {
        long intervalMinutes = config.getLong("scan.interval-minutes");
        boolean notifyExistingOnFirstRun = config.getBoolean("scan.notify-existing-on-first-run");

        Path stateFile = workingDirectory.resolve(config.getString("scan.state-file")).normalize();
        return new ScanSettings(Duration.ofMinutes(intervalMinutes), notifyExistingOnFirstRun, stateFile);
    }

    private static String stringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }
}
