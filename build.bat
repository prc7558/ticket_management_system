@echo off
REM ============================================================
REM Complaint & Ticket Management System - Build Script (Windows)
REM ============================================================

echo.
echo ============================================
echo   Building Complaint ^& Ticket Management System
echo ============================================
echo.

REM Create output directories
if not exist "out" mkdir out
if not exist "dist" mkdir dist

REM Check for MySQL connector
set MYSQL_JAR=lib\mysql-connector-j-8.3.0.jar
if not exist "%MYSQL_JAR%" (
    echo [WARNING] MySQL Connector JAR not found at %MYSQL_JAR%
    echo Please download mysql-connector-j-8.3.0.jar and place it in the lib\ folder.
    echo Download from: https://dev.mysql.com/downloads/connector/j/
    echo.
    set MYSQL_JAR=
)

REM Compile all Java sources
echo [1/3] Compiling Java sources...
if defined MYSQL_JAR (
    javac -d out -cp "%MYSQL_JAR%" --source 17 src\model\*.java src\util\*.java src\dao\*.java src\service\*.java src\ui\*.java
) else (
    javac -d out --source 17 src\model\*.java src\util\*.java src\dao\*.java src\service\*.java src\ui\*.java
)

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b 1
)
echo [OK] Compilation successful.

REM Copy config.properties to output
echo [2/3] Copying resources...
copy config.properties out\ >nul 2>&1
echo [OK] Resources copied.

REM Package as JAR
echo [3/3] Packaging JAR...
cd out
jar cfm ..\dist\TicketManager.jar ..\META-INF\MANIFEST.MF *
cd ..
echo [OK] JAR created at dist\TicketManager.jar

echo.
echo ============================================
echo   Build Complete!
echo ============================================
echo.
echo To run: java -jar dist\TicketManager.jar
echo (Ensure MySQL is running and schema.sql has been executed)
echo.
