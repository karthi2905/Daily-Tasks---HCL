@echo off
SET JAVA_HOME=C:\Program Files\Java\jdk-25
SET PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"
echo ========================================================
echo Compiling and Running ATM Simulator (Day 3)...
echo ========================================================
"C:\Users\karth\scoop\apps\maven\current\bin\mvn.cmd" clean package exec:java
pause
