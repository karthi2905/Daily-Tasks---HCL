@echo off
setlocal
cd /d "%~dp0"

echo ================================================================================
echo        Compiling and Running Payment Hierarchy and Polymorphism (Day 5)...
echo ================================================================================

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn clean compile exec:java
) else if exist "C:\Users\karth\scoop\apps\maven\current\bin\mvn.cmd" (
    "C:\Users\karth\scoop\apps\maven\current\bin\mvn.cmd" clean compile exec:java
) else (
    echo [INFO] Maven not detected. Using direct JDK command-line toolchain...
    if not exist target\classes mkdir target\classes
    javac -d target\classes src\com\technova\payment\model\*.java src\com\technova\payment\service\*.java src\com\technova\payment\app\*.java
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Compilation failed!
        exit /b %ERRORLEVEL%
    )
    java -cp target\classes com.technova.payment.app.PaymentApplication
)

echo.
echo ================================================================================
echo                     EXECUTION COMPLETED SUCCESSFULLY
echo ================================================================================
endlocal
