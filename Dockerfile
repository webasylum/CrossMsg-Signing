# Build stage
FROM gradle:7.6.1-jdk17-alpine AS builder

# Add labels
LABEL org.opencontainers.image.title="TSG Cross Message Signing"
LABEL org.opencontainers.image.description="ISO 20022 message signing and validation service"
LABEL org.opencontainers.image.vendor="TSG"
LABEL com.tsg.project="tsg-crossmsg-signing"
LABEL com.tsg.version="1.0.0"

# Set working directory
WORKDIR /app

# Copy only the files needed for building
COPY build.gradle settings.gradle ./
COPY src ./src

# Build the application
RUN gradle build --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

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
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Set health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD /usr/local/bin/healthcheck.sh

# Default command
CMD ["java", "-jar", "app.jar"] 