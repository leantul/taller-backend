FROM maven:3.9.10-eclipse-temurin-21 AS build
WORKDIR /app
COPY .mvn ./.mvn
COPY mvnw ./mvnw
COPY pom.xml ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw --batch-mode -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
