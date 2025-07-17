#!/bin/bash

echo "=== TSG CrossMsg Signing Dev Container Setup ==="
echo "Checking Java installation..."
java -version
echo "JAVA_HOME: $JAVA_HOME"
echo "Java location: $(which java)"

echo ""
echo "Checking Gradle installation..."
gradle --version

echo ""
echo "Checking workspace..."
ls -la /app

echo ""
echo "Checking project structure..."
if [ -f "/app/build.gradle" ]; then
    echo "✅ build.gradle found"
else
    echo "❌ build.gradle not found"
fi

if [ -d "/app/src" ]; then
    echo "✅ src directory found"
    ls -la /app/src
else
    echo "❌ src directory not found"
fi

echo ""
echo "=== Setup Complete ===" 