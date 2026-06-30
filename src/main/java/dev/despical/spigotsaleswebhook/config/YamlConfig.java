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

import lombok.RequiredArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2026
 */
@RequiredArgsConstructor
public final class YamlConfig {

    private final Map<String, Object> values;

    public static YamlConfig empty() {
        return new YamlConfig(new LinkedHashMap<>());
    }

    public static YamlConfig loadRequired(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalStateException("Missing config file: " + path);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            return fromInputStream(inputStream, path.toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read YAML file: " + path, exception);
        }
    }

    public static YamlConfig loadExternalOrResource(Path externalPath, String resourceName) {
        if (Files.exists(externalPath)) {
            return loadRequired(externalPath);
        }

        return loadRequiredResource(resourceName);
    }

    public static YamlConfig loadRequiredResource(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing config resource: " + resourceName + ". Copy src/main/resources/config.example.yml to src/main/resources/config.yml and fill it.");
            }

            return fromInputStream(inputStream, "classpath:" + resourceName);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read YAML resource: " + resourceName, exception);
        }
    }

    public String getString(String path) {
        Object value = get(path);
        return value == null ? null : String.valueOf(value);
    }

    public long getLong(String path) {
        Object value = get(path);
        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(String.valueOf(value));
    }

    public boolean getBoolean(String path) {
        Object value = get(path);
        if (value instanceof Boolean bool) {
            return bool;
        }

        return Boolean.parseBoolean(String.valueOf(value));
    }

    public List<Map<String, Object>> mapList(String path) {
        Object value = get(path);

        if (value instanceof List<?> list) {
            return list.stream()
                .filter(Map.class::isInstance)
                .map(YamlConfig::castMap)
                .toList();
        }

        return Collections.emptyList();
    }

    private static YamlConfig fromInputStream(InputStream inputStream, String sourceName) {
        Object loaded = new Yaml().load(inputStream);

        if (loaded == null) {
            return empty();
        }

        if (loaded instanceof Map<?, ?> map) {
            return new YamlConfig(castMap(map));
        }

        throw new IllegalStateException("YAML root must be an object: " + sourceName);
    }

    private Object get(String path) {
        Object current = values;

        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }

            current = map.get(part);
        }

        return current;
    }

    private static Map<String, Object> castMap(Object rawMap) {
        Map<?, ?> map = (Map<?, ?>) rawMap;
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        return result;
    }
}
