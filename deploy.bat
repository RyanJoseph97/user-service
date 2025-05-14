@echo off
set WEBAPPS_PATH=C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\

echo Running Maven build...
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Exiting.
    exit /b %ERRORLEVEL%
)

echo Moving files to webapps directory...

if exist target\user-service.war (
    move /Y target\user-service.war "%WEBAPPS_PATH%"
) else (
    echo user-service.war not found!
)

if exist target\user-service (
    xcopy /E /I /Y "target\user-service" "%WEBAPPS_PATH%\user-service"
) else (
    echo user-service directory not found!
)

echo Done moving user-service files

call catalina.bat jpda start