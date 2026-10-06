@echo off
SET JAVA_HOME=C:\Program Files\Java\jdk-25
SET PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"
echo Starting TechNova Booking System...
"C:\Users\karth\scoop\apps\maven\current\bin\mvn.cmd" exec:java
pause
