# Single-stage Dockerfile for learning purposes
FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Expose the port and run the application
EXPOSE 8080
CMD ["sh", "-c", "java -jar target/*.jar"]