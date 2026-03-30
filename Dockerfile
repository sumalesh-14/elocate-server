# Multi-stage build for Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only dependency-related files first (layer cache)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw.cmd ./

RUN chmod +x mvnw

# Download all dependencies — this layer is cached unless pom.xml changes
RUN ./mvnw dependency:go-offline -B -q

# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B -q -Dspring-boot.run.profiles=production

# Runtime stage — lightweight JRE only
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/elocate-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=production -jar app.jar"]
