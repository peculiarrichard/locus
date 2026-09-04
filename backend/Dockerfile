# syntax=docker/dockerfile:1

# Multi-stage build for one backend module; build with --build-arg MODULE=<path> --build-arg ARTIFACT=<artifact-id> and context=backend/.

FROM maven:3-eclipse-temurin-26 AS build
ARG MODULE
WORKDIR /app
COPY pom.xml pom.xml
COPY ${MODULE}/pom.xml ${MODULE}/pom.xml
RUN mvn -f ${MODULE}/pom.xml dependency:go-offline -q
COPY ${MODULE}/src ${MODULE}/src
RUN mvn -f ${MODULE}/pom.xml package -DskipTests -q

FROM eclipse-temurin:25-jre-alpine
ARG MODULE
ARG ARTIFACT
WORKDIR /app
COPY --from=build /app/${MODULE}/target/${ARTIFACT}-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
