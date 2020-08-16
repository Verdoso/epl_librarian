@ECHO OFF
CHCP 1250 > NUL

ECHO Comprobando instalación de Java...
where /q java || ECHO No hemos podido detectar java instalado en su sistema. No se puede ejecutar el programa. && PAUSE && EXIT /B
ECHO Java detectado!

ECHO Comprobando versión de Java...
for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -fullversion 2^>^&1') do set "jver=%%j%%k%%l%%m"
if %jver% LSS 180000 ECHO La versión minima de Java soportada es la 1.8. No se puede ejecutar el programa. && PAUSE && EXIT /B
ECHO La versión de Java es suficiente!

ECHO Ejecutando...
@ECHO ON

java -Xms1g -Xmx1g -jar epl_librarian-${project.version}.jar
PAUSE
