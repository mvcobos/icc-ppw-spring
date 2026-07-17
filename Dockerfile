# ETAPA 1: BUILD
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
COPY src ./src
RUN ./gradlew build -x test --no-daemon

# ETAPA 2: RUNTIME
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]