@echo off
echo ===================================================
echo  TransitOps - Starting Java Spring Boot Backend
echo ===================================================
echo.
cd /d "%~dp0backend-java"
mvn spring-boot:run
pause
