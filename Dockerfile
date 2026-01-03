# Multi-stage build for optimal image size
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY bootstrap ./bootstrap
COPY love ./love
COPY twix-submodule ./twix-submodule
COPY .git ./.git

# Copy config files from submodule to bootstrap resources
RUN cp twix-submodule/*.yml bootstrap/src/main/resources/ || true

# Build the application
RUN gradle :bootstrap:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy the JAR file from builder stage
COPY --from=builder /app/bootstrap/build/libs/*.jar app.jar

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]