@echo off
setlocal
cd /d "%~dp0"

set MODE=%1
if "%MODE%"=="" set MODE=--demo

echo ================================================================================
echo   Compiling and Running Order Processor: Exception Handling (Day 6)...
echo   Mode: %MODE%
echo ================================================================================

if not exist out mkdir out
javac -d out src\com\technova\orders\exception\*.java src\com\technova\orders\model\*.java src\com\technova\orders\service\*.java src\com\technova\orders\app\*.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Direct javac compilation failed!
    exit /b %ERRORLEVEL%
)

java -cp out com.technova.orders.app.OrderApplication %MODE%

echo.
echo ================================================================================
echo                     EXECUTION COMPLETED
echo ================================================================================
endlocal
