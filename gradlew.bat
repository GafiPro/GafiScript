@echo off
setlocal

where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle %*
  exit /b %errorlevel%
)

where curl.exe >nul 2>nul
if %errorlevel% neq 0 (
  echo Gradle was not found and curl.exe is unavailable.
  exit /b 1
)

where powershell.exe >nul 2>nul
if %errorlevel% neq 0 (
  echo PowerShell was not found.
  exit /b 1
)

set "VERSION=9.6.1"
set "ROOT=%~dp0.gradle-bootstrap"
set "ZIP=%ROOT%\gradle-%VERSION%-bin.zip"
set "DIST=%ROOT%\gradle-%VERSION%"

if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%ROOT%" mkdir "%ROOT%"
  curl.exe -L --fail --silent --show-error "https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip" -o "%ZIP%"
  if %errorlevel% neq 0 exit /b %errorlevel%

  powershell.exe -NoProfile -Command "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%ROOT%' -Force"
  if %errorlevel% neq 0 exit /b %errorlevel%
)

call "%DIST%\bin\gradle.bat" %*
exit /b %errorlevel%
