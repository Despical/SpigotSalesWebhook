# Contributing

Thanks for taking the time to improve SpigotSalesWebhook.

Before making a larger change, please open an issue to discuss the behavior, configuration impact, or deployment implications.

## Pull Request Process

* Use Java 25.
* Keep changes focused and avoid unrelated formatting churn.
* Use spaces for indentation; do not use tabs.
* Respect the existing package layout:
  * `config` for YAML/config binding.
  * `discord` for webhook delivery.
  * `model` for data records.
  * `service` for orchestration.
  * `spigot` for scraping.
  * `state` for seen-buyer persistence.
  * `util` for shared helpers.
* Do not commit real `src/main/resources/config.yml` values.
* Update `src/main/resources/config.example.yml` when adding or changing config keys.
* Keep `README.md` and issue templates aligned with user-facing behavior.
* Run the build before opening a PR:

```bash
./gradlew clean jar
```

Windows:

```bat
gradlew.bat clean jar
```

If you touch Docker-related files, also verify the image build:

```bash
docker build -t spigot-sales-webhook .
```

## Issue Process

When submitting an issue:

* Search existing issues first.
* Use the provided bug or feature template when it fits.
* Include Java version, operating system, run command, relevant logs, and sanitized config details.
* Never paste your Discord webhook URL, Spigot cookie, or full private `config.yml`.
* Keep support questions separate from actionable bugs when possible.

## Additional Resources

* [General GitHub documentation](https://help.github.com/)
* [GitHub pull request documentation](https://help.github.com/articles/creating-a-pull-request/)
