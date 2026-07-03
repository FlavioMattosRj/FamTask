<#
    Build completo e limpo do projeto tarefas-benchmark, com os caches de build
    (repositorio local do Maven e cache do npm) redirecionados para dentro do
    proprio projeto, na pasta ".buildcache".

    Motivo: manter todos os caches de build na mesma tecnologia/dispositivo de
    persistencia em que o projeto reside, para que medicoes de tempo de build
    (benchmark) nao sejam distorcidas por caches localizados em outro disco
    (ex.: repositorio Maven padrao em %USERPROFILE%\.m2 ou cache npm padrao em
    %APPDATA%\npm-cache).

    Uso:
        .\scripts\build-benchmark.ps1

    Ou, para reaproveitar a funcao em outro script de benchmark:
        . .\scripts\build-benchmark.ps1 -SoDefinirFuncao
        Invoke-BuildBenchmark
#>

param(
    [switch]$SoDefinirFuncao
)

function Invoke-BuildBenchmark {
    [CmdletBinding()]
    param(
        [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
    )

    $repositorioMaven = Join-Path $ProjectRoot ".buildcache\maven-repo"
    $cacheNpm = Join-Path $ProjectRoot ".buildcache\npm-cache"

    New-Item -ItemType Directory -Force -Path $repositorioMaven | Out-Null
    New-Item -ItemType Directory -Force -Path $cacheNpm | Out-Null

    # Redireciona o cache do npm ANTES de qualquer chamada de build. O exec-maven-plugin
    # invoca "npm.cmd" como processo filho da JVM do Maven, e processos filhos herdam as
    # variaveis de ambiente do processo pai, entao basta setar aqui antes do "mvn".
    $env:npm_config_cache = $cacheNpm

    Write-Host "Repositorio local do Maven: $repositorioMaven"
    Write-Host "Cache do npm:               $cacheNpm"

    Push-Location $ProjectRoot
    try {
        & mvn clean package "-Dmaven.repo.local=$repositorioMaven"
        if ($LASTEXITCODE -ne 0) {
            throw "mvn clean package falhou (exit code $LASTEXITCODE)"
        }
    }
    finally {
        Pop-Location
    }
}

if (-not $SoDefinirFuncao) {
    Invoke-BuildBenchmark
}
