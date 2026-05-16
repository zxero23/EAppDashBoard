param(
    [string]$PayaraJar = "payara-micro-7.2026.2.jar",
    [string]$WarFile = "target/ROOT.war",
    [string]$OutputJar = "target/EAppDashBoard-micro.jar",
    [switch]$SkipBuild,
    [switch]$SkipJavaSwitch
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

function Ensure-Java21 {
    if ($SkipJavaSwitch) {
        Write-Host "[INFO] Omitiendo cambio de Java (-SkipJavaSwitch)."
        return
    }

    if (Get-Command jabba -ErrorAction SilentlyContinue) {
        $candidates = @("system@21.0.10", "zulu@1.21.0", "openjdk@21")
        foreach ($candidate in $candidates) {
            Write-Host "[INFO] Intentando Java 21 con jabba: $candidate"
            & jabba use $candidate
            if ($LASTEXITCODE -eq 0) {
                Write-Host "[INFO] Java activa: $candidate"
                return
            }
        }
        throw "[ERROR] No se pudo activar una JVM 21 con jabba. Instala una version Java 21 (por ejemplo zulu@1.21.0)."
    }

    Write-Warning "jabba no esta disponible. Se usara JAVA_HOME/java actual; asegurate de usar Java 21."
}

try {
    Ensure-Java21
    Assert-Path -PathValue $PayaraJar -Message "[ERROR] No se encontro '$PayaraJar' en la raiz del proyecto."

    if (-not $SkipBuild) {
        Write-Host "[INFO] Ejecutando build Maven para generar WAR (Java 21 / Jakarta)..."
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
