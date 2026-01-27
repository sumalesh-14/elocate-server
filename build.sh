#!/bin/bash
# Render build script for Spring Boot application

echo "Starting build process..."

# Make mvnw executable
chmod +x mvnw

# Clean and build the project
./mvnw clean package -DskipTests

echo "Build completed successfully!"
