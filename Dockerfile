# ============================================================
#  TrackMyWallet - Multi-stage Production Dockerfile
#  Base: Java 21 (Eclipse Temurin)
# ============================================================

# ------------------------------------------------------------
# Stage 1: Build the Application
# ------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copy Maven wrapper files and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalize line endings and grant execute permissions on mvnw
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Cache Maven dependencies (offline mode)
RUN ./mvnw dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build production executable JAR (skip tests during container build as DB is external)
RUN ./mvnw clean package -DskipTests -B

# ------------------------------------------------------------
# Stage 2: Production Runtime
# ------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for health checking and remove apt cache
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Run as non-root user for container security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy compiled JAR from builder stage
COPY --from=builder --chown=spring:spring /build/target/trackmywallet-0.0.1-SNAPSHOT.jar app.jar

# Switch to non-root user
USER spring:spring

# Default port (Render overrides this with $PORT)
EXPOSE 8080

# Configure JVM flags optimized for container environments (e.g. Render 512MB RAM)
ENV PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Container healthcheck using the /api/health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD curl -f http://localhost:${PORT}/api/health || exit 1

# Launch the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
