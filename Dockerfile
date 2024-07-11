# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21

# Copy the application JAR file to the container
COPY target/thanos-exporter-0.0.1-SNAPSHOT.jar /app/thanos-exporter-0.0.1-SNAPSHOT.jar

# Make port 8080 available to the world outside this container
# EXPOSE 8080

# Run the JAR file
CMD ["java", "-jar", "/app/thanos-exporter-0.0.1-SNAPSHOT.jar"]