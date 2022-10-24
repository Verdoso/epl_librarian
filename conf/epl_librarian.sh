#!/bin/bash

if type -p java > /dev/null
then
    echo "¡Java detectado!"
    _java=java
elif  [ ! -z ${JAVA_HOME+x} ]  && [ -f "$JAVA_HOME/bin/java" ] 
then
    echo "¡Java detectado por JAVA_HOME!"     
    _java="$JAVA_HOME/bin/java"
else
    echo "No hemos podido detectar java instalado en su sistema. No se puede ejecutar el programa."
fi

JAVA_VERSION=$($_java -version 2>&1 | sed -n ';s/.* version "\(.*\)\.\(.*\)\..*".*/\1\2/p;')

if [[ "$JAVA_VERSION" -ge 170 ]]; then
  echo "¡Versión de Java compatible!"
  $_java -Xms1g -Xmx1g -jar epl_librarian-@project.version@.jar
else         
    echo "La versión minima de Java soportada es la 17. No se puede ejecutar el programa."
fi

