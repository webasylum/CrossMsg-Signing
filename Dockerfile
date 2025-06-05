# Build stage
FROM gradle:8.6-jdk17 AS builder

# Add labels
LABEL org.opencontainers.image.title="TSG Cross Message Signing"
LABEL org.opencontainers.image.description="ISO 20022 message signing and validation service"
LABEL org.opencontainers.image.vendor="TSG"
LABEL com.tsg.project="tsg-crossmsg-signing"
LABEL com.tsg.version="1.0.0"

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the application
RUN gradle build --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre

# Add labels
LABEL org.opencontainers.image.title="TSG Cross Message Signing"
LABEL org.opencontainers.image.description="ISO 20022 message signing and validation service"
LABEL org.opencontainers.image.vendor="TSG"
LABEL com.tsg.project="tsg-crossmsg-signing"
LABEL com.tsg.version="1.0.0"

# Install only necessary packages
RUN apk add --no-cache netcat-openbsd

# Set working directory
WORKDIR /app

# Copy health check script
COPY scripts/healthcheck.sh /usr/local/bin/healthcheck.sh
RUN chmod +x /usr/local/bin/healthcheck.sh

# Copy only the built artifacts from the builder stage
COPY --from=builder /app/build/libs/*.jar ./app.jar

# Set health check
HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/health || exit 1

# Default command
CMD ["java", "-jar", "app.jar"]

# Development stage
FROM gradle:8.6-jdk17

LABEL org.opencontainers.image.title="TSG Cross Message Signing"
LABEL org.opencontainers.image.description="ISO 20022 message signing and validation service"
LABEL org.opencontainers.image.vendor="TSG"
LABEL com.tsg.version="1.0.0"

WORKDIR /app

# Copy build files
COPY build.gradle settings.gradle gradle.properties ./
COPY src ./src

# Install curl for healthcheck
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Build the application
RUN gradle build --no-daemon

# Keep container running for development
CMD ["tail", "-f", "/dev/null"] 