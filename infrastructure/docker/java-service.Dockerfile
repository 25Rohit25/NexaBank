FROM maven:3.9.11-eclipse-temurin-21 AS build

ARG MODULE
WORKDIR /workspace
COPY . .
RUN mvn -B -pl "${MODULE}" -am -DskipTests package

FROM eclipse-temurin:21-jre

ARG MODULE
RUN useradd --system --uid 10001 --create-home nexa
WORKDIR /app
COPY --from=build --chown=nexa:nexa /workspace/${MODULE}/target/*.jar /app/app.jar
USER nexa
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
