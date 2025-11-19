# Step 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -e -X clean package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081   # your backend port

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
