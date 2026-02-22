@echo off

echo Running Maven build...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Exiting.
    exit /b %ERRORLEVEL%
)

echo Starting user-service...
cd target
java -jar user-service-1.0-SNAPSHOT.jar

echo user-service started successfully
