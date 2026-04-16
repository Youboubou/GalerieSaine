@echo off
REM Script a lancer UNE SEULE FOIS au premier demarrage sous Windows
REM pour telecharger gradle-wrapper.jar officiel.

cd /d "%~dp0"

set WRAPPER_JAR=gradle\wrapper\gradle-wrapper.jar
set GRADLE_VERSION=8.10.2

if exist %WRAPPER_JAR% (
    for %%A in (%WRAPPER_JAR%) do if %%~zA GTR 0 (
        echo gradle-wrapper.jar existe deja, rien a faire.
        exit /b 0
    )
)

echo Telechargement de gradle.zip...
powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%TEMP%\gradle.zip'"
powershell -Command "Expand-Archive -Path '%TEMP%\gradle.zip' -DestinationPath '%TEMP%\gradle-extract' -Force"

for /r "%TEMP%\gradle-extract" %%F in (gradle-wrapper*.jar) do copy "%%F" "%WRAPPER_JAR%" >nul

rmdir /s /q "%TEMP%\gradle-extract"
del "%TEMP%\gradle.zip"

if exist %WRAPPER_JAR% (
    echo gradle-wrapper.jar installe avec succes !
) else (
    echo Echec. Ouvre simplement le projet dans Android Studio,
    echo il regenerera automatiquement le wrapper au premier Gradle Sync.
)
