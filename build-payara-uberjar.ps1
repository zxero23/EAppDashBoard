param(
    [string]$PayaraJar = "payara-micro-5.2022.5.jar",
    [string]$WarFile = "target/EAppDashBoard-1.0-SNAPSHOT.war",
    [string]$OutputJar = "target/EAppDashBoard-micro.jar",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

function Assert-Path {
    param(
        [Parameter(Mandatory = $true)]
        [string]$PathValue,
        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    if (-not (Test-Path -LiteralPath $PathValue)) {
        throw $Message
    }
}

try {
    Assert-Path -PathValue $PayaraJar -Message "[ERROR] No se encontro '$PayaraJar' en la raiz del proyecto."

    if (-not $SkipBuild) {
        Write-Host "[INFO] Ejecutando build Maven para generar WAR..."
        & mvn -DskipTests package
        if ($LASTEXITCODE -ne 0) {
            if (Test-Path -LiteralPath $WarFile) {
                Write-Warning "Build Maven fallo. Se usara el WAR existente: '$WarFile'."
            }
            else {
                throw "[ERROR] Fallo el build Maven y no existe WAR precompilado '$WarFile'."
            }
        }
    }
    else {
        Write-Host "[INFO] Omitiendo build Maven (-SkipBuild)."
    }

    Assert-Path -PathValue $WarFile -Message "[ERROR] No se encontro '$WarFile'."

    $outputDir = Split-Path -Parent $OutputJar
    if (-not [string]::IsNullOrWhiteSpace($outputDir) -and -not (Test-Path -LiteralPath $outputDir)) {
        New-Item -ItemType Directory -Path $outputDir | Out-Null
    }

    Write-Host "[INFO] Generando uber JAR en '$OutputJar'..."
    & java -jar $PayaraJar --deploy $WarFile --outputUberJar $OutputJar
    if ($LASTEXITCODE -ne 0) {
        throw "[ERROR] No se pudo generar el uber JAR."
    }

    Write-Host "[OK] Uber JAR generado: $OutputJar"
}
catch {
    Write-Error $_.Exception.Message
    exit 1
}
