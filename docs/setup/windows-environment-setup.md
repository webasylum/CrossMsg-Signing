# Windows Environment Setup Guide

**Author:** Martin Sansone (martin@web-asylum.com)  
**Last Updated:** July 2025  
**Project:** TSG-CrossMsg-Signing

## Overview

This guide explains the recommended Windows development environment setup for the TSG-CrossMsg-Signing project. The setup uses **Windows 11 + WSL2 + Docker Desktop** to provide optimal cross-platform development capabilities while maintaining performance and compatibility.

## Architecture Rationale

### Why This Setup?

The TSG-CrossMsg-Signing project requires a sophisticated development environment that can handle:

1. **Cross-Platform Development**: Java applications that may need to run on both Windows and Linux
2. **Containerized Testing**: Consistent, reproducible test environments
3. **Performance**: Fast file I/O and build times
4. **Tool Integration**: Support for Windows IDEs (CursorAI, VS Code) and Linux tools
5. **ISO 20022 Standards**: Financial messaging standards that often require Linux-based tooling

### The Solution: Windows 11 + WSL2 + Docker Desktop

```
┌─────────────────────────────────────────────────────────────┐
│                    Windows 11 Host                          │
├─────────────────────────────────────────────────────────────┤
│  C:\Projects\TSG-CrossMsg-Signing                          │
│  ├── src/                                                   │
│  ├── build.gradle                                          │
│  ├── docker-compose.yml                                    │
│  └── Dockerfile                                            │
├─────────────────────────────────────────────────────────────┤
│                    WSL2 (Ubuntu)                            │
│  /mnt/c/Projects/TSG-CrossMsg-Signing                      │
│  └── (Same files, Linux file system access)                │
├─────────────────────────────────────────────────────────────┤
│                  Docker Desktop                             │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │   Dev Container │  │  Test Container │                  │
│  │   (Gradle)      │  │   (JUnit)       │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

## Prerequisites

### System Requirements
- **Windows 11** (Build 22000 or later)
- **WSL2** enabled
- **Docker Desktop** with WSL2 backend
- **At least 16GB RAM** (32GB recommended)
- **SSD storage** for optimal performance

### Software Requirements
- Windows 11 Pro, Enterprise, or Education
- WSL2 with Ubuntu distribution
- Docker Desktop for Windows
- Git for Windows
- Your preferred IDE (CursorAI, VS Code, IntelliJ IDEA)

## Step-by-Step Setup

### 1. Enable WSL2

Open PowerShell as Administrator and run:

```powershell
# Enable WSL
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart

# Enable Virtual Machine feature
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

# Restart your computer
Restart-Computer
```

After restart, set WSL2 as default:

```powershell
wsl --set-default-version 2
```

### 2. Install Ubuntu on WSL2

```powershell
# Install Ubuntu from Microsoft Store or via command line
wsl --install -d Ubuntu

# Verify installation
wsl --list --verbose
```

### 3. Install Docker Desktop

1. Download Docker Desktop from [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. Install with **WSL2 backend** option enabled
3. Ensure Docker Desktop is configured to use WSL2 integration

### 4. Configure Project Directory Structure

Create the recommended directory structure:

```powershell
# Create main projects directory
mkdir C:\Projects

# Clone or copy your project
cd C:\Projects
git clone <your-repo-url> TSG-CrossMsg-Signing

# Create Docker-specific subdirectory (optional)
mkdir C:\Projects\docker
```

### 5. Verify WSL2 Access

From WSL2 Ubuntu, verify access to your project:

```bash
# In WSL2 Ubuntu terminal
ls -la /mnt/c/Projects/TSG-CrossMsg-Signing
```

## Project-Specific Configuration

### Directory Mapping

Your project uses this directory structure:

```
Windows Path: C:\Projects\TSG-CrossMsg-Signing
WSL Path: /mnt/c/Projects/TSG-CrossMsg-Signing
Docker Context: Uses Docker Desktop with WSL2 backend
```

### Docker Configuration

The project's `docker-compose.yml` is already optimized for this setup:

```yaml
version: '3.8'
services:
  dev:
    build:
      context: .
    volumes:
      - type: bind
        source: .                    # C:\Projects\TSG-CrossMsg-Signing
        target: /app                 # Inside container
      - type: bind
        source: ./build              # Build output
        target: /app/build
    ports:
      - "8080:8080"
      - "5005:5005"
