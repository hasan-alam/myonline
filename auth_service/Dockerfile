# =============================================
# Dockerfile for auth_service
# myonline Authorization Microservice
# =============================================
# Pre-built JAR approach:
#   Run "mvn clean install" locally before building this image.
#   The fat JAR from target/ is copied directly into the runtime image.
# =============================================

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="myonline Platform Team <admin@myonline.com>"
LABEL description="myonline Authorization Microservice"
LABEL version="0.0.1-SNAPSHOT"

# Create a non-root user for running the application (security best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the pre-built fat JAR (run "mvn clean install" first)
COPY target/auth_service-0.0.1-SNAPSHOT.jar app.jar

# Set ownership to non-root user
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose the application port (must match server.port in application.properties)
EXPOSE 8081

# JVM options:
#   -Xmx512m      : Maximum heap size
#   -Xms256m      : Initial heap size
#   -XX:+UseG1GC  : Use G1 garbage collector (recommended for microservices)
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
