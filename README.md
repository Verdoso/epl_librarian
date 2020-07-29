# epl_librarian
Repositorio para una mini-aplicación para consultar localmente el catálogo de EPL

## Para ejecutar
Simplemente descargar el binario y ejecutar en un terminal con java -jar epl-librarian-version.jar. Al acabar de preparar el catalogo, debería abrirse automáticamente una ventana del navegador a la direccion http://localhost:7070/librarian/. Ojo que si hacemos doble-click en Windows es posible que nos arranque la aplicación en segundo plano y para matarla tengamos que usar el botón "Cerrar" de la aplicación (arriba a la derecha) o usar el "Administrador de tareas" de Windows.

### Requisitos
* Tener instalado Java 8 o superior

## Actualizaciones de ePubLlibre
Hay que tener en cuenta que:
* El programa almacena el catalogo en el directorio temporal del usuario
* Si el catalogo almacenado tienes mas de 24, intentará descargarse una versión nueva desde ePubLlibre
* En caso de no poder descargarse una versión nueva, intenta cargar un fichero que se haya descargado manualmente y colocado en el directorio temporal.

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
