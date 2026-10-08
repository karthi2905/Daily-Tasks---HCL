@echo off
SET JAVA_HOME=C:\Program Files\Java\jdk-25
SET PATH=%JAVA_HOME%\bin;%PATH%
SET MAVEN_OPTS=-Xmx512m
cd /d "%~dp0"
echo ========================================================
echo Compiling and Running Bank Account OOP & Debugging (Day 4)...
echo ========================================================
if exist "C:\Users\karth\scoop\apps\maven\current\bin\mvn.cmd" (
    "C:\Users\karth\scoop\apps\maven\current\bin\mvn.cmd" clean compile exec:java
) else (
    where mvn >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        mvn clean compile exec:java
    ) else (
        if not exist out mkdir out
        javac -d out src/com/technova/bank/model/*.java src/com/technova/bank/service/*.java src/com/technova/bank/app/*.java
        java -cp out com.technova.bank.app.BankApplication
    )
)
pause
