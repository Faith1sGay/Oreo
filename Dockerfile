FROM maven:3.6-jdk-11-slim AS BUILDER
WORKDIR /app
COPY ./pom.xml /app
COPY ./src /app/src
RUN mvn -q package

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=BUILDER /app/target/oreo-*.jar /app/oreo.jar
CMD ["java", "-jar", "oreo.jar"]