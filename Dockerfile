# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# dependencies resolve in their own layer so a source edit does not re-download them
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- run ----------
# jammy rather than alpine: the temurin alpine images have no arm64 build
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# the app never writes to disk, so it can run as an unprivileged user
RUN useradd --system --create-home --shell /usr/sbin/nologin chess
COPY --from=build /build/target/*.jar app.jar
USER chess

EXPOSE 8080
ENV PORT=8080
# the bot's transposition table wants headroom; let the JVM use most of the container
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=30s --timeout=3s --start-period=25s --retries=3 \
    CMD curl -fsS "http://localhost:${PORT}/api/chess/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
