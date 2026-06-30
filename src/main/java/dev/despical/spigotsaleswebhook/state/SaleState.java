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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
public record SaleState(boolean initialized, Map<String, Set<String>> seenSalesByPlugin) {

    public static SaleState empty() {
        return new SaleState(false, new LinkedHashMap<>());
    }

    public Set<String> seenSalesFor(String pluginName) {
        return seenSalesByPlugin.getOrDefault(pluginName, Set.of());
    }

    public SaleState markInitialized(Map<String, Set<String>> keysByPlugin) {
        Map<String, Set<String>> copiedKeys = new LinkedHashMap<>();
        keysByPlugin.forEach((pluginName, keys) -> copiedKeys.put(pluginName, new LinkedHashSet<>(keys)));
        return new SaleState(true, copiedKeys);
    }
}
