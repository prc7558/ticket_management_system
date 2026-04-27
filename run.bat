@echo off
REM ============================================================
REM Quick Run Script - Compiles and runs the application directly
REM ============================================================

if not exist "out" mkdir out

REM ── Set classpath: prefer downloaded JAR, fall back to current dir ──
set JDBC_JAR=lib\mysql-connector-j-8.3.0.jar
if not exist "%JDBC_JAR%" (
    echo [ERROR] MySQL JDBC driver not found at: %JDBC_JAR%
    echo Please run the following command to download it:
    echo   powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar' -OutFile 'lib\mysql-connector-j-8.3.0.jar'"
    pause
    exit /b 1
)

echo [1/3] Compiling sources...
javac -d out -cp "%JDBC_JAR%" --release 17 ^
    src\model\*.java ^
    src\util\*.java ^
    src\dao\*.java ^
    src\service\*.java ^
    src\ui\*.java

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. See errors above.
    pause
    exit /b 1
)
echo [OK] Compilation successful.

echo [2/3] Copying resources...
xcopy /Y config.properties out\ >nul 2>&1
echo [OK] Resources copied.

echo [3/3] Starting application...
java -cp "out;%JDBC_JAR%" ui.LoginFrame

