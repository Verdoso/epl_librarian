# epl_librarian
Repositorio para una mini-aplicación para consultar localmente el catálogo de EPL

## Para ejecutar
Simplemente descargar el binario y ejecutar en un terminal con java -jar epl-librarian-version.jar. Al acabar de preparar el catalogo, debería abrirse automáticamente una ventana del navegador a la direccion http://localhost:7070/librarian/. Ojo que si hacemos doble-click en Windows es posible que nos arranque la aplicación en segundo plano y para matarla tengamos que usar el "Administrador de tareas" de Windows.

### Requisitos
* Tener instalado Java 8 o superior

## Actualizaciones de ePubLlibre
Hay que tener en cuenta que:
* El programa almacena el catalogo en el directorio temporal del usuario
* Si el catalogo almacenado tienes mas de 24, intentará descargarse una versión nueva desde ePubLlibre
* En caso de no poder descargarse una versión nueva, intenta cargar un fichero que se haya descargado manualmente y colocado en el directorio temporal.

## Funcionamiento
El programa ofrece varios listados:
* Libros: En este listado se muestran los datos básicos de los libros y se puede filtrar y ordenar por distintas categorías
* Autores: Listado ordenable/filtrable de autores, al hacer doble click sobre uno de ellos se añade como filtro en el listado de libros.
* Géneros: Listado ordenable/filtrable de géneros, al hacer doble click sobre uno de ellos se añade como filtro en el listado de libros.
* Idiomas: Listado ordenable/filtrable de idiomas, al hacer doble click sobre uno de ellos se añade como filtro en el listado de libros.

En el listado de libros se puede escoger una fecha (indicada por "Considerar novedades libros posteriores a") para que únicamente se muestres los libros disponibles en ePubLlibre a partir de esa fecha. Para que el filtro se lleve a cabo hay que activar el interruptor "Solo novedades".
Si le damos al botón de guardar almacenará la fecha y la recordará para posteriores arranques.
