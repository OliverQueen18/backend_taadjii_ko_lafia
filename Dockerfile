FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create a new stage for runtime
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=0 /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
