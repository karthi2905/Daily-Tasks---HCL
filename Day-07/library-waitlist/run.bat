@echo off
setlocal
cd /d "%~dp0"

set MODE=%1
if "%MODE%"=="" set MODE=--app

echo ================================================================================
echo   Compiling and Running Library Waitlist and Collections (Day 7)...
echo   Target: %MODE%
echo ================================================================================

if not exist out mkdir out
javac -d out src\com\technova\library\model\*.java src\com\technova\library\service\*.java src\com\technova\library\app\*.java benchmark\CollectionBenchmark.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)

if "%MODE%"=="--benchmark" (
    java -cp out com.technova.library.benchmark.CollectionBenchmark
) else if "%MODE%"=="--all" (
    java -cp out com.technova.library.app.LibraryApplication
    echo.
    java -cp out com.technova.library.benchmark.CollectionBenchmark
) else (
    java -cp out com.technova.library.app.LibraryApplication
)

echo.
echo ================================================================================
echo                     EXECUTION COMPLETED
echo ================================================================================
endlocal
