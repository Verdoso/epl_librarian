package org.greeneyed.epl.librarian.services;

import org.greeneyed.epl.librarian.model.Autor;
import org.greeneyed.epl.librarian.model.Genero;
import org.greeneyed.epl.librarian.model.Idioma;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.model.Pagina;
import org.greeneyed.epl.librarian.model.Sumario;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.UpdateSpec;
import org.greeneyed.epl.librarian.services.model.BusquedaElemento;
import org.greeneyed.epl.librarian.services.model.BusquedaElemento.ElementOrdering;
import org.greeneyed.epl.librarian.services.model.BusquedaLibro;

public interface BibliotecaService {

    public enum BOOK_ORDERING {
        POR_AUTOR, POR_CALIBRE, POR_COLECCION, POR_IDIOMA, POR_PUBLICADO, POR_TITULO
    };

    public enum AUTOR_ORDERING implements ElementOrdering<Autor> {
        POR_AUTOR, POR_LIBROS;

        @Override
        public Class<Autor> getOrderingClass() {
            return Autor.class;
        }
    };

    public enum GENERO_ORDERING implements ElementOrdering<Genero> {
        POR_GENERO, POR_LIBROS;

        @Override
        public Class<Genero> getOrderingClass() {
            return Genero.class;
        }
    };

    public enum IDIOMA_ORDERING implements ElementOrdering<Idioma> {
        POR_IDIOMA, POR_LIBROS;

        @Override
        public Class<Idioma> getOrderingClass() {
            return Idioma.class;
        }
    };

    void update(UpdateSpec updateSpec);

    Sumario getSumario();

    Pagina<Libro> paginaLibros(BusquedaLibro busquedaLibro);

    Pagina<Autor> paginaAutor(BusquedaElemento<Autor> busquedaAutor);

    Pagina<Genero> paginaGenero(BusquedaElemento<Genero> busquedaGenero);

    Pagina<Idioma> paginaIdioma(BusquedaElemento<Idioma> busquedaIdioma);

}