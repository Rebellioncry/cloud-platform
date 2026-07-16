@echo off
title Nacos Config Push
powershell -ExecutionPolicy Bypass -File "%~dp0push-config.ps1"
echo.
pause
