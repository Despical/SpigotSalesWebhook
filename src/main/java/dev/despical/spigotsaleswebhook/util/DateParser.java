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

package dev.despical.spigotsaleswebhook.util;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
@UtilityClass
public final class DateParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateParser.class);
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");

    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    public static ZonedDateTime parse(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return ZonedDateTime.now(ZONE);
        }

        ZonedDateTime now = ZonedDateTime.now(ZONE);
        String lower = dateStr.toLowerCase(Locale.ROOT).trim();

        try {
            if (lower.contains("ago")) {
                return parseAgo(lower, now);
            }

            if (lower.equals("yesterday")) {
                return now.minusDays(1).truncatedTo(ChronoUnit.DAYS);
            }

            if (lower.equals("today")) {
                return now.truncatedTo(ChronoUnit.DAYS);
            }

            if (dateStr.contains(" at ")) {
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
                return dateTime.atZone(ZONE);
            }

            LocalDate date = LocalDate.parse(dateStr, DATE_ONLY_FORMATTER);
            return date.atStartOfDay(ZONE);
        } catch (Exception _) {
            LOGGER.warn("Date could not be parsed: {}", dateStr);
            return now;
        }
    }

    private static ZonedDateTime parseAgo(String lower, ZonedDateTime now) {
        int amount = extractAmount(lower);

        if (lower.contains("second") || lower.contains("s ago")) {
            return now.minusSeconds(amount);
        }

        if (lower.contains("minute") || lower.contains("m ago")) {
            return now.minusMinutes(amount);
        }

        if (lower.contains("hour") || lower.contains("h ago")) {
            return now.minusHours(amount);
        }

        if (lower.contains("day")) {
            return now.minusDays(amount);
        }

        return now;
    }

    private static int extractAmount(String text) {
        Matcher matcher = NUMBER_PATTERN.matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        if (text.contains("an ") || text.contains("a ")) {
            return 1;
        }

        return 0;
    }
}
