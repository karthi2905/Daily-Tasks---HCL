@echo off
setlocal
cd /d "%~dp0"

set ARG=%1
if "%ARG%"=="" set ARG=data\products.csv

echo ================================================================================
echo   Compiling and Running Generic Toolkit (Day 9)...
echo   Argument: %ARG%
echo ================================================================================

if not exist out mkdir out
javac -d out src\com\technova\generics\model\*.java src\com\technova\generics\util\*.java src\com\technova\generics\service\*.java src\com\technova\generics\resource\*.java src\com\technova\generics\demo\*.java src\com\technova\generics\app\*.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Direct javac compilation failed!
    exit /b %ERRORLEVEL%
)

if "%ARG%"=="--app" (
    java -cp out com.technova.generics.app.GenericToolkitApplication data\products.csv
) else (
    java -cp out com.technova.generics.app.GenericToolkitApplication %ARG%
)

echo.
echo ================================================================================
echo                     EXECUTION COMPLETED
echo ================================================================================
endlocal
