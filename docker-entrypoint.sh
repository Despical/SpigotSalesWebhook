#!/bin/sh
set -e

mkdir -p /opt/spigot-sales-webhook/data
chown -R spigotsales:spigotsales /opt/spigot-sales-webhook/data

if [ "$#" -gt 0 ] && [ "${1#-}" != "$1" ]; then
    set -- /opt/spigot-sales-webhook/bin/SpigotSalesWebhook "$@"
fi

if command -v runuser >/dev/null 2>&1; then
    exec runuser -u spigotsales -- "$@"
fi

exec su -s /bin/sh spigotsales -c "$*"
