FROM maven:3.6-openjdk-15 AS BUILDER
WORKDIR /app
COPY ./pom.xml /app
RUN mvn -B -q dependency:go-offline
COPY ./src /app/src
RUN mvn -q package

FROM openjdk:15-alpine
WORKDIR /app
COPY --from=BUILDER /app/target/oreo-*.jar /app/oreo.jar
CMD ["java", "-jar", "oreo.jar"]