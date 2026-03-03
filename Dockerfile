# 1. Build stage
FROM gradle:8.6-jdk21 AS builder

WORKDIR /prod-stack

# Copy all files
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Make gradlew executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew bootJar --no-daemon

# Debug: List all files to see where the JAR is
RUN echo "=== Listing build/libs directory ===" && \
    ls -la build/libs/ || echo "build/libs/ not found!" && \
    echo "=== Searching for JAR files ===" && \
    find . -name "*.jar" -type f

# 2. Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /prod-stack

# Debug: List what's available from builder
RUN echo "=== Files in builder ==="
COPY --from=builder /prod-stack/build/libs/ /tmp/build-libs/
RUN ls -la /tmp/build-libs/

# Copy the JAR (with debug)
COPY --from=builder /prod-stack/build/libs/*.jar app.jar || echo "Failed to copy JAR!"

# Verify the copy
RUN ls -la app.jar || echo "app.jar not found!"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]