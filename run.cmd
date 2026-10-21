@echo off
setlocal
cd /d "%~dp0"
chcp 65001 >nul
call build.cmd
if errorlevel 1 exit /b 1
java -cp out Main
exit /b %errorlevel%
