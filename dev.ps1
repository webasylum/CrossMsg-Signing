# Development script for TSG CrossMsg Signing project

param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

function Show-Help {
    Write-Host "Available commands:"
    Write-Host "  build    - Build the project"
    Write-Host "  test     - Run tests"
    Write-Host "  clean    - Clean build files"
    Write-Host "  shell    - Open a shell in the dev container"
    Write-Host "  start    - Start the dev container"
    Write-Host "  stop     - Stop the dev container"
    Write-Host "  help     - Show this help message"
}

function Start-DevContainer {
    docker-compose up -d
}

function Stop-DevContainer {
    docker-compose down
}

function Enter-DevContainer {
    docker-compose exec dev powershell
}

function Invoke-GradleCommand {
    param([string]$GradleCommand)
    docker-compose exec dev gradle $GradleCommand
}

switch ($Command) {
    "build" {
        Start-DevContainer
        Invoke-GradleCommand "build"
    }
    "test" {
        Start-DevContainer
        Invoke-GradleCommand "test"
    }
    "clean" {
        Start-DevContainer
        Invoke-GradleCommand "clean"
    }
    "shell" {
        Start-DevContainer
        Enter-DevContainer
    }
    "start" {
        Start-DevContainer
    }
    "stop" {
        Stop-DevContainer
    }
    "help" {
        Show-Help
    }
    default {
        Write-Host "Unknown command: $Command"
        Show-Help
    }
} 