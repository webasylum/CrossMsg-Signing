#!/bin/bash

# Check if Gradle is available
if ! command -v gradle &> /dev/null; then
    echo "Gradle is not installed"
    exit 1
fi

# Check if Java is available and version is correct
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [[ $java_version != 17* ]]; then
    echo "Java version $java_version is not compatible (required: 17)"
    exit 1
fi

# Check if required ports are available
if ! nc -z localhost 5005 &> /dev/null; then
    echo "Debug port 5005 is not available"
    exit 1
fi

# Check if Gradle daemon is running
if ! gradle --status &> /dev/null; then
    echo "Gradle daemon is not running"
    exit 1
fi

# Check if project can be built
if ! gradle --no-daemon classes &> /dev/null; then
    echo "Project build failed"
    exit 1
fi

echo "Health check passed"
exit 0 