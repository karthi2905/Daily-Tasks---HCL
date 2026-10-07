@echo off
SET JAVA_HOME=C:\Program Files\Java\jdk-25
SET PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"
echo ========================================================
echo Compiling and Running Monthly Usage Analyser (Day 2)...
echo ========================================================
"C:\Users\karth\scoop\apps\maven\current\bin\mvn.cmd" clean compile exec:java
pause
