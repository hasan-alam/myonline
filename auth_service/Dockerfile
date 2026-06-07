# =============================================
# Dockerfile for auth_service
# myonline Authorization Microservice
# =============================================
# Multi-stage build using Java 17:
#   Stage 1 (builder): Compile and package the Spring Boot application with Maven
#   Stage 2 (runtime): Run the packaged JAR on a minimal JRE image
# =============================================

# ----------------------------
# Stage 1: Build
# ----------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

LABEL stage="builder"

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and POM first (layer caching: dependencies change less often than source)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy application source
COPY src src

# Build the application, skipping tests (tests should be run in CI pipeline separately)
RUN ./mvnw package -DskipTests -B

# ----------------------------
# Stage 2: Runtime
# ----------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="myonline Platform Team <admin@myonline.com>"
LABEL description="myonline Authorization Microservice"
LABEL version="0.0.1-SNAPSHOT"

# Create a non-root user for running the application (security best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the packaged JAR from the builder stage
COPY --from=builder /app/target/auth_service-0.0.1-SNAPSHOT.jar app.jar

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
