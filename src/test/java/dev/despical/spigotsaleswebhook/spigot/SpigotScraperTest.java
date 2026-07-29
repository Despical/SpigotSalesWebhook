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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
class SpigotScraperTest {

    private final SpigotScraper scraper = new SpigotScraper(new AppConfig.SpigotSettings("", 0, List.of()));

    @Test
    void extractsPreviousUsernameFromBuyerBlurb() {
        Element item = Jsoup.parse("""
            <li class="primaryContent memberListItem">
                <div class="userBlurb">Previously OldDespical, Male</div>
            </li>
            """).selectFirst("li");

        assertEquals("OldDespical", scraper.previousUsername(item));
    }

    @Test
    void returnsNullWhenBuyerHasNotChangedUsername() {
        Element item = Jsoup.parse("""
            <li class="primaryContent memberListItem">
                <div class="userBlurb">Male</div>
            </li>
            """).selectFirst("li");

        assertNull(scraper.previousUsername(item));
    }

    @Test
    void detectsSpigotLoginPage() {
        var document = Jsoup.parse(
            "<form action=\"/login/login\"><input name=\"login\"></form>",
            "https://www.spigotmc.org/resources/example.123/buyers"
        );

        assertTrue(scraper.isAuthenticationPage(document));
    }

    @Test
    void detectsSpigotPermissionPage() {
        var document = Jsoup.parse(
            "<main>You do not have permission to view this page or perform this action.</main>",
            "https://www.spigotmc.org/resources/example.123/buyers"
        );

        assertTrue(scraper.isAuthenticationPage(document));
    }

    @Test
    void acceptsAuthenticatedBuyerPage() {
        var document = Jsoup.parse(
            "<li class=\"primaryContent memberListItem\"><a class=\"username\">Despical</a></li>",
            "https://www.spigotmc.org/resources/example.123/buyers"
        );

        assertFalse(scraper.isAuthenticationPage(document));
    }
}
