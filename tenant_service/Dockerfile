# =============================================================
# Dockerfile for myonline Tenant Microservice
# =============================================================
# Base image: Eclipse Temurin JRE 17 on Alpine (small footprint)
# Pre-build: Run 'mvn clean install' to generate the fat JAR
# =============================================================

FROM eclipse-temurin:17-jre-alpine

# Metadata labels
LABEL maintainer="myonline Platform Team <admin@myonline.com>"
LABEL description="myonline Tenant Microservice"
LABEL version="0.0.1-SNAPSHOT"

# Create a non-root user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory inside the container
WORKDIR /app

# Copy the built fat JAR from the Maven target directory
COPY target/tenant_service-0.0.1-SNAPSHOT.jar app.jar

# Transfer ownership of the JAR to the non-root user
RUN chown appuser:appgroup app.jar

# Switch to the non-root user
USER appuser

# Expose the service port
EXPOSE 8082

# JVM tuning options — can be overridden via environment variable
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
