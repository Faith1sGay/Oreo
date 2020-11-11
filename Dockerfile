FROM maven:3.6-jdk-11-slim AS BUILDER
WORKDIR /app
COPY ./pom.xml /app
RUN mvn -B -q dependency:go-offline
COPY ./src /app/src
RUN mvn -q package

FROM adoptopenjdk/openjdk12:alpine-jre
WORKDIR /app
COPY --from=BUILDER /app/target/oreo-*.jar /app/oreo.jar
CMD ["java", "-jar", "oreo.jar"]