@echo off
:: Gradle startup script for Windows
set DIR=%~dp0
set APP_BASE_NAME=%~n0
set DEFAULT_JVM_OPTS=

set CLASSPATH=%DIR%\gradle\wrapper\gradle-wrapper-main.jar;%DIR%\gradle\wrapper\gradle-wrapper-shared.jar

set JAVA_EXE=java.exe
if defined JAVA_HOME set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto execute
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.&exit /b 1

:execute
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
