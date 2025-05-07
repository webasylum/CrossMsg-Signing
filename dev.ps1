# Development script for TSG CrossMsg Signing project

param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

function Show-Help {
    Write-Host "Available commands:"
    Write-Host "  build    - Build the project"
    Write-Host "  test     - Run tests"
    Write-Host "  watch    - Run tests in watch mode"
    Write-Host "  debug    - Start debug mode"
    Write-Host "  clean    - Clean build files"
    Write-Host "  shell    - Start development shell"
    Write-Host "  health   - Check container health status"
    Write-Host "  docker   - Show Docker resources for this project"
    Write-Host "  help     - Show this help message"
}

function Build-Project {
    Write-Host "Building project..."
    docker-compose build
}

function Run-Tests {
    Write-Host "Running tests..."
    docker-compose run --rm app gradle test
}

function Watch-Tests {
    Write-Host "Starting test watch mode..."
    docker-compose up app-dev
}

function Start-Debug {
    Write-Host "Starting debug mode..."
    Write-Host "Connect your debugger to localhost:5005"
    docker-compose up app-debug
}

function Clean-Project {
    Write-Host "Cleaning project..."
    docker-compose run --rm app gradle clean
    docker-compose down -v
}

function Start-Shell {
    Write-Host "Starting development shell..."
    docker-compose run --rm app bash
}

function Check-Health {
    Write-Host "Checking container health status..."
    docker-compose ps
    Write-Host "`nDetailed health status:"
    docker-compose ps --format json | ConvertFrom-Json | ForEach-Object {
        Write-Host "`nService: $($_.Service)"
        Write-Host "Status: $($_.Status)"
        Write-Host "Health: $($_.Health)"
    }
}

function Show-Docker-Resources {
    Write-Host "`n=== TSG Cross Message Signing Docker Resources ===`n"
    
    Write-Host "Containers:"
    docker ps -a --filter "name=tsg-crossmsg-signing" --format "table {{.Names}}\t{{.Status}}\t{{.Image}}"
    
    Write-Host "`nImages:"
    docker images --filter "label=com.tsg.project=tsg-crossmsg-signing" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
    
    Write-Host "`nVolumes:"
    docker volume ls --filter "name=tsg-crossmsg-signing" --format "table {{.Name}}\t{{.Driver}}\t{{.Scope}}"
}

# Main script logic
switch ($Command) {
    "build" { Build-Project }
    "test" { Run-Tests }
    "watch" { Watch-Tests }
    "debug" { Start-Debug }
    "clean" { Clean-Project }
    "shell" { Start-Shell }
    "health" { Check-Health }
    "docker" { Show-Docker-Resources }
    "help" { Show-Help }
    default {
        Write-Host "Unknown command: $Command"
        Show-Help
    }
} 