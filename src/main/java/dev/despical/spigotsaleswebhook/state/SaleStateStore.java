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

package dev.despical.spigotsaleswebhook.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
@RequiredArgsConstructor
public class SaleStateStore {

    private static final TypeReference<Map<String, Set<String>>> PLUGIN_KEY_MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final Path stateFile;

    public SaleState load() {
        if (!Files.exists(stateFile)) {
            return SaleState.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(stateFile.toFile());
            if (root == null || root.isNull()) {
                return SaleState.empty();
            }

            boolean initialized = root.path("initialized").asBoolean(false);
            JsonNode pluginKeysNode = root.path("seenSalesByPlugin");

            if (pluginKeysNode.isObject()) {
                Map<String, Set<String>> pluginKeys = objectMapper.convertValue(pluginKeysNode, PLUGIN_KEY_MAP_TYPE);
                return new SaleState(initialized, pluginKeys);
            }

            return SaleState.empty();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read state file: " + stateFile, exception);
        }
    }

    public void save(SaleState state) {
        try {
            Path parent = stateFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(stateFile.toFile(), state);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write state file: " + stateFile, exception);
        }
    }
}
