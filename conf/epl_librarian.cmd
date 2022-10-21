@ECHO OFF
CHCP 1250 > NUL

ECHO Comprobando instalación de Java...
where /q java || ECHO No hemos podido detectar java instalado en su sistema. No se puede ejecutar el programa. && PAUSE && EXIT /B
ECHO Java detectado!

ECHO Comprobando Java instalado...
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVAVER=%%g"
set JAVAVER=%JAVAVER:"=%

for /f "delims=. tokens=1-3" %%v in ("%JAVAVER%") do set "jver=%%v.%%w"
ECHO Java instalado= %jver%

if %jver% LSS 17 ECHO La versión minima de Java soportada es la 17. No se puede ejecutar el programa. && PAUSE && EXIT /B
ECHO Java instalado compatible!

ECHO Ejecutando...
@ECHO ON

java -Xms1g -Xmx1g -jar epl_librarian-${project.version}.jar
PAUSE
