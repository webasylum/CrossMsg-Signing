#!/bin/bash

# CrossMsg-Signing WebUI Deployment Script
# This script deploys the web UI to the AlmaLinux server

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="CrossMsg-Signing-WebUI"
DOMAIN="web-asylum.com"
SSL_CERT_PATH="/etc/nginx/ssl"
LOG_DIR="./logs"

echo -e "${BLUE}=== CrossMsg-Signing WebUI Deployment ===${NC}"

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

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_error "This script should not be run as root"
   exit 1
fi

# Check prerequisites
print_status "Checking prerequisites..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "docker-compose.yml" ]; then
    print_error "docker-compose.yml not found. Please run this script from the project root."
    exit 1
fi

print_status "Prerequisites check passed."

# Create necessary directories
print_status "Creating necessary directories..."
mkdir -p $LOG_DIR
mkdir -p nginx/ssl
mkdir -p logs/nginx

# Check SSL certificates
print_status "Checking SSL certificates..."
if [ ! -f "nginx/ssl/web-asylum.com.crt" ] || [ ! -f "nginx/ssl/web-asylum.com.key" ]; then
    print_warning "SSL certificates not found in nginx/ssl/"
    print_warning "Please place your SSL certificates in nginx/ssl/ with the following names:"
    print_warning "  - web-asylum.com.crt (certificate)"
    print_warning "  - web-asylum.com.key (private key)"
    print_warning "Continuing without SSL for now..."
    
    # Create a temporary self-signed certificate for testing
    print_status "Creating temporary self-signed certificate for testing..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout nginx/ssl/web-asylum.com.key \
        -out nginx/ssl/web-asylum.com.crt \
        -subj "/C=US/ST=State/L=City/O=Organization/CN=web-asylum.com"
fi

# Stop existing containers
print_status "Stopping existing containers..."
docker-compose down --remove-orphans

# Build and start services
print_status "Building and starting services..."
docker-compose up --build -d

# Wait for services to be ready
print_status "Waiting for services to be ready..."
sleep 30

# Check service health
print_status "Checking service health..."

# Check backend health
if curl -f http://localhost:8080/CrossMsg-Signing/api/health > /dev/null 2>&1; then
    print_status "Backend service is healthy"
else
    print_error "Backend service is not responding"
    docker-compose logs backend
    exit 1
fi

# Check frontend health
if curl -f http://localhost:3000/health > /dev/null 2>&1; then
    print_status "Frontend service is healthy"
else
    print_error "Frontend service is not responding"
    docker-compose logs frontend
    exit 1
fi

# Check nginx health
if curl -f http://localhost/CrossMsg-Signing/health > /dev/null 2>&1; then
    print_status "Nginx reverse proxy is healthy"
else
    print_error "Nginx reverse proxy is not responding"
    docker-compose logs nginx
    exit 1
fi

# Show service status
print_status "Service status:"
docker-compose ps

# Show access information
echo ""
echo -e "${GREEN}=== Deployment Complete ===${NC}"
echo -e "${BLUE}Access URLs:${NC}"
echo -e "  Local: http://localhost/CrossMsg-Signing/"
echo -e "  HTTPS: https://$DOMAIN/CrossMsg-Signing/"
echo ""
echo -e "${BLUE}Health Check URLs:${NC}"
echo -e "  Backend: http://localhost:8080/CrossMsg-Signing/api/health"
echo -e "  Frontend: http://localhost:3000/health"
echo -e "  Nginx: http://localhost/CrossMsg-Signing/health"
echo ""
echo -e "${BLUE}Logs:${NC}"
echo -e "  Application logs: $LOG_DIR"
echo -e "  Nginx logs: $LOG_DIR/nginx"
echo ""
echo -e "${YELLOW}Useful Commands:${NC}"
echo -e "  View logs: docker-compose logs -f"
echo -e "  Stop services: docker-compose down"
echo -e "  Restart services: docker-compose restart"
echo -e "  Update and redeploy: ./scripts/deploy.sh"
echo ""

print_status "Deployment completed successfully!" 