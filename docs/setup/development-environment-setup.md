# Development Environment Setup Guide

## Overview

This guide provides step-by-step instructions for setting up the development environment for the TSG CrossMsg Signing project, which consists of:

1. **Core Project**: `Iso20022KvpParser` with sophisticated KVP extraction and signature strategies
2. **WebUI Sub-Project**: `CrossMsg-Signing-WebUI` demonstration interface

## Prerequisites

### Required Software
- **Windows 11** with latest updates
- **Docker Desktop** for Windows (with WSL2 backend)
- **VS Code** or **Cursor IDE** with Dev Containers extension
- **Git for Windows**
- **PowerShell 7+** (included in Windows 11)

### System Requirements
- **RAM**: Minimum 8GB, recommended 16GB
- **Storage**: At least 10GB free space
- **CPU**: Multi-core processor (Intel/AMD)

## Development Environment Options

### Option 1: Core Project Development (Recommended for Parser Work)

**Use Case**: Working on the `Iso20022KvpParser`, signature strategies, and running unit tests.

#### Setup Steps

1. **Open VS Code/Cursor IDE**
2. **Open Project Folder**:
   ```powershell
   # Navigate to project root
   cd C:\Projects\TSG-CrossMsg-Signing
   ```
3. **Open in Dev Container**:
   - Press `Ctrl+Shift+P`
   - Type: `Dev Containers: Open Folder in Container`
   - Select the project folder
   - Wait for container to build (may take 5-10 minutes first time)

4. **Verify Setup**:
   ```bash
   # Check workspace mounting
   ls -la /app
   
   # Verify Java installation
   java -version
   
   # Verify Gradle installation
   gradle --version
   
   # Check project structure
   ls -la /app/src
   ```

5. **Run Core Tests**:
   ```bash
   # Run all tests
   gradle test
   
   # Run specific KVP parser tests
   gradle test --tests "*Iso20022KvpParser*Test"
   
   # Run signature strategy tests
   gradle test --tests "*XmlC14n*Test"
   gradle test --tests "*Jws*Test"
   gradle test --tests "*Hybrid*Test"
   ```

#### Development Workflow

```bash
# Build the project
gradle build

# Run tests with verbose output
gradle test --info

# Clean and rebuild
gradle clean build

# Run specific test class
gradle test --tests "com.tsg.crossmsg.signing.test.Iso20022KvpParserXmlToJsonXmlC14nTest"
```

### Option 2: Full Application Development (WebUI + Core)

**Use Case**: Working on both the core parser and the WebUI demonstration interface.

#### Setup Steps

1. **Start WebUI Application**:
   ```powershell
   # Navigate to WebUI directory
   cd C:\Projects\TSG-CrossMsg-Signing\CrossMsg-Signing-WebUI
   
   # Start the full application stack
   docker-compose up -d
   ```

2. **Verify WebUI Startup**:
   ```powershell
   # Check container status
   docker-compose ps
   
   # View logs
   docker-compose logs -f
   ```

3. **Access WebUI**:
   - **Frontend**: Open browser to `http://localhost:3000`
   - **Backend API**: `http://localhost:8080`
   - **Nginx Proxy**: `http://localhost:80` (if configured)

4. **For Core Development** (in separate terminal):
   ```powershell
   # Navigate to project root
   cd C:\Projects\TSG-CrossMsg-Signing
   
   # Use VS Code Dev Container for core development
   # (Follow Option 1 steps)
   ```

#### WebUI Development Workflow

```powershell
# Start WebUI stack
cd CrossMsg-Signing-WebUI
docker-compose up -d

# View logs
docker-compose logs -f

# Stop WebUI stack
docker-compose down

# Rebuild and restart
docker-compose down
docker-compose up --build -d
```

## Container Configuration

### Core Project Dev Container

**File**: `.devcontainer/devcontainer.json`

