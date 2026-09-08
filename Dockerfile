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
ENV JAVA_OPTS="-XX:MaxRAMPercentage=65.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.war"]
