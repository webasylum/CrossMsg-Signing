# Build stage
FROM gradle:8.6-jdk17 AS builder

# Add labels
LABEL org.opencontainers.image.title="TSG Cross Message Signing"
LABEL org.opencontainers.image.description="ISO 20022 message signing and validation service"
LABEL org.opencontainers.image.vendor="TSG"
LABEL com.tsg.project="tsg-crossmsg-signing"

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

# Install system dependencies and Python
RUN apt-get update && apt-get install -y \
    curl \
    python3 \
    python3-pip \
    python3-dev \
    build-essential \
    git \
    && rm -rf /var/lib/apt/lists/* \
    && python3 -m pip install --upgrade pip

# Create a non-root user for development
RUN useradd -m -s /bin/bash developer \
    && chown -R developer:developer /app \
    && mkdir -p /home/developer/.local \
    && chown -R developer:developer /home/developer/.local

# Set up Python environment
ENV PYTHONPATH=/app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PATH="/home/developer/.local/bin:${PATH}"

# Install base Python packages as root
RUN pip3 install --no-cache-dir \
    python-docx \
    office-word-mcp-server \
    pylint \
    autopep8 \
    black \
    mypy \
    pytest \
    pytest-cov \
    pip-tools

# Create a requirements directory for project-specific dependencies
RUN mkdir -p /app/requirements \
    && chown -R developer:developer /app/requirements

# Switch to non-root user
USER developer

# Build the application
RUN gradle build --no-daemon

# Keep container running for development
CMD ["tail", "-f", "/dev/null"] 