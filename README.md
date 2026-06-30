# SpigotSalesWebhook

[![](https://github.com/Despical/SpigotSalesWebhook/actions/workflows/build.yaml/badge.svg)](https://github.com/Despical/SpigotSalesWebhook/actions/workflows/build.yaml)
[![](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Java 25](https://img.shields.io/badge/Java-25-007396.svg)](https://www.java.com/)

SpigotSalesWebhook is a lightweight Java worker that watches SpigotMC premium resource buyer lists and sends Discord webhook notifications when new buyers appear.

It is designed for a simple VDS-style setup: configure your Spigot cookie, Discord webhook URL, and resource buyer-list URLs once, then leave the worker running. The first scan records the current buyers without sending old purchases; later scans only notify for new buyers.

---

## Features

* **Hourly buyer scans:** Polls configured Spigot resource buyer pages on a configurable interval.
* **Discord embeds:** Sends clean webhook embeds with buyer, plugin, price, currency, and purchase time.
* **First-run baseline:** Records existing buyers on the first run without spamming Discord.
* **Plugin-based state:** Tracks seen buyers per plugin in `data/seen-sales.json`.
* **Single config file:** Uses `src/main/resources/config.yml`; no `.env` file is required.
* **Self-hostable:** Runs as a standalone Java process or Docker container.

---

## Requirements

* Java 25
* A Discord webhook URL
* A valid SpigotMC cookie with access to your premium resource buyer pages

---

## Configuration

Copy the example config and fill in your private values:

```bash
cp src/main/resources/config.example.yml src/main/resources/config.yml
```

Windows PowerShell:

```powershell
Copy-Item src/main/resources/config.example.yml src/main/resources/config.yml
```

`src/main/resources/config.yml` is ignored by git. Do not commit your webhook URL or Spigot cookie.

Example:

```yaml
discord:
  webhook-url: "https://discord.com/api/webhooks/..."
  username: "Spigot Sales"

spigot:
  cookie: "xf_session=...; xf_user=...; xf_tfa_trust=..."
  request-delay-ms: 800
  plugins:
    - name: "Plugin Name"
      buyer-list-url: "https://www.spigotmc.org/resources/plugin-name.plugin-id/buyers"

scan:
  interval-minutes: 60
  notify-existing-on-first-run: false
  state-file: "data/seen-sales.json"
```

---

## Building From Source

Windows:

```bat
gradlew.bat clean jar
```

Linux / macOS:

```bash
./gradlew clean jar
```

---

## Running

Run continuously:

```bash
java -jar build/libs/spigot-sales-webhook-1.0.0.jar
```

Run one scan and exit:

```bash
java -jar build/libs/spigot-sales-webhook-1.0.0.jar --once
```

Run one scan and notify existing buyers too:

```bash
java -jar build/libs/spigot-sales-webhook-1.0.0.jar --once --notify-existing
```

---

## Docker

Docker is the recommended way to keep the worker running on a server.

1. Create `src/main/resources/config.yml` from `src/main/resources/config.example.yml` and fill in your Discord webhook URL and Spigot cookie.
2. Start the container:

```bash
docker compose up -d --build
```

3. Check logs:

```bash
docker compose logs -f spigot-sales-webhook
```

4. Stop the worker:

```bash
docker compose down
```

The compose file mounts `src/main/resources/config.yml` into `/opt/spigot-sales-webhook/config.yml` as read-only and mounts `./data` into `/opt/spigot-sales-webhook/data`, so seen buyer state survives rebuilds and restarts.

The Docker image does not copy your real `src/main/resources/config.yml` into the image. Keep private webhook and cookie values only in your local config file.

---

## State File

Seen buyers are stored per plugin:

```json
{
  "initialized": true,
  "seenSalesByPlugin": {
    "Plugin Name": [
      "examplebuyer"
    ]
  }
}
```

Delete the configured state file if you want the worker to rebuild its baseline from scratch.

---

## Security

We prioritize user privacy and application integrity. Please do not open public issues for discovered vulnerabilities.

Read our [SECURITY.md](../SECURITY.md) for responsible disclosure reporting.

---

## Contributing

We welcome Pull Requests from the community. To help us maintain clean project history and formatting, please follow these guidelines:

* **No tabs:** Use spaces exclusively for indentation.
* **Style consistency:** Respect the established code architecture and style templates.
* **Version control cleanliness:** Do not increment project version numbers in example configurations within your PR.
* **Minimal diffs:** Disable automated reformat-on-save settings that affect untouched files.

Learn more via our formal [Contribution Guidelines](../CONTRIBUTING.md).

---

## License

This project is licensed under the [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html).

See the [LICENSE](../LICENSE) file for comprehensive copyright notices and third-party attributions.
