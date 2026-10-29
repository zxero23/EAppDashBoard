param(
    [string]$PayaraJar = "payara-micro-5.2022.5.jar",
    [string]$AsadminFile = "create-datasource.asadmin",
    [string]$WarFile = "target/ROOT.war",
    [string]$UberJar = "target/SysSE-server.jar",
    [string]$PostgresJarPath = "",
    [int]$Port = 8080,
    [int]$MaxPortTries = 20,
    [switch]$SkipStopExisting,
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

function Test-PortInUse {
    param([int]$PortNumber)
    $listeners = [System.Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners()
    return ($listeners | Where-Object { $_.Port -eq $PortNumber } | Select-Object -First 1) -ne $null
}

function Get-AvailablePort {
    param(
        [int]$StartPort,
        [int]$Tries
    )

    $candidate = $StartPort
    for ($i = 0; $i -lt $Tries; $i++) {
        if (-not (Test-PortInUse -PortNumber $candidate)) {
            return $candidate
        }
        $candidate++
    }
    throw "[ERROR] No se encontro puerto libre desde $StartPort en $Tries intentos."
}

function Resolve-PostgresDriver {
    param([string]$PreferredPath)

    if (-not [string]::IsNullOrWhiteSpace($PreferredPath) -and (Test-Path -LiteralPath $PreferredPath)) {
        return (Resolve-Path -LiteralPath $PreferredPath).Path
    }

    $localJar = "target/payara-libs/postgresql.jar"
    if (Test-Path -LiteralPath $localJar) {
        return (Resolve-Path -LiteralPath $localJar).Path
    }

    $m2Base = Join-Path $env:USERPROFILE ".m2/repository/org/postgresql/postgresql"
    if (Test-Path -LiteralPath $m2Base) {
        $candidate = Get-ChildItem -Path $m2Base -Recurse -Filter "postgresql-*.jar" -File -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
        if ($null -ne $candidate) {
            return $candidate.FullName
        }
    }

    throw "[ERROR] No se encontro el driver PostgreSQL. Usa -PostgresJarPath o coloca postgresql.jar en target/payara-libs."
}

function Stop-ExistingServerProcess {
    param([string]$UberJarPath)

    $uberFileName = [System.IO.Path]::GetFileName($UberJarPath)
    $javaProcesses = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" -ErrorAction SilentlyContinue
    if ($null -eq $javaProcesses) {
        return
    }

    $matching = $javaProcesses | Where-Object {
        $_.CommandLine -and ($_.CommandLine -like "*$uberFileName*")
    }

    foreach ($proc in $matching) {
        Write-Warning "Deteniendo proceso Java previo (PID $($proc.ProcessId)) que usa $uberFileName"
        Stop-Process -Id $proc.ProcessId -Force -ErrorAction SilentlyContinue
    }
}

function Resolve-MavenCommand {
    $candidates = @()

    $cmdFromPath = Get-Command mvn -ErrorAction SilentlyContinue
    if ($cmdFromPath) {
        $candidates += $cmdFromPath.Source
    }

    $cmdFromPathCmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($cmdFromPathCmd) {
        $candidates += $cmdFromPathCmd.Source
    }

    if ($env:MAVEN_HOME) {
        $candidates += (Join-Path $env:MAVEN_HOME "bin/mvn.cmd")
    }

    if ($env:M2_HOME) {
        $candidates += (Join-Path $env:M2_HOME "bin/mvn.cmd")
    }

    $candidates += "C:/apache-maven-3.6.3/bin/mvn.cmd"
    $candidates += "C:/apache-maven/bin/mvn.cmd"
    $candidates += "C:/Program Files/Apache/Maven/bin/mvn.cmd"

    foreach ($candidate in $candidates) {
        if (-not [string]::IsNullOrWhiteSpace($candidate) -and (Test-Path -LiteralPath $candidate)) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }

    throw "[ERROR] No se encontro Maven (mvn/mvn.cmd). Agrega Maven al PATH o configura MAVEN_HOME."
}

try {
    if (-not $SkipJavaSwitch) {
        if (Get-Command jabba -ErrorAction SilentlyContinue) {
            Write-Host "[1/4] Usando Java 8 (jabba use zulu@1.8.0)..."
            & jabba use zulu@1.8.0
            if ($LASTEXITCODE -ne 0) {
                throw "[ERROR] Fallo 'jabba use zulu@1.8.0'."
            }
        }
        else {
            Write-Warning "No se encontro jabba en PATH. Continuando con el Java actual."
        }
    }

    Assert-Path -PathValue $PayaraJar -Message "[ERROR] No se encontro '$PayaraJar' en la raiz del proyecto."
    Assert-Path -PathValue $AsadminFile -Message "[ERROR] No se encontro '$AsadminFile' en la raiz del proyecto."
    $PostgresDriver = Resolve-PostgresDriver -PreferredPath $PostgresJarPath
    Assert-Path -PathValue $PostgresDriver -Message "[ERROR] No se encontro '$PostgresDriver'."

    if (-not $SkipStopExisting) {
        Stop-ExistingServerProcess -UberJarPath $UberJar
    }

    $mavenCmd = Resolve-MavenCommand
    Write-Host "[2/4] mvn clean package ($mavenCmd)"
    & $mavenCmd clean package
    if ($LASTEXITCODE -ne 0) {
        throw "[ERROR] Fallo el paso 1 (mvn clean package)."
    }

    Assert-Path -PathValue $WarFile -Message "[ERROR] No se encontro '$WarFile' despues del build."

    Write-Host "[3/4] Generando uber JAR ($UberJar) con driver PostgreSQL"
    & java -jar $PayaraJar --addlibs $PostgresDriver --postbootcommandfile $AsadminFile --deploy $WarFile --contextRoot / --outputUberJar $UberJar
    if ($LASTEXITCODE -ne 0) {
        throw "[ERROR] Fallo el paso 2 (outputUberJar)."
    }

    Assert-Path -PathValue $UberJar -Message "[ERROR] No se encontro '$UberJar'."

    $selectedPort = Get-AvailablePort -StartPort $Port -Tries $MaxPortTries
    if ($selectedPort -ne $Port) {
        Write-Warning "Puerto $Port ocupado. Se usara $selectedPort."
    }

    Write-Host "[4/4] Levantando local: java -jar $UberJar --addlibs $PostgresDriver --port $selectedPort"
    & java -jar $UberJar --addlibs $PostgresDriver --port $selectedPort
    exit $LASTEXITCODE
}
catch {
    Write-Error $_.Exception.Message
    exit 1
}
