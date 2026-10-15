@echo off
setlocal
cd /d "%~dp0"

set MODE=%1
if "%MODE%"=="" set MODE=--app

echo ================================================================================
echo   Compiling and Running Employee Directory and Object Contracts (Day 8)...
echo   Target: %MODE%
echo ================================================================================

if not exist out mkdir out
javac -d out src\com\technova\employeedirectory\model\*.java src\com\technova\employeedirectory\service\*.java src\com\technova\employeedirectory\demo\*.java src\com\technova\employeedirectory\app\*.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)

if "%MODE%"=="--demo" (
    java -cp out com.technova.employeedirectory.demo.HashCodeBugDemo
) else if "%MODE%"=="--all" (
    java -cp out com.technova.employeedirectory.app.EmployeeDirectoryApplication
    echo.
    java -cp out com.technova.employeedirectory.demo.HashCodeBugDemo
) else (
    java -cp out com.technova.employeedirectory.app.EmployeeDirectoryApplication
)

echo.
echo ================================================================================
echo                     EXECUTION COMPLETED
echo ================================================================================
endlocal
