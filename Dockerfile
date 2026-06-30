FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /workspace

COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY src src

RUN chmod +x gradlew && ./gradlew --no-daemon installDist

FROM eclipse-temurin:25-jre-noble

WORKDIR /opt/spigot-sales-webhook

RUN useradd --create-home --home-dir /opt/spigot-sales-webhook --shell /usr/sbin/nologin spigotsales

COPY --from=build /workspace/build/install/SpigotSalesWebhook/ /opt/spigot-sales-webhook/

RUN mkdir -p /opt/spigot-sales-webhook/data && chown -R spigotsales:spigotsales /opt/spigot-sales-webhook

USER spigotsales

VOLUME ["/opt/spigot-sales-webhook/data"]

ENTRYPOINT ["/opt/spigot-sales-webhook/bin/SpigotSalesWebhook"]
