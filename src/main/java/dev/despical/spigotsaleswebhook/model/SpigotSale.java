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

package dev.despical.spigotsaleswebhook.model;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
public record SpigotSale(
    String pluginName,
    String pluginUrl,
    String username,
    String previousUsername,
    String userProfileUrl,
    ZonedDateTime purchaseDate,
    double price,
    String currency
) {

    private static final Pattern MEMBER_ID_PATTERN = Pattern.compile(
        "/members/(?:[^/?#]*\\.)?(\\d+)(?:/|$)",
        Pattern.CASE_INSENSITIVE
    );

    public String buyerKey() {
        if (userProfileUrl != null) {
            Matcher matcher = MEMBER_ID_PATTERN.matcher(userProfileUrl);

            if (matcher.find()) {
                return "spigot-user:" + matcher.group(1);
            }
        }

        return normalizedUsername(username);
    }

    public Set<String> legacyBuyerKeys() {
        Set<String> keys = new LinkedHashSet<>();
        addUsernameKey(keys, username);
        addUsernameKey(keys, previousUsername);
        return keys;
    }

    public boolean wasSeen(Set<String> seenBuyerKeys) {
        if (seenBuyerKeys.contains(buyerKey())) {
            return true;
        }

        return legacyBuyerKeys().stream().anyMatch(seenBuyerKeys::contains);
    }

    private static void addUsernameKey(Set<String> keys, String value) {
        if (value != null && !value.isBlank()) {
            keys.add(normalizedUsername(value));
        }
    }

    private static String normalizedUsername(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
