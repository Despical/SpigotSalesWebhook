# Security Policy

Security reports are taken seriously. If you find a vulnerability in SpigotSalesWebhook, please report it privately instead of opening a public issue.

## Reporting a Vulnerability

Please send security reports to:

```text
contact@despical.dev
```

When possible, include the following details:

* A clear description of the vulnerability.
* Steps to reproduce the issue.
* The affected version, commit, branch, operating system, and Java version.
* Relevant sanitized logs or configuration snippets.
* Whether the issue involves Discord webhook delivery, Spigot cookie handling, state persistence, config loading, or buyer scraping.

Please do not include real Discord webhook URLs, Spigot cookies, private buyer data, or destructive payloads.

## Scope

The following areas are considered security-sensitive:

* Leaked Discord webhook URLs or SpigotMC cookies.
* Unsafe handling of `src/main/resources/config.yml`.
* Buyer-list scraping behavior that could expose private purchase data.
* State file corruption or unintended disclosure of buyer names.
* Discord webhook payload injection or unwanted mentions.
* Dependency, build, or packaging issues that expose secrets.

Reports about ordinary bugs, scraper selector drift, missing purchases, or feature requests should use the normal GitHub issue tracker instead.

## Supported Versions

Only the latest public version of SpigotSalesWebhook is currently supported.

## Response

After a valid report is received, the issue will be reviewed as soon as possible. If confirmed, a fix will be prepared privately and released with credit where appropriate.

Please avoid public disclosure until a fix is available.
