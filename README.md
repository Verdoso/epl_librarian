[![made-with-java](https://img.shields.io/badge/Made%20with-Java-ca0000.svg)](https://openjdk.java.net/)
[![made-with-vue](https://img.shields.io/badge/Made%20with-Vue.js-4fc08d.svg)](https://vuejs.org/)
[![made-with-vue](https://img.shields.io/badge/Made%20with-Spring%20Boot-348c25.svg)](https://spring.io/projects/spring-boot)

# EPL Librarian [![GitHub release](https://img.shields.io/github/release/Verdoso/epl_librarian.svg)](https://GitHub.com/Verdoso/epl_librarian/releases/)
Repositorio para una mini-aplicación para consultar localmente el catálogo de EPL

## Para ejecutar
Simplemente descargar el fichero zip, descomprimirlo en un directorio sin espacios y ejecutar el fichero .cmd o .sh dependiendo de si estamos en Windows o \*nix. También podemos ejecutarlo directamente en un terminal con el comando java -jar epl_librarian-version.jar. Al acabar de preparar el catalogo, debería abrirse automáticamente una ventana del navegador apuntando a la direccion http://localhost:7070/librarian/. Ojo que si hacemos doble-click en Windows es posible que nos arranque la aplicación en segundo plano y para matarla tengamos que usar el botón "Cerrar" de la aplicación (arriba a la derecha) o usar el "Administrador de tareas" de Windows.

### Requisitos
* Tener instalado Java 8 o superior
* Tener configurado el comando java en el path (lo podemos comprobar ejecutando java -version en un terminal)

## Actualizaciones de ePubLlibre
Hay que tener en cuenta que:
* El programa almacena los ficheros del catalogo en el directorio temporal del usuario
* Si el catalogo almacenado tienes mas de 24h, intentará descargarse una versión nueva desde ePubLlibre
* En caso de no poder descargarse una versión nueva, intentará cargar un fichero que se haya descargado manualmente y colocado en el directorio temporal.

## Uso avanzado
* En caso de que no queramos que intente descargarse una versión nueva de la biblioteca de ePubLlibre, podemos añadir al arrancar la propiedad ```-Ddescarga_epl=false```. En ese caso intentará usar el backup o el fichero descargado manualmente, pero en ningún caso intentará acceder a ePubLlibre.
* Si queremos incrementar el número de horas antes de descargar una una versión nueva de la biblioteca de ePubLlibre (por defecto 24h) podemos incrementarlo, por ejemplo a 48h, añadiendo la propiedad ```-Dantiguedad_maxima=48``` al arranque.
* Si queremos que directamente ignore la antiguedad del backup y si tiene uno lo use, podemos hacerlo añadiendo al arranque la propiedad ```-Dactualizacion_automatica=false```. Si no hay backup intentará la descarga de ePubLlibre, pero si lo hay no la intentará.
* Modo '**superportable**'. Si queremos que el programa no cree ningún fichero fuera del directorio del programa, podemos pasarle el flag ```-Dsuperportable=true```. En ese caso, el fichero de preferencias y los ficheros temporales/backup de las descargas se almacenaran el el mismo directorio desde donde se lanza el programa, en los directorios .librariany y tmp. **Ojo** Eso quiere decir que al cambiar de versión del programa, tendremos que copiar las preferencias (directorio .librarian) y los ficheros temporales (directorio tmp) para no perder esos datos.

## Integración, opcional, con Calibre
Si tenemos instalado el software para gestión de libros [Calibre](https://calibre-ebook.com/es) en el mismo ordenador donde ejecutar EPL Librarian y queremos que nos compruebe los libros que tenemos, podemos hacerlo especificándole donde se encuentra la biblioteca de Calibre en nuestro ordenador en las preferencias.

Con el EPL Librarian apagado, editamos el fichero $HOME/.librarian/preferences.properties ( EPL Librarian nos indica la localización exacta en la consola al arrancar ) y añadimos una propiedad ```calibre_home``` indicándonle el camino a la biblioteca (el directorio donde Calibre guarda su fichero metadata.db).
Por ejemplo, si Calibre guarda los libros en un directorio 'D:/Libros' podríamos añadir a preferences.properties la siguiente linea
calibre_home=D:/Libros

Al arrancar EPL Librarian nos indicará que integración con calibre está habilitada, tardará un poco más en arrancar y en la biblioteca aparecera una nueva columna que nos indicará, para cada libro, si se ha encontrado dicho libre en nuestra biblioteca Calibre.

La comprobación mira el título exacto del libro y sus autores, por lo que puede ser que no encuentre algún libro que no tenga exactamente el mismo título (p.e. 'El Camel Club' vs 'Camel Club') o que tenga alguna errata en el nombre (p.e. 'James S.A. Corey' vs 'James S. A. Corey'). Se ha optado por quedarse corto y decir que hay libros que no tenemos, por pequeñas diferencias en los datos, que posibilitar los errores al reves y dar por hecho que tenemos ciertos libros por ser demasiado generosos en las comprobaciones.

## Funcionamiento
El programa ofrece varios listados

### Listado principal (libros)
En este listado se muestran los datos básicos de los libros y se puede filtrar y ordenar por distintos criterios (título, colección, autor, idioma, género). En este listado se puede escoger una fecha (indicada por "Considerar novedades libros posteriores a") para que únicamente se muestres los libros disponibles en ePubLlibre a partir de esa fecha. Para que el filtro se lleve a cabo hay que activar el interruptor "Solo novedades". Si le damos al botón de guardar almacenará la fecha y la recordará para posteriores arranques. Por otro lado, si hemos marcado como favoritos algunos idiomas, generos o autores, podemos activar los filtros para que solo se muestren los libros que cumplan esos criterios.

![Imagen de la página principal](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/Screen_Principal.PNG)

Podemos ampliar la información de cada libro, haciendo clic en la marca al lado del título, y así podremos ver más detalles del libro y el enlace magnet ofrecido por EPL.

![Imagen del detalle de un libro](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/Screen_Detalle.PNG)

### Listado ordenable/filtrable de autores
En esta sección podemos consultar los autores de los libros del catalogo, podemos marcarlos como favoritos y podemos seleccionar uno en concreto (doblec clic) para añadirlo como filtro en la página principal.
![Imagen del listado de autores](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/Screen_Autores.PNG)

### Listado ordenable/filtrable de géneros
En esta sección podemos consultar los géneros bajo los que están clasificados los libros del catalogo, podemos marcarlos como favoritos y podemos seleccionar uno en concreto (doblec clic) para añadirlo como filtro en la página principal.
![Imagen del listado de géneros](https://raw.githubusercontent.com/Verdoso/epl_librarian/master/docs/Screen_Generos.PNG)

### Listado ordenable/filtrable de idiomas
En esta sección podemos consultar los idiomas de los libros del catalogo, podemos marcarlos como favoritos y podemos seleccionar uno en concreto (doblec clic) para añadirlo como filtro en la página principal. Funciona exactamente igual a los listados anteriores de autores y géneros.
