EPL Librarian (versión ${project.version})
------------------------------------------
Para ejecutar el programa use el script adecuado para su sistema operativo (epl_librarian.cmd en Windows, epl_librarian.sh en sistemas basados en *nix)

Cuando acabe de actualizar la biblioteca, el programa abrira una pestaña del navegador con la interfaz de usuario. Si eso no ocurre, intente acceder manualmente accediendo al enlace http://localhost:7070/librarian/

Requisitos
----------
Java 8 o superior
Poder conectarse al servidor de ePubLibre
Usar el puerto 7070

Troubleshooting
---------------
Compruebe los errores que se muestran en la ventana al ejecutar el programa
Al ejecutar el programa se debería generar un fichero de log epl_librarian.log en el mismo fichero desde donde se ejecuta el programa, si no es así es que no se ha llegado ni a iniciar.
Con los datos obtenidos de estos dos ultimos pasos y el numero de versión del programa (${project.version}), consulte en https://github.com/Verdoso/epl_librarian/issues para ver si es un error conocido o abra una incidencia nueva.