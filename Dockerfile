FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app
COPY .mvn ./.mvn
COPY mvnw ./mvnw
COPY pom.xml ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw --batch-mode -DskipTests clean package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.war app.war
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=10.0 -XX:MaxRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
