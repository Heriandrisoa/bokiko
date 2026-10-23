# Runs the app from any working directory.
# Usage: .\run.ps1 -Djetty.http.port=9090
$ErrorActionPreference = 'Stop'
$pom = Join-Path $PSScriptRoot 'pom.xml'
& mvn -f $pom 'org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.40:run' @args
exit $LASTEXITCODE
