param(
    [string]$AdbPath = "E:\sdk\platform-tools\adb.exe",
    [string]$PackageName = "com.animegen.app",
    [string]$ModelPath = "E:\projrcts\ai_web\models\qwen2.5-3b-instruct-q4_k_m.gguf",
    [string]$ModelName = "qwen2.5-3b-instruct-q4_k_m.gguf"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $AdbPath)) {
    throw "adb not found: $AdbPath"
}
if (-not (Test-Path $ModelPath)) {
    throw "model file not found: $ModelPath"
}

Write-Host "Checking connected devices..."
$devices = & $AdbPath devices
$online = $devices | Select-String -Pattern "device$"
if (-not $online) {
    throw "no online adb device found"
}

$tmpPath = "/data/local/tmp/$ModelName"

Write-Host "Pushing model to temp path: $tmpPath"
& $AdbPath push $ModelPath $tmpPath

Write-Host "Copying model into app files/models..."
& $AdbPath shell run-as $PackageName mkdir -p files/models
& $AdbPath shell run-as $PackageName cp $tmpPath files/models/$ModelName

Write-Host "Verifying app-side model path..."
& $AdbPath shell run-as $PackageName ls -l files/models/$ModelName

Write-Host "Done."
