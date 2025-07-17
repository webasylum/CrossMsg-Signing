# TSG-CrossMsg-Signing Setup Verification Script
# This script verifies that the core project development environment is working correctly

Write-Host "🔍 TSG-CrossMsg-Signing Setup Verification" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# Check if Docker is running
Write-Host "`n1. Checking Docker Desktop..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "✅ Docker is available: $dockerVersion" -ForegroundColor Green
}
catch {
    Write-Host "❌ Docker is not available. Please install Docker Desktop." -ForegroundColor Red
    exit 1
}

# Check if docker-compose is available
Write-Host "`n2. Checking Docker Compose..." -ForegroundColor Yellow
try {
    $composeVersion = docker-compose --version
    Write-Host "✅ Docker Compose is available: $composeVersion" -ForegroundColor Green
}
catch {
    Write-Host "❌ Docker Compose is not available." -ForegroundColor Red
    exit 1
}

# Check if dev.ps1 exists
Write-Host "`n3. Checking development script..." -ForegroundColor Yellow
if (Test-Path "dev.ps1") {
    Write-Host "✅ dev.ps1 script found" -ForegroundColor Green
}
else {
    Write-Host "❌ dev.ps1 script not found. Are you in the correct directory?" -ForegroundColor Red
    exit 1
}

# Check if docker-compose.yml exists
Write-Host "`n4. Checking Docker Compose configuration..." -ForegroundColor Yellow
if (Test-Path "docker-compose.yml") {
    Write-Host "✅ docker-compose.yml found" -ForegroundColor Green
}
else {
    Write-Host "❌ docker-compose.yml not found. Are you in the correct directory?" -ForegroundColor Red
    exit 1
}

# Start the development environment
Write-Host "`n5. Starting development environment..." -ForegroundColor Yellow
try {
    .\dev.ps1 start
    Write-Host "✅ Development environment started successfully" -ForegroundColor Green
}
catch {
    Write-Host "❌ Failed to start development environment" -ForegroundColor Red
    Write-Host "   Try: docker-compose down --volumes; docker system prune -f" -ForegroundColor Yellow
    exit 1
}

# Wait a moment for container to fully start
Start-Sleep -Seconds 5

# Check if container is running
Write-Host "`n6. Verifying container status..." -ForegroundColor Yellow
try {
    $containerStatus = docker-compose ps
    if ($containerStatus -match "Up") {
        Write-Host "✅ Development container is running" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Development container is not running properly" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Failed to check container status" -ForegroundColor Red
    exit 1
}

# Test Java installation
Write-Host "`n7. Testing Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = docker-compose exec dev java -version 2>&1
    if ($javaVersion -match "17") {
        Write-Host "✅ Java 17 is available in container" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Java 17 not found in container" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Failed to test Java installation" -ForegroundColor Red
    exit 1
}

# Test Gradle installation
Write-Host "`n8. Testing Gradle installation..." -ForegroundColor Yellow
try {
    $gradleVersion = docker-compose exec dev gradle --version 2>&1
    if ($gradleVersion -match "8.6") {
        Write-Host "✅ Gradle 8.6 is available in container" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Gradle 8.6 not found in container" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Failed to test Gradle installation" -ForegroundColor Red
    exit 1
}

# Test project structure
Write-Host "`n9. Testing project structure..." -ForegroundColor Yellow
try {
    $projectFiles = docker-compose exec dev ls -la /app
    if ($projectFiles -match "build.gradle") {
        Write-Host "✅ Project files are accessible in container" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Project files not accessible in container" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Failed to test project structure" -ForegroundColor Red
    exit 1
}

# Run a quick test
Write-Host "`n10. Running quick test..." -ForegroundColor Yellow
try {
    $testResult = docker-compose exec dev gradle test --tests "*ProjectSetup*Test" --console=plain 2>&1
    if ($testResult -match "BUILD SUCCESSFUL") {
        Write-Host "✅ Quick test passed successfully" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Quick test failed" -ForegroundColor Red
        Write-Host "   Test output: $testResult" -ForegroundColor Yellow
        exit 1
    }
}
catch {
    Write-Host "❌ Failed to run quick test" -ForegroundColor Red
    exit 1
}

Write-Host "`n🎉 SETUP VERIFICATION COMPLETE!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host "✅ Your TSG-CrossMsg-Signing development environment is working correctly!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Run all tests: .\dev.ps1 test" -ForegroundColor White
Write-Host "2. Access development shell: .\dev.ps1 shell" -ForegroundColor White
Write-Host "3. View test reports: build/reports/tests/html/index.html" -ForegroundColor White
Write-Host "`nFor more information, see the README.md file." -ForegroundColor Cyan