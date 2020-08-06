package org.greeneyed.epl.librarian.services.model;

import java.time.LocalDate;

import org.greeneyed.epl.librarian.services.BibliotecaService.BOOK_ORDERING;

import lombok.Data;

@Data
public class BusquedaLibro {
    private final int numeroPagina;
    private final int porPagina;
    private final BOOK_ORDERING ordering;
    private final boolean reversed;
    private final String filtroTitulo;
    private final String filtroColeccion;
    private final String filtroAutor;
    private final String filtroGenero;
    private final String filtroIdioma;
    private final LocalDate filtroFecha;
    private final boolean soloAutoresFavoritos;
    private final boolean soloIdiomasFavoritos;
    private final boolean soloGenerosFavoritos;
    private final Boolean soloNoEnPropiedad;
}