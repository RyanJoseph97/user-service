@echo off
set WEBAPPS_PATH=C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\

echo Running Maven build...
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Exiting.
    exit /b %ERRORLEVEL%
)

echo Moving files to webapps directory...

if exist target\eventmaster.war (
    move /Y target\eventmaster.war "%WEBAPPS_PATH%"
) else (
    echo eventmaster.war not found!
)

if exist target\eventmaster (
    xcopy /E /I /Y "target\eventmaster" "%WEBAPPS_PATH%\eventmaster"
) else (
    echo eventmaster directory not found!
)

echo Done moving eventmaster files

call catalina.bat jpda start