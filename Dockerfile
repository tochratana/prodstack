# 1. Build stage - using JDK 25 if available
FROM gradle:8.6-jdk25-ea AS builder
#Note: -ea for early-access

WORKDIR /prod-stack

COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# 2. Runtime stage
FROM eclipse-temurin:25-jre AS runtime  # Also need JRE 25

WORKDIR /prod-stack

COPY --from=builder /prod-stack/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]