**Features**:
- Java 17 (Eclipse Temurin)
- Gradle 7.6.1
- Debugging tools (curl, vim, tree)
- Proper Java path configuration (`/opt/java/openjdk`)
- Workspace mounted at `/app`

### WebUI Docker Compose

**File**: `CrossMsg-Signing-WebUI/docker-compose.yml`

**Services**:
- **backend**: Spring Boot application using core parser
- **frontend**: React application for UI
- **nginx**: Reverse proxy for production deployment

## Testing Strategy

### Core Project Testing

```bash
# Run all tests
gradle test

# Run specific test categories
gradle test --tests "*Iso20022KvpParser*Test"    # KVP parser tests
gradle test --tests "*XmlC14n*Test"              # XML C14N + XMLDSig tests
gradle test --tests "*Jws*Test"                  # RFC 8785 + JWS tests
gradle test --tests "*Hybrid*Test"               # Hybrid/Detached Hash tests

# Run with verbose output
gradle test --info --debug

# Generate test reports
gradle test jacocoTestReport
```

### WebUI Testing

1. **Functional Testing**:
   - Start WebUI: `docker-compose up -d`
   - Open browser: `http://localhost:3000`
   - Test message editing functionality
   - Test signature strategy buttons
   - Verify tampering detection

2. **API Testing**:
   - Test backend endpoints: `http://localhost:8080`
   - Verify parser integration
   - Test signature validation

## Troubleshooting

### Common Issues

#### Core Project Issues

**Dev Container Not Loading**:
```powershell
# Rebuild container without cache
# In VS Code: Ctrl+Shift+P → "Dev Containers: Rebuild Container Without Cache"
```

**Java Path Issues**:
```bash
# Verify Java installation in container
java -version
echo $JAVA_HOME
which java
```

**Tests Failing**:
```bash
# Check container logs
docker-compose logs dev

# Clean and rebuild
gradle clean build
```

#### WebUI Issues

**Port Conflicts**:
```powershell
# Check what's using the ports
netstat -ano | findstr :3000
netstat -ano | findstr :8080

# Stop conflicting services or change ports in docker-compose.yml
```

**Container Startup Failures**:
```powershell
# Check container logs
docker-compose logs

# Check container status
docker-compose ps

# Rebuild containers
docker-compose down
docker-compose up --build -d
```

**Parser Integration Issues**:
```powershell
# Verify backend is using core project classes
docker-compose exec backend ls -la /app/src/main/java/com/tsg/crossmsg/signing/
```

### Performance Optimization

#### Docker Desktop Settings
- **Memory**: Allocate at least 8GB RAM
- **CPU**: Allocate at least 4 cores
- **Disk**: Enable WSL2 backend for better performance

#### Gradle Optimization
```bash
# Enable Gradle daemon
gradle --daemon

# Use parallel builds
gradle build --parallel

# Increase memory for Gradle
export GRADLE_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
```

## Development Best Practices

### Code Organization
- **Core Project**: Keep parser logic separate from UI concerns
- **WebUI**: Focus on presentation and user interaction
- **Testing**: Maintain separation between unit tests and integration tests

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/parser-improvement

# Make changes and test
gradle test

# Commit changes
git add .
git commit -m "Improve KVP parser accuracy"

# Push and create pull request
git push origin feature/parser-improvement
```

### Documentation
- Update README.md for significant changes
- Document new signature strategies
- Update test documentation
- Maintain architecture diagrams

## Next Steps

1. **Start with Core Development**: Use Option 1 to work on the `Iso20022KvpParser`
2. **Test Signature Strategies**: Run comprehensive tests for all three strategies
3. **Integrate with WebUI**: Use Option 2 to test full application functionality
4. **Contribute Improvements**: Follow best practices for code organization and testing

## Support

For issues with:
- **Core Project**: Check container logs and test output
- **WebUI**: Check Docker Compose logs and browser console
- **Environment**: Verify Docker Desktop and VS Code Dev Containers setup
- **Performance**: Optimize Docker Desktop settings and Gradle configuration 