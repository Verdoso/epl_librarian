[![made-with-java](https://img.shields.io/badge/Made%20with-Java-ca0000.svg)](https://openjdk.java.net/)
[![made-with-vue](https://img.shields.io/badge/Made%20with-Vue.js-4fc08d.svg)](https://vuejs.org/)
[![made-with-vue](https://img.shields.io/badge/Made%20with-Spring%20Boot-348c25.svg)](https://spring.io/projects/spring-boot)

# EPL Librarian [![GitHub release](https://shields.io/github/v/release/Verdoso/epl_librarian)]
Repositorio para una mini-aplicación para consultar localmente el catálogo de EPL

## Últimas versiones
[//]: # (BEGIN)
* Release 1.17.41: https://github.com/Verdoso/epl_librarian/releases/tag/Release_Tag_v1.17.41
* Release 1.17.39: https://github.com/Verdoso/epl_librarian/releases/tag/Release_Tag_v1.17.39
* Release 1.17.38: https://github.com/Verdoso/epl_librarian/releases/tag/Release_Tag_v1.17.38

[//]: # (END)

## Para ejecutar

Para ejecutar EPL Librarian hay tres formas de hacerlo:
* La versión Java multiplataforma, que sirve para cualquier S.O. con Java instalado.
* La versión nativa para Windows, para aquellos usuarios de Windows que no quieran tener que instalarse Java y prefiera un simple ejecutable.
* La versión docker, para los más frikis del lugar que tengan Docker instalado y no quieran tener que descargarse manualmente ningún fichero.

### Versión Java multiplataforma
Esta versión sirve para cualquier S.O. y es la que hay que usar, de momento, en Linux o MacOS.

#### Requisitos
* Tener instalado Java 17 o superior (Por ejemplo, del proyecto abierto y gratuito Temurin https://adoptium.net/es/temurin/releases)
* Tener configurado el comando java en el path (lo podemos comprobar ejecutando java -version en un terminal)

#### Ejecución
Descargar el fichero epl_librarian-\*.\*.\*.zip, descomprimirlo en un directorio sin espacios y ejecutar el fichero .cmd o .sh dependiendo de si estamos en Windows o \*nix. También podemos ejecutarlo directamente en un terminal con el comando `java  -Xms1g -Xmx1g -jar epl_librarian-version.jar`. Ojo que si hacemos doble-click en el .jar Windows es posible que nos arranque la aplicación en segundo plano y para matarla tengamos que usar el botón "Cerrar" de la aplicación (arriba a la derecha) o usar el "Administrador de tareas" de Windows.

### Versión nativa para Windows
Esta versión se puede ejecutar en Windows 10 sin que haga falta tener Java instalado

#### Requisitos
* Windows 10

#### Ejecución
Descargar el fichero epl_librarian-\*.\*.\*-Windows.zip, descomprimirlo en un directorio sin espacios y ejecutar el fichero .exe que contiene. Seguramente al ejecutar por primera vez una versión, Windows nos dirá que es un fichero sin editor conocido y tengamos que darle permisos para ejecutarlo y para acceder a la red. Le damos permisos y ya está, no tendremos que volver a dárselos.

### Versión docker
Esta versión se puede ejecutar desde cualquier cliente que tenga Docker instalado, independientemente del S.O.

#### Requisitos
* Cliente docker instalado

#### Ejecución
Primero hay que decidir donde queremos almacenar las preferencias y los ficheros temporales, ya que si almacenaramos los ficheros dentro del contenedor docker se perderían cada vez que iniciáramos un nuevo contenedor. Ese directorio para las preferencias y los temporales lo mapearemos como volumen a `/librarian`. Después, opcionalmente, si queremos la integración con Calibre tenemos que mapear el volumen `/calibre` al directorio donde tengamos almacenados los libros. Una vez decidido esto, ejecutamos un contenedor con la imagen `verdoso/epl_librarian:version_a_ejecutar` que mapee el puerto `7070` al puerto donde queramos acceder a nuestra aplicación y con los volumenes mencionados anteriormente.

Por ejemplo: Estando en Windows y queriendo las preferencias y los directorios en el directorio %HOME%\.librarian y teniendo la libreria de Calibre (metadata.db) en D:\libreria_calibre, podriamos usar un comando tal que así:

```
docker run --rm -p 7070:7070 -v %HOME%\.librarian:/librarian -v D:\libreria_calibre:/calibre verdoso/epl_librarian:1.17.39
```

Para utilizar cualquiera de las opciones avanzadas al ejecutar la imagen Docker, debemos pasar las  opciones como variables de entorno docker. Por ejemplo, si queremos que no intente acceder a ePubLibre (flag -Ddescarga_epl=false en el modo Java), en el modo docker debemos añadir `-e descarga_epl=false` al comando de ejecución, y quedaría así:

```
docker run --rm -p 7070:7070 -v %HOME%\.librarian:/librarian -v D:\libreria_calibre:/calibre -e descarga_epl=false verdoso/epl_librarian:1.17.39
```

**Nota**: En docker, el modo '**superportable**' no tiene mucho sentido, así que dicha opción se ignora.

### Acceder a la aplicación
En cualquiera de las versiones, al acabar de preparar el catalogo (aparece en el log el mensaje `EPL Librarian inicializado`), debería abrirse automáticamente una ventana del navegador apuntando a la direccion http://localhost:7070/librarian/, si no es así (como por ejemplo al ejecutar desde Docker o en Linux) tendremos que escribirla nosotros en un navegador para acceder.

## Actualizaciones de ePubLibre
Hay que tener en cuenta que:
* El programa almacena los ficheros del catalogo en el directorio temporal del usuario
* Si el catalogo almacenado tienes mas de 24h, intentará descargarse una versión nueva desde ePubLibre
* En caso de no poder descargarse una versión nueva, intentará cargar un fichero que se haya descargado manualmente y colocado en el directorio temporal.

## Actualizaciones de esta misma aplicación
A partir de la versión 1.17.30, EPL Librarian consulta este mismo README en GitHub para ver si existe una nueva versión y, si es el caso, muestra un un botón de aviso, al lado del botón de cerrar, que enlaza con la página desde donde se puede uno descargar la nueva versión. Esta funcionalidad es meramente informativa y lo único que se hace es consultar la página README como si fuese un navegador cualquiera, pero en caso de no querer que el programa acceda a la página de GitHub, se puede deshabilitar esta opción añadiendo la propiedad ```skip_version_check``` a true a nuestro fichero de preferencias.

![Botón que indica que hay una nueva versión publicada](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/NuevaVersion.PNG)


## Uso avanzado
* En caso de que no queramos que intente descargarse una versión nueva de la biblioteca de ePubLibre, podemos añadir al arrancar la propiedad ```-Ddescarga_epl=false```. En ese caso intentará usar el backup o el fichero descargado manualmente, pero en ningún caso intentará acceder a ePubLibre.
* Si queremos incrementar el número de horas antes de descargar una una versión nueva de la biblioteca de ePubLibre (por defecto 24h) podemos incrementarlo, por ejemplo a 48h, añadiendo la propiedad ```-Dantiguedad_maxima=48``` al arranque.
* Si queremos que directamente ignore la antiguedad del backup y si tiene uno lo use, podemos hacerlo añadiendo al arranque la propiedad ```-Dactualizacion_automatica=false```. Si no hay backup intentará la descarga de ePubLibre, pero si lo hay no la intentará.
* Modo '**superportable**'. Si queremos que el programa no cree ningún fichero fuera del directorio del programa, podemos pasarle el flag ```-Dsuperportable=true```. En ese caso, el fichero de preferencias y los ficheros temporales/backup de las descargas se almacenaran el el mismo directorio desde donde se lanza el programa, en los directorios .librariany y tmp. **Ojo** Eso quiere decir que al cambiar de versión del programa, tendremos que copiar las preferencias (directorio .librarian) y los ficheros temporales (directorio tmp) para no perder esos datos.
* Si queremos que se muestren las miniaturas de las portadas en la tabla principal, sin tener que abrir el detalle de cada libro para verlas, podemos añadir la propiedad ```thumbnails_in_main``` a true en nuestro fichero de preferencias. Con eso podremos ver las portadas de los libros directamente sin abrir los detalles.
* Por defecto, el programa intenta descargarse una nueva versión de la biblioteca de ePubLibre únicamente al arrancar. Si queremos poder refrescar el fichero (normalmente solo es útil desde la versión docker si no reiniciamos el contenedor), podemos añadir la propiedad ```-Dcan_reload_epl=true``` al arranque. Con eso podremos ver un botón en el sumario para intentar recargar los libros. El refresco de datos sigue las mismas reglas de caducidad de backups e indicaciones de las preferencias.

![Tabla de libros mostrando directamente las miniaturas de las portadas](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/miniaturasLibros.png)

## Integración, opcional, con Calibre
Si tenemos instalado el software para gestión de libros [Calibre](https://calibre-ebook.com/es) en el mismo ordenador donde se ejecuta EPL Librarian y queremos que nos compruebe los libros que tenemos, podemos hacerlo especificándole donde se encuentra la biblioteca de Calibre en nuestro ordenador, en las preferencias, de la siguiente forma:

Con el EPL Librarian apagado, editamos el fichero $HOME/.librarian/preferences.properties ( EPL Librarian nos indica la localización exacta en la consola al arrancar ) y añadimos una propiedad ```calibre_home``` indicándonle el camino a la biblioteca en formato "properties de Java"(el directorio donde Calibre guarda su fichero metadata.db).
Por ejemplo, si Calibre guarda los libros en un directorio 'D:\Libros' podríamos añadir a preferences.properties la siguiente linea
calibre_home=D\\:/Libros
Es necesario añadir una barra inversa '\\' antes del caracter ':' y cambiar las barras '\\' del path por barras '/' para evitar que haya problemas, ya que esos caracteres son especiales en los ficheros de properties.

Al arrancar EPL Librarian nos indicará que integración con Calibre está habilitada, tardará un poco más en arrancar y en la biblioteca aparecera unas nuevas columnas que nos indicarán, para cada libro, si se ha encontrado dicho libre en nuestra biblioteca Calibre y si ese libro lo "descartamos". También nos aparecerá un filtro nuevo para ocultar los libros que ya tengamos en nuestra biblioteca de Calibre y/o los que hayamos descartado. Para descartar o habilitar de nuevo un libro basta con hacer click en el simbolo del ojo en la fila correspondiente. De esta forma es muy sencillo saber si tenemos todos los libros que nos interesan de nuestros autores favoritos, simplemente marcando los filtros de "autores favoritos", "idiomas favoritos" y "ocultar libros que tenemos y los descartados".

La comprobación mira el título exacto del libro y sus autores, por lo que puede ser que no encuentre algún libro que no tenga exactamente el mismo título (p.e. 'El Camel Club' vs 'Camel Club') o que tenga alguna errata en el nombre (p.e. 'James S.A. Corey' vs 'James S. A. Corey'). Se ha optado por quedarse corto y decir que hay libros que no tenemos, por pequeñas diferencias en los datos, que posibilitar los errores al reves y dar por hecho que tenemos ciertos libros por ser demasiado generosos en las comprobaciones.

### Actualización de la biblioteca de Calibre (por defecto, desactivada)
Si queremos que el programa actualice la base de datos de Calibre con los identificadores de los libros de EPubLibre, podemos hacerlo configurando la propiedad ```calibre_update``` a true. De esta forma, cuando encuentre un libro en Calibre que se encuentre en la lista de libros de EPubLibre pero no tenga bien configurado su epl_id, actualizará la BDD del Calibre para ponérselo. Los libros que tienen configurado el epl_id correctamente se reconocen mucho más rápido durante la sincronización.


### Sincronización con Calibre en ejecución
El programa sincroniza automáticamente los datos de Calibre al arrancar, pero también es posible actualizar los datos mientras el programa está en marcha haciendo click con el ratón sobre el botón de "Integración con Calibre":

![Botón que indica la integración con Calibre](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/IntegracionCalibre.png)

Esta funcionalidad es útil cuando estamos añadiendo al Calibre libros que nos faltán y queremos actualizar los resultados del filtro "Ocultar los que tengo/descartados".

## Funcionamiento
El programa ofrece varios listados

### Listado principal (libros)
En este listado se muestran los datos básicos de los libros y se puede filtrar y ordenar por distintos criterios (título, colección, autor, idioma, género). En este listado se puede escoger una fecha (indicada por "Considerar novedades libros posteriores a") para que únicamente se muestres los libros disponibles en ePubLibre a partir de esa fecha. Para que el filtro se lleve a cabo hay que activar el interruptor "Solo novedades". Si le damos al botón de guardar almacenará la fecha y la recordará para posteriores arranques. Por otro lado, si hemos marcado como favoritos algunos idiomas, generos o autores, podemos activar los filtros para que solo se muestren los libros que cumplan esos criterios. Si tenemos activa la integración con Calibre también tendremos el filtro de ocultar los libros que ya tenemos o que hemos descartado, y veremos las columnas correspondientes a esos valores.

![Imagen de la página principal](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/Screen_Principal.PNG)

Podemos ampliar la información de cada libro, haciendo clic en la marca al lado del título, y así podremos ver más detalles del libro, como la sinopsis, la portada, un enlace a la página del libro en EPL y los enlaces magnet ofrecidos por EPL.

![Imagen del detalle de un libro](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/Screen_Detalle.PNG)

### Listado ordenable/filtrable de autores
En esta sección podemos consultar los autores de los libros del catalogo, podemos marcarlos como favoritos y podemos seleccionar uno en concreto (doblec clic) para añadirlo como filtro en la página principal.
![Imagen del listado de autores](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/Screen_Autores.PNG)

### Listado ordenable/filtrable de géneros
En esta sección podemos consultar los géneros bajo los que están clasificados los libros del catalogo, podemos marcarlos como favoritos y podemos seleccionar uno en concreto (doblec clic) para añadirlo como filtro en la página principal.
![Imagen del listado de géneros](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/Screen_Generos.PNG)

### Listado ordenable/filtrable de idiomas
En esta sección podemos consultar los idiomas de los libros del catalogo, podemos marcarlos como favoritos y podemos seleccionar uno en concreto (doblec clic) para añadirlo como filtro en la página principal. Funciona exactamente igual a los listados anteriores de autores y géneros.

### Sección de gráficos
En esta sección podemos encontrar unos gráficos sobre la distribución de autores, géneros e idiomas. En el caso de autores y géneros solo se muestran los más populares, dado el volumen de datos. El gráfico de doughnut de idiomas es interactivo y podemos habilitar/deshabilitar idiomas para poder ver el resto de idiomas más claramente.
En caso de tener la integración con Calibre habilitada, podremos optar por ver los mismos gráficos pero solo de los libros de EPL que tenemos en nuestra libreria de Calibre a través del check (Mi biblioteca Calibre).
![Imagen de la sección de gráficos - gráfico de autores](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/seccion_graficos.jpg)

### Opción de salto en la paginación
A partir de la version 0.0.41, en el listado de libros se puede saltar directamente a una página introduciendo el número deseado en el campo al lado de los botones de "siguiente" y "anterior" de la paginación.
![Imagen de la opción de salto de página](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/InputGoTo.PNG)

### Opciones de minimización de la interfaz
Se han añadido opciones para poder minimizar la parte de los menús de la interfaz y recogerlo todo y dejar el máximo espacio a la tabla de consulta de libros.

![Imagen de la opción para minimizar el menú](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/menu_encogido.jpg)