```

### Development Workflow

1. **Development**: Use your preferred IDE in Windows
   - CursorAI, VS Code, IntelliJ IDEA
   - Direct access to `C:\Projects\TSG-CrossMsg-Signing`

2. **Building**: Use Docker Desktop
   ```powershell
   .\dev.ps1 build
   ```

3. **Testing**: Run in containers
   ```powershell
   .\dev.ps1 test
   ```

4. **Debugging**: Use container debugging
   ```powershell
   .\dev.ps1 shell
   ```

## Performance Optimizations

### WSL2 Performance Settings

Create or edit `%USERPROFILE%\.wslconfig`:

```ini
[wsl2]
memory=16GB
processors=8
swap=0
localhostForwarding=true
```

### Docker Desktop Settings

1. **Resources**: Allocate sufficient memory (8GB+ recommended)
2. **WSL Integration**: Enable for Ubuntu distribution
3. **File Sharing**: Ensure `C:\Projects` is shared

### File System Performance

- **Use WSL2 file system** for Linux tools when possible
- **Use Windows file system** for IDE access
- **Docker volumes** provide optimal performance for container operations

## Troubleshooting

### Common Issues

#### WSL2 Not Starting
```powershell
# Reset WSL2
wsl --shutdown
wsl --update
```

#### Docker Desktop Issues
```powershell
# Reset Docker Desktop
docker system prune -a
```

#### File Permission Issues
```bash
# In WSL2, check file permissions
ls -la /mnt/c/Projects/TSG-CrossMsg-Signing
```

#### Performance Issues
- Ensure WSL2 has sufficient memory allocation
- Check Docker Desktop resource limits
- Verify antivirus exclusions for project directories

### Verification Commands

```powershell
# Check WSL2 status
wsl --list --verbose

# Check Docker status
docker info

# Check project access
wsl -d Ubuntu -e ls -la /mnt/c/Projects/TSG-CrossMsg-Signing
```

## Benefits of This Setup

### 1. **Cross-Platform Compatibility**
- Windows IDEs for development
- Linux tooling for testing and deployment
- Containerized builds for consistency

### 2. **Performance**
- WSL2 provides near-native Linux performance
- Docker Desktop with WSL2 backend optimizes container operations
- Direct file system access eliminates I/O bottlenecks

### 3. **Development Experience**
- Use familiar Windows tools (CursorAI, VS Code)
- Access Linux-specific tools when needed
- Consistent environment across team members

### 4. **Production Alignment**
- Containers run the same way in development and production
- Linux-based deployment targets are easily tested
- ISO 20022 tooling compatibility

## Alternative Setups

### Option 1: Pure Windows
- **Pros**: Simpler setup, native performance
- **Cons**: Limited Linux tooling, potential compatibility issues

### Option 2: Pure Linux VM
- **Pros**: Full Linux environment
- **Cons**: Performance overhead, complex file sharing

### Option 3: Cloud Development
- **Pros**: Consistent environment, no local setup
- **Cons**: Network dependency, potential latency

## Conclusion

The Windows 11 + WSL2 + Docker Desktop setup provides the optimal balance of:
- **Performance**: Fast file I/O and container operations
- **Compatibility**: Support for both Windows and Linux tooling
- **Productivity**: Familiar development environment with powerful Linux capabilities
- **Consistency**: Reproducible builds and testing environments

This setup is specifically designed for projects like TSG-CrossMsg-Signing that require:
- Financial messaging standards compliance
- Cross-platform compatibility
- Containerized development and testing
- Integration with both Windows and Linux ecosystems

## References

- [WSL2 Installation Guide](https://docs.microsoft.com/en-us/windows/wsl/install)
- [Docker Desktop WSL2 Backend](https://docs.docker.com/desktop/windows/wsl/)
- [WSL2 Performance Optimization](https://docs.microsoft.com/en-us/windows/wsl/compare-versions)
- [ISO 20022 Standards](https://www.iso20022.org/)

---

**Note**: This setup guide is specifically tailored for the TSG-CrossMsg-Signing project. For other projects, consider the specific requirements and adjust accordingly. 