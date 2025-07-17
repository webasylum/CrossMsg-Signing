FROM gradle:7.6.1-jdk17

# Create /app and set permissions
RUN mkdir -p /app && chown -R gradle:gradle /app && chmod -R 755 /app

USER gradle
WORKDIR /app

# Keep container running
CMD ["tail", "-f", "/dev/null"] 