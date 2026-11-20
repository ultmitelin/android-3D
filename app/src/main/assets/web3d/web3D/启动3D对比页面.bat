@echo off
cd /d "%~dp0"

netstat -ano | findstr ":8000 " | findstr "LISTENING" >nul
if %errorlevel%==0 goto openpage

echo Starting local server...

rem Prefer the py launcher (bundled with python.org installer), then PATH python
where py >nul 2>nul
if %errorlevel%==0 (
  start "3D-Server (close this window to stop)" py -3 -m http.server 8000
  goto wait
)

where python >nul 2>nul
if %errorlevel%==0 (
  start "3D-Server (close this window to stop)" python -m http.server 8000
  goto wait
)

echo [ERROR] Python 3 not found. Install it from https://www.python.org/downloads/
echo         and check "Add python.exe to PATH" during installation.
pause
exit

:wait
timeout /t 2 /nobreak >nul

:openpage
start "" "http://localhost:8000/3d-model-comparison.html"
exit