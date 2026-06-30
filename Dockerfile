FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /workspace

COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY src src

RUN chmod +x gradlew && ./gradlew --no-daemon installDist

FROM eclipse-temurin:25-jre-noble

WORKDIR /opt/spigot-sales-webhook

ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"

ARG APP_UID=1001
ARG APP_GID=1001

RUN groupadd --gid ${APP_GID} spigotsales \
    && useradd --uid ${APP_UID} --gid ${APP_GID} --create-home --home-dir /opt/spigot-sales-webhook --shell /usr/sbin/nologin spigotsales

COPY --from=build /workspace/build/install/SpigotSalesWebhook/ /opt/spigot-sales-webhook/
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN mkdir -p /opt/spigot-sales-webhook/data \
    && chown -R spigotsales:spigotsales /opt/spigot-sales-webhook \
    && chmod +x /usr/local/bin/docker-entrypoint.sh

USER root

VOLUME ["/opt/spigot-sales-webhook/data"]

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["/opt/spigot-sales-webhook/bin/SpigotSalesWebhook"]
