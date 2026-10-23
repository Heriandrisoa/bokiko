@echo off
setlocal
rem Runs the app from any working directory.
rem Usage: run.cmd [-Djetty.http.port=9090]
mvn -f "%~dp0pom.xml" org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.40:run %*
exit /b %ERRORLEVEL%
