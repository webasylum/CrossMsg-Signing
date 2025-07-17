#!/bin/bash

# Script to copy web UI files to private repository
# This script copies only the web UI components to the private repo

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PRIVATE_REPO_PATH="./CrossMsg-Signing-WebUI"
SOURCE_DIR="."

echo -e "${BLUE}=== Copying Web UI Files to Private Repository ===${NC}"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if private repo directory exists
if [ ! -d "$PRIVATE_REPO_PATH" ]; then
    print_error "Private repository directory not found: $PRIVATE_REPO_PATH"
    print_error "Please clone the private repository first:"
    print_error "  git clone https://github.com/webasylum/CrossMsg-Signing-WebUI.git ../CrossMsg-Signing-WebUI"
    exit 1
fi

# Create backup of private repo
print_status "Creating backup of private repository..."
BACKUP_DIR="./CrossMsg-Signing-WebUI-backup-$(date +%Y%m%d-%H%M%S)"
cp -r "$PRIVATE_REPO_PATH" "$BACKUP_DIR"
print_status "Backup created: $BACKUP_DIR"

# Clean private repo (keep .git)
print_status "Cleaning private repository..."
cd "$PRIVATE_REPO_PATH"
# Only run git commands if there are tracked files
if [ "$(git ls-files | wc -l)" -gt 0 ]; then
    git checkout -- .
    git clean -fd
fi
cd - > /dev/null

# Copy web UI files
print_status "Copying web UI files..."

# Copy webui-backend
if [ -d "$SOURCE_DIR/webui-backend" ]; then
    cp -r "$SOURCE_DIR/webui-backend" "$PRIVATE_REPO_PATH/"
    print_status "✓ Copied webui-backend"
else
    print_error "webui-backend directory not found"
    exit 1
fi

# Copy webui-frontend
if [ -d "$SOURCE_DIR/webui-frontend" ]; then
    cp -r "$SOURCE_DIR/webui-frontend" "$PRIVATE_REPO_PATH/"
    print_status "✓ Copied webui-frontend"
else
    print_error "webui-frontend directory not found"
    exit 1
fi

# Copy nginx configuration
if [ -d "$SOURCE_DIR/nginx" ]; then
    cp -r "$SOURCE_DIR/nginx" "$PRIVATE_REPO_PATH/"
    print_status "✓ Copied nginx configuration"
else
    print_error "nginx directory not found"
    exit 1
fi

# Copy scripts
if [ -d "$SOURCE_DIR/scripts" ]; then
    cp -r "$SOURCE_DIR/scripts" "$PRIVATE_REPO_PATH/"
    print_status "✓ Copied scripts"
else
    print_error "scripts directory not found"
    exit 1
fi

# Copy docker-compose.yml
if [ -f "$SOURCE_DIR/docker-compose.yml" ]; then
    cp "$SOURCE_DIR/docker-compose.yml" "$PRIVATE_REPO_PATH/"
    print_status "✓ Copied docker-compose.yml"
else
    print_error "docker-compose.yml not found"
    exit 1
fi

# Copy README
if [ -f "$SOURCE_DIR/README-WebUI.md" ]; then
    cp "$SOURCE_DIR/README-WebUI.md" "$PRIVATE_REPO_PATH/README.md"
    print_status "✓ Copied README.md"
else
    print_error "README-WebUI.md not found"
    exit 1
fi

# Copy ISO message samples (needed for the web UI)
if [ -d "$SOURCE_DIR/src/test/resources/iso" ]; then
    mkdir -p "$PRIVATE_REPO_PATH/src/test/resources"
    cp -r "$SOURCE_DIR/src/test/resources/iso" "$PRIVATE_REPO_PATH/src/test/resources/"
    print_status "✓ Copied ISO message samples"
else
    print_warning "ISO message samples not found - web UI may not work properly"
fi

# Make scripts executable
print_status "Making scripts executable..."
chmod +x "$PRIVATE_REPO_PATH/scripts/"*.sh

# Create .gitignore for private repo
print_status "Creating .gitignore for private repository..."
cat > "$PRIVATE_REPO_PATH/.gitignore" << 'EOF'
# Logs
logs/
*.log

# SSL certificates (should be added manually)
nginx/ssl/*.crt
nginx/ssl/*.key

# Node modules (for development)
webui-frontend/node_modules/

# Build artifacts
webui-backend/build/
webui-frontend/build/

# IDE files
.vscode/
.idea/
*.swp
*.swo

# OS files
.DS_Store
Thumbs.db

# Temporary files
*.tmp
*.temp
EOF

print_status "✓ Created .gitignore"

# Show what was copied
echo ""
echo -e "${GREEN}=== Files Copied Successfully ===${NC}"
echo -e "${BLUE}Web UI Components:${NC}"
echo "  ✓ webui-backend/ (Spring Boot API)"
echo "  ✓ webui-frontend/ (React application)"
echo "  ✓ nginx/ (Reverse proxy configuration)"
echo "  ✓ scripts/ (Deployment scripts)"
echo "  ✓ docker-compose.yml (Container orchestration)"
echo "  ✓ README.md (Documentation)"
echo "  ✓ src/test/resources/iso/ (Message samples)"
echo "  ✓ .gitignore (Git ignore rules)"

# Instructions for next steps
echo ""
echo -e "${YELLOW}=== Next Steps ===${NC}"
echo "1. Navigate to the private repository:"
echo "   cd $PRIVATE_REPO_PATH"
echo ""
echo "2. Review the copied files:"
echo "   ls -la"
echo ""
echo "3. Commit the changes:"
echo "   git add ."
echo "   git commit -m 'Add web UI components'"
echo "   git push origin main"
echo ""
echo "4. Deploy to your AlmaLinux server:"
echo "   ./scripts/deploy.sh"
echo ""
echo -e "${GREEN}✓ Web UI files successfully copied to private repository!${NC}" 