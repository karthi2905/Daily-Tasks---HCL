@echo off
setlocal

echo ================================================================================
echo                     TECHNOVA PAYMENT PROCESSING -- DAY 5
echo ================================================================================

cd /d "%~dp0"

echo [1/2] Compiling Java source files...
if not exist "out" mkdir out

javac -d out src/com/technova/payment/model/*.java src/com/technova/payment/service/*.java src/com/technova/payment/app/*.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)
echo [SUCCESS] Compilation successful.

echo.
echo [2/2] Running PaymentApplication...
echo --------------------------------------------------------------------------------
java -cp out com.technova.payment.app.PaymentApplication
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Execution failed with exit code %ERRORLEVEL%!
    exit /b %ERRORLEVEL%
)

echo.
echo ================================================================================
echo                     EXECUTION COMPLETED SUCCESSFULLY
echo ================================================================================
endlocal
