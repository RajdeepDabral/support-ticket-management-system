FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=10s --start-period=120s --retries=15 \
  CMD curl -sf --max-time 5 http://127.0.0.1:8080/api/v1/tickets || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
