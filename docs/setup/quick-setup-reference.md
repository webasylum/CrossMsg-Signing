# Quick Setup Reference

**For TSG-CrossMsg-Signing Project**

## 🚀 Quick Start (5 minutes)

### 1. Verify Prerequisites
```powershell
# Check Windows version (needs Windows 11)
winver

# Check WSL2
wsl --list --verbose

# Check Docker
docker --version
docker-compose --version
```

### 2. Clone and Setup
```powershell
# Clone to recommended location
cd C:\Projects
git clone <your-repo-url> TSG-CrossMsg-Signing
cd TSG-CrossMsg-Signing

# Verify WSL2 access
wsl -d Ubuntu -e ls -la /mnt/c/Projects/TSG-CrossMsg-Signing
```

### 3. Build and Test
```powershell
# Start development environment
.\dev.ps1 start

# Build project
.\dev.ps1 build

# Run tests
.\dev.ps1 test
```

## ✅ Environment Checklist

- [ ] Windows 11 (Build 22000+)
- [ ] WSL2 with Ubuntu installed
- [ ] Docker Desktop with WSL2 backend
- [ ] Project in `C:\Projects\TSG-CrossMsg-Signing`
- [ ] WSL2 can access `/mnt/c/Projects/TSG-CrossMsg-Signing`
- [ ] Docker containers can build and run

## 🔧 Common Commands

```powershell
# Development workflow
.\dev.ps1 start      # Start dev container
.\dev.ps1 build      # Build project
.\dev.ps1 test       # Run tests
.\dev.ps1 shell      # Open shell in container
.\dev.ps1 stop       # Stop containers

# Docker commands
docker ps            # List running containers
docker logs <container>  # View container logs
docker system prune  # Clean up Docker resources

# WSL2 commands
wsl --list --verbose # List WSL distributions
wsl --shutdown       # Shutdown WSL2
wsl --update         # Update WSL2
```

## 🐛 Quick Troubleshooting

### Docker Issues
```powershell
# Reset Docker Desktop
docker system prune -a
# Restart Docker Desktop
```

### WSL2 Issues
```powershell
# Reset WSL2
wsl --shutdown
wsl --update
```

### File Access Issues
```bash
# Check file permissions in WSL2
ls -la /mnt/c/Projects/TSG-CrossMsg-Signing
```

## 📁 Directory Structure

```
C:\Projects\TSG-CrossMsg-Signing\          # Windows path
/mnt/c/Projects/TSG-CrossMsg-Signing/      # WSL2 path
/app/                                       # Container path
```

## 🔗 Full Documentation

For detailed setup instructions, see:
- [Windows Environment Setup Guide](windows-environment-setup.md)
- [Project README](../../README.md)
- [Architecture Documentation](../architecture/system-design.md)

---

**Need Help?** Check the troubleshooting section in the full setup guide or open an issue on GitHub. 