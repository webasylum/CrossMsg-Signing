# Development Environment Setup

**Author:** Martin Sansone (martin@web-asylum.com)  
**Last Updated:** July 2025

## Overview

This directory contains all environment setup and configuration documentation for the TSG-CrossMsg-Signing project. The project is designed to run in a **Windows 11 + WSL2 + Docker Desktop** environment for optimal cross-platform development capabilities.

## Setup Guides

### 🚀 Quick Start
- **[Quick Setup Reference](quick-setup-reference.md)** - Get up and running in 5 minutes
  - Prerequisites checklist
  - Essential commands
  - Common troubleshooting

### 📋 Complete Setup
- **[Windows Environment Setup](windows-environment-setup.md)** - Comprehensive setup guide
  - Step-by-step WSL2 installation
  - Docker Desktop configuration
  - Performance optimizations
  - Architecture rationale

## Environment Architecture

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

## Why This Setup?

The TSG-CrossMsg-Signing project requires:

1. **Cross-Platform Development**: Java applications for Windows and Linux
2. **Containerized Testing**: Consistent, reproducible test environments
3. **Performance**: Fast file I/O and build times
4. **Tool Integration**: Support for Windows IDEs and Linux tools
5. **ISO 20022 Standards**: Financial messaging standards compatibility

## Prerequisites

- **Windows 11** (Build 22000 or later)
- **WSL2** with Ubuntu distribution
- **Docker Desktop** with WSL2 backend
- **At least 16GB RAM** (32GB recommended)
- **SSD storage** for optimal performance

## Quick Verification

```powershell
# Check environment
wsl --list --verbose
docker --version
docker-compose --version

# Verify project access
wsl -d Ubuntu -e ls -la /mnt/c/Projects/TSG-CrossMsg-Signing
```

## Development Workflow

1. **Development**: Use Windows IDEs (CursorAI, VS Code)
2. **Building**: Use Docker Desktop with `.\dev.ps1 build`
3. **Testing**: Run in containers with `.\dev.ps1 test`
4. **Debugging**: Use container debugging with `.\dev.ps1 shell`

## Troubleshooting

### Common Issues
- **WSL2 not starting**: `wsl --shutdown && wsl --update`
- **Docker issues**: `docker system prune -a`
- **File permissions**: Check WSL2 access to `/mnt/c/Projects/`

### Performance Issues
- Ensure WSL2 has sufficient memory allocation
- Check Docker Desktop resource limits
- Verify antivirus exclusions for project directories

## Related Documentation

- [Project README](../../README.md) - Main project documentation
- [Architecture Documentation](../architecture/system-design.md) - System design details
- [Implementation Guide](../implementation/README.md) - Development implementation

---

**Need Help?** Start with the [Quick Setup Reference](quick-setup-reference.md) or check the troubleshooting sections in the detailed guides. 