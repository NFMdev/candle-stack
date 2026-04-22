# FROM maven:3.9-eclipse-temurin-25 AS build
# WORKDIR /app

# COPY . .
# RUN mvn -pl ingestion-service -am clean package -DskipTests

# FROM eclipse-temurin:25-jre
# WORKDIR /app

# COPY --from=build /app/ingestion-service/target/*.jar app.jar

# ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]