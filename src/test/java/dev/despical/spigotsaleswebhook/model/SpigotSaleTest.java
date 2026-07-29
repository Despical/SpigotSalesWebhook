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

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
class SpigotSaleTest {

    @Test
    void usesStableMemberIdWhenUsernameChanges() {
        SpigotSale originalSale = sale("OldDespical", null, "https://www.spigotmc.org/members/olddespical.615094/");
        SpigotSale renamedSale = sale("Despical", "OldDespical", "https://www.spigotmc.org/members/despical.615094/");

        assertEquals("spigot-user:615094", originalSale.buyerKey());
        assertEquals(originalSale.buyerKey(), renamedSale.buyerKey());
    }

    @Test
    void recognizesLegacyStateUsingCurrentOrPreviousUsername() {
        SpigotSale renamedSale = sale("Despical", "OldDespical", "https://www.spigotmc.org/members/despical.615094/");

        assertTrue(renamedSale.wasSeen(Set.of("olddespical")));
        assertTrue(renamedSale.wasSeen(Set.of("despical")));
        assertTrue(renamedSale.wasSeen(Set.of("spigot-user:615094")));
        assertFalse(renamedSale.wasSeen(Set.of("another-user")));
    }

    @Test
    void fallsBackToNormalizedUsernameWithoutMemberId() {
        SpigotSale sale = sale("  Despical  ", null, "");

        assertEquals("despical", sale.buyerKey());
    }

    private SpigotSale sale(String username, String previousUsername, String profileUrl) {
        return new SpigotSale(
            "Advanced Parkour",
            "https://www.spigotmc.org/resources/advanced-parkour.123/",
            username,
            previousUsername,
            profileUrl,
            ZonedDateTime.parse("2026-05-13T12:00:00Z"),
            4.99,
            "USD"
        );
    }
}
