@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "INSTALL_DIR=%USERPROFILE%\AppData\Local\Programs\Common\jOpenAlert"

echo ====== jOpenAlert Startup Script ======

if not exist "%INSTALL_DIR%" (
    echo jOpenAlert installation directory not found: %INSTALL_DIR%
    echo Please ensure jOpenAlert is installed correctly. Check README.md for installation instructions.
    exit /b 1
)

:wait_for_network
echo Checking for network connectivity...
ping.exe -n 1 -w 1000 1.1.1.1 >nul
if errorlevel 1 (
	echo No response received. Trying again in 30 seconds...
	timeout /t 30 /nobreak >nul
	goto wait_for_network
)

echo Network connectivity established. Proceeding to launch jOpenAlert (latest version)...
set "JAR_PATH="
for /f "delims=" %%F in ('dir /b /a-d /o-d "%INSTALL_DIR%\jOpenAlert-*.jar" 2^>nul') do (
	set "JAR_PATH=%INSTALL_DIR%\%%F"
	goto launch
)

if not defined JAR_PATH (
    echo Error: No jOpenAlert JAR file found in "%INSTALL_DIR%".
    exit /b 1
)

:launch
echo Launching jOpenAlert...
start "jOpenAlert" java -jar "!JAR_PATH!"

echo jOpenAlert launched successfully.
exit /b 0