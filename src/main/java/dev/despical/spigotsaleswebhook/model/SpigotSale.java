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
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
public record SpigotSale(
    String pluginName,
    String pluginUrl,
    String username,
    String userProfileUrl,
    ZonedDateTime purchaseDate,
    double price,
    String currency
) {

    public String buyerKey() {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
