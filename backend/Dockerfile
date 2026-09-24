
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN useradd --system --uid 1001 --create-home spring
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R spring:spring /app

USER spring
EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]