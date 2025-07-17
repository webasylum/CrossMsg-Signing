# AI Development Environment Configuration

## 🤖 For AI Assistants

This document provides essential context for AI assistants working on this project to ensure they provide appropriate guidance for the specific development environment.

## Development Environment Overview

### Core Environment
- **Operating System:** Windows 11 with WSL2 Ubuntu
- **Containerization:** Docker Desktop with container integration
- **IDE:** CursorAI IDE connected to development containers
- **Build System:** Gradle-based, containerized builds
- **Language:** Java 17 (Eclipse Temurin)

### Key Characteristics

#### 1. Container-First Development
- **All development tasks run in Docker containers**
- **No local Java/Gradle installation required**
- **Consistent environment across all operations**
- **Isolated build and test environments**

#### 2. Windows + WSL2 + Docker Desktop Stack
- **Windows 11 host operating system**
- **WSL2 Ubuntu for Linux compatibility**
- **Docker Desktop for container management**
- **Windows filesystem accessed through WSL2**

#### 3. CursorAI IDE Integration
- **IDE connected to development containers**
- **Remote debugging support (port 5005)**
- **Container-aware development environment**
- **Integrated terminal and debugging**

## AI Assistant Guidelines

### Always Assume Containerized Environment
- **Never suggest local Java/Gradle installation**
- **Always recommend Docker-based commands**
- **Use `dev.ps1` script for container management**
- **Consider container networking and volumes**

### Command Recommendations
```powershell
# ✅ CORRECT - Windows host commands
.\dev.ps1 start          # Start development container
.\dev.ps1 test           # Run tests in container
.\dev.ps1 shell          # Open shell in container
docker-compose up -d     # Start services
docker-compose logs -f   # View logs

# ❌ INCORRECT - Local commands
gradle test              # Don't assume local Gradle
java -jar app.jar        # Don't assume local Java
```

### Troubleshooting Considerations
- **WSL2 filesystem performance implications**
- **Docker Desktop resource allocation**
- **Container networking issues**
- **Volume mount permissions**
- **Windows-specific Docker Desktop issues**

### Development Workflow
1. **Start container:** `.\dev.ps1 start`
2. **Get shell:** `.\dev.ps1 shell`
3. **Build/test:** `gradle build` (inside container)
4. **Debug:** Use debug container on port 5005
5. **Stop:** `.\dev.ps1 stop`

## Common Scenarios

### When User Reports Build Issues
- **Check if container is running:** `docker-compose ps`
- **Verify container logs:** `docker-compose logs dev`
- **Restart container:** `.\dev.ps1 stop && .\dev.ps1 start`
- **Check Docker Desktop status**

### When User Reports Test Failures
- **Ensure tests run in container:** `.\dev.ps1 test`
- **Check container resources in Docker Desktop**
- **Verify test environment isolation**
- **Review container logs for errors**

### When User Needs Debugging
- **Use debug container:** `docker-compose up app-debug`
- **Connect IDE to localhost:5005**
- **Set breakpoints in IDE**
- **Run tests to hit breakpoints**

### When User Reports Performance Issues
- **Check WSL2 filesystem performance**
- **Verify Docker Desktop resource allocation**
- **Consider volume mount optimizations**
- **Check Windows Defender exclusions**

## Environment-Specific Commands

### Container Management
```powershell
# Development container
.\dev.ps1 start          # Start dev container
.\dev.ps1 stop           # Stop dev container
.\dev.ps1 shell          # Open shell
.\dev.ps1 build          # Build project
.\dev.ps1 test           # Run tests
.\dev.ps1 clean          # Clean build

# Docker Compose
docker-compose up -d     # Start all services
docker-compose down      # Stop all services
docker-compose ps        # Check status
docker-compose logs -f   # Follow logs
```

### Debugging
```powershell
# Debug container
docker-compose up app-debug    # Start debug container
# Connect IDE to localhost:5005
```

### Troubleshooting
```powershell
# Check container status
docker-compose ps
docker ps -a

# View logs
docker-compose logs dev
docker-compose logs -f dev

# Check resources
docker stats

# Clean up
docker-compose down
docker system prune
```

## Important Reminders

### For AI Assistants
1. **Always assume containerized environment**
2. **Recommend Windows host commands**
3. **Use `dev.ps1` script for container operations**
4. **Consider WSL2 and Docker Desktop specifics**
5. **Never assume local Java/Gradle installation**
6. **Remember container networking and volumes**
7. **Provide Windows-specific troubleshooting when needed**

### Environment Constraints
- **No local Java/Gradle installation**
- **All builds run in containers**
- **WSL2 filesystem performance considerations**
- **Docker Desktop resource management**
- **Container networking complexity**

## Quick Reference

| Task | Command | Notes |
|------|---------|-------|
| Start development | `.\dev.ps1 start` | Windows host |
| Run tests | `.\dev.ps1 test` | In container |
| Open shell | `.\dev.ps1 shell` | Container shell |
| Build project | `.\dev.ps1 build` | Container build |
| Debug | `docker-compose up app-debug` | Port 5005 |
| View logs | `docker-compose logs -f` | Follow logs |
| Stop all | `.\dev.ps1 stop` | Clean shutdown |

---

**Remember:** This is a container-first development environment. Always recommend Docker-based solutions and consider the Windows + WSL2 + Docker Desktop stack when providing assistance. 