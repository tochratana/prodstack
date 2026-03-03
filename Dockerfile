## 1. Build stage
#FROM gradle:8.6-jdk21 AS builder
#
#WORKDIR /prod-stack
#
#COPY gradle gradle
#COPY gradlew .
#COPY build.gradle settings.gradle ./
#
#RUN chmod +x gradlew
#
#COPY src src
#
#RUN ./gradlew bootJar --no-daemon
#
#
## 2. Runtime stage
#FROM eclipse-temurin:21-jre
#
#WORKDIR /prod-stack
#
#COPY --from=builder /prod-stack/build/libs/*.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]



# 1. Build stage
FROM gradle:8.6-jdk21 AS builder

WORKDIR /prod-stack

# Copy all files
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Make executable and debug
RUN chmod +x gradlew && \
    echo "=== Gradle Version ===" && \
    ./gradlew --version && \
    echo "=== Files in directory ===" && \
    ls -la && \
    echo "=== Source files ===" && \
    ls -la src/

# Try to build
RUN ./gradlew bootJar --no-daemon --stacktrace || true

# 2. Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /prod-stack

COPY --from=builder /prod-stack/build/libs/*.jar app.jar || true

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]