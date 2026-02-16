Param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$logDir = Join-Path $root ".run-logs"
if (!(Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

function Require-Command {
    Param([string]$Name)
    if (!(Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Missing command: $Name"
    }
}

Require-Command "docker"
Require-Command "mvn"

Write-Host "[1/4] Starting infrastructure (MySQL/Redis/MinIO) ..."
docker compose up -d
if ($LASTEXITCODE -ne 0) {
    throw "docker compose up failed with exit code $LASTEXITCODE"
}

if (!$SkipBuild) {
    Write-Host "[2/4] Building backend modules (skip tests) ..."
    mvn -DskipTests clean package
    if ($LASTEXITCODE -ne 0) {
        throw "maven build failed with exit code $LASTEXITCODE"
    }
} else {
    Write-Host "[2/4] Build skipped (--SkipBuild)."
}

Write-Host "[3/4] Starting API in a new PowerShell window ..."
$apiCommand = "Set-Location '$root'; mvn -pl animegen-api spring-boot:run 2>&1 | Tee-Object -FilePath '$logDir\api.log'"
Start-Process -FilePath "powershell" -ArgumentList "-NoExit", "-Command", $apiCommand | Out-Null

Write-Host "[4/4] Starting Worker in a new PowerShell window ..."
$workerCommand = "Set-Location '$root'; mvn -pl animegen-worker spring-boot:run 2>&1 | Tee-Object -FilePath '$logDir\worker.log'"
Start-Process -FilePath "powershell" -ArgumentList "-NoExit", "-Command", $workerCommand | Out-Null

Write-Host ""
Write-Host "Backend start commands have been launched."
Write-Host "API log:    .run-logs\api.log"
Write-Host "Worker log: .run-logs\worker.log"
