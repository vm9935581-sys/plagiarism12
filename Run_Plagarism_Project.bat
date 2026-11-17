
@echo off
title Plagarism Detection System
color 0A

set "BACKEND=C:\Users\Dell\OneDrive\Desktop\plagiarism-detection-system\backend\plagarism-detection"

echo ==========================================
echo    PLAGARISM DETECTION SYSTEM
echo ==========================================
echo.

if not exist "%BACKEND%\mvnw.cmd" (
    echo ERROR: Backend folder not found.
    pause
    exit /b 1
)

echo Checking if the project is already running...

powershell -NoProfile -Command "try {$r=Invoke-WebRequest 'http://localhost:8080/api/test' -UseBasicParsing -TimeoutSec 3; if ($r.StatusCode -eq 200 -and $r.Content -match 'Backend Working Successfully') {exit 0}} catch {}; exit 1"

if not errorlevel 1 goto OPEN_LOGIN

echo Checking port 8080...

for /f %%P in ('powershell -NoProfile -Command "(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty OwningProcess)"') do set "PID=%%P"

if defined PID (
    echo.
    echo Port 8080 is occupied by process ID %PID%.
    echo Close the old server window using Ctrl+C,
    echo then double-click this file again.
    pause
    exit /b 1
)

echo Starting backend...

start "Plagarism Detection Backend" /min cmd /k "cd /d "%BACKEND%" && mvnw.cmd spring-boot:run"

echo Waiting for the application to start...

powershell -NoProfile -Command "$ok=$false; for($i=0;$i -lt 60;$i++){try{$r=Invoke-WebRequest 'http://localhost:8080/api/test' -UseBasicParsing -TimeoutSec 2; if($r.StatusCode -eq 200 -and $r.Content -match 'Backend Working Successfully'){$ok=$true;break}}catch{}; Start-Sleep -Seconds 2}; if($ok){exit 0}else{exit 1}"

if errorlevel 1 (
    echo.
    echo The backend did not respond. Check its window for errors.
    pause
    exit /b 1
)

:OPEN_LOGIN
echo Opening login page...
start "" "http://localhost:8080/login.html"
exit /b 0
