#!/bin/bash
# Render start script for Spring Boot application

echo "Starting Elocate API..."

# Run the Spring Boot application with production profile
java -Dspring.profiles.active=production \
     -Xmx512m \
     -Xms256m \
     -jar target/elocate-0.0.1-SNAPSHOT.jar
