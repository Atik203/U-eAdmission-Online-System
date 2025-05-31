@echo off
echo Starting UeAdmission Chat Server...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in the PATH.
    echo Please install Java and try again.
    pause
    exit /b 1
)

REM Set the classpath to include all necessary JAR files
set CLASSPATH=target\classes

REM Add all JAR files in the lib directory to the classpath
for %%i in (lib\*.jar) do call :append_classpath %%i

REM Check if MySQL JDBC driver exists in lib directory
set MYSQL_DRIVER_EXISTS=0
for %%i in (lib\mysql-connector-*.jar) do set MYSQL_DRIVER_EXISTS=1

if %MYSQL_DRIVER_EXISTS%==0 (
    echo ERROR: MySQL JDBC driver not found in lib directory.
    echo Please download mysql-connector-java-8.0.33.jar and place it in the lib directory.
    echo Download URL: https://repo1.maven.org/maven2/com/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
    echo.
    echo After downloading, place the JAR file in the lib directory and run this script again.
    pause
    exit /b 1
)

REM Add Maven repository MySQL driver to classpath if it exists
set MAVEN_REPO=%USERPROFILE%\.m2\repository\mysql\mysql-connector-java\8.0.33
if exist "%MAVEN_REPO%\mysql-connector-java-8.0.33.jar" (
    echo Found MySQL driver in Maven repository, adding to classpath...
    set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\mysql-connector-java-8.0.33.jar
)

REM Run the standalone chat server
echo Starting chat server on default port 9001...
java -cp %CLASSPATH% com.ueadmission.chat.server.StandaloneChatServer
goto :eof

:append_classpath
set CLASSPATH=%CLASSPATH%;%1
goto :eof
