FROM gradle:7.6.1-jdk17

# Create /app and set permissions
RUN mkdir -p /app && chown -R gradle:gradle /app && chmod -R 755 /app

# Install additional tools for debugging
RUN apt-get update && apt-get install -y \
    curl \
    vim \
    tree \
    && rm -rf /var/lib/apt/lists/*

USER gradle
WORKDIR /app

# Keep container running and add some debugging
CMD ["tail", "-f", "/dev/null"] 