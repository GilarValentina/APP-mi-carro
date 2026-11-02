$ErrorActionPreference = 'Stop'

function Normalize-Path([string]$Path) {
    if ([string]::IsNullOrWhiteSpace($Path)) { return $null }
    return ([System.IO.Path]::GetFullPath($Path).TrimEnd('\\').TrimEnd('/')).Replace('/', '\').ToLowerInvariant()
}

$ExpectedProjectPath = 'C:\Users\ASUS\AndroidStudioProjects\Proyecto_MiCarro'
$currentPath = (Resolve-Path .).Path
$normalizedCurrent = Normalize-Path $currentPath
$normalizedExpected = Normalize-Path $ExpectedProjectPath
$gitRoot = $null

try {
    $gitRoot = (git -C $currentPath rev-parse --show-toplevel 2>$null).Trim()
} catch {
    $gitRoot = $null
}

Write-Host '=== Verificación de proyecto MiCarro ==='
Write-Host "Ruta actual: $currentPath"
Write-Host "Ruta esperada: $ExpectedProjectPath"

if ($normalizedCurrent -ne $normalizedExpected) {
    Write-Host 'ERROR: la carpeta actual NO es la ruta correcta del proyecto.' -ForegroundColor Red
    Write-Host 'Abre Android Studio con la carpeta: C:\Users\ASUS\AndroidStudioProjects\Proyecto_MiCarro' -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path (Join-Path $currentPath 'settings.gradle.kts'))) {
    Write-Host 'ERROR: no se encontró settings.gradle.kts en la ruta actual.' -ForegroundColor Red
    exit 1
}

if ($gitRoot) {
    $normalizedGitRoot = Normalize-Path $gitRoot
    if ($normalizedGitRoot -ne $normalizedExpected) {
        Write-Host 'ERROR: el repositorio Git no coincide con la carpeta activa.' -ForegroundColor Red
        Write-Host "Git root detectado: $gitRoot" -ForegroundColor Yellow
        exit 1
    }
}

if (-not $gitRoot) {
    Write-Host 'ADVERTENCIA: no se detectó repositorio Git en esta carpeta.' -ForegroundColor Yellow
} else {
    $branch = git -C $currentPath rev-parse --abbrev-ref HEAD
    Write-Host "Rama actual: $branch" -ForegroundColor Green
}

Write-Host 'OK: la ruta del proyecto es la correcta.' -ForegroundColor Green
