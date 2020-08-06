package org.greeneyed.epl.librarian.services.impl;

import org.greeneyed.epl.librarian.model.Autor;
import org.greeneyed.epl.librarian.model.Genero;
import org.greeneyed.epl.librarian.model.Idioma;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.model.Pagina;
import org.greeneyed.epl.librarian.model.Sumario;
import org.greeneyed.epl.librarian.services.BibliotecaService;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.UpdateSpec;
import org.greeneyed.epl.librarian.services.model.BusquedaElemento;
import org.greeneyed.epl.librarian.services.model.BusquedaLibro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class BibliotecaDobleImplementation implements BibliotecaService {

    private final CQEngineBibliotecaImplementation cQEngineBibliotecaImplementation;
    private final JPABibliotecaImplementation jpaBibliotecaImplementation;

    @Override
    public void update(UpdateSpec updateSpec) {
        //cQEngineBibliotecaImplementation.update(updateSpec);
        jpaBibliotecaImplementation.update(updateSpec);
    }

    @Override
    public Sumario getSumario() {
        return cQEngineBibliotecaImplementation.getSumario();
    }

    @Override
    public Pagina<Libro> paginaLibros(BusquedaLibro busquedaLibro) {
        return cQEngineBibliotecaImplementation.paginaLibros(busquedaLibro);
    }

    @Override
    public Pagina<Autor> paginaAutor(BusquedaElemento<Autor> busquedaAutor) {
        return cQEngineBibliotecaImplementation.paginaAutor(busquedaAutor);
    }

    @Override
    public Pagina<Genero> paginaGenero(BusquedaElemento<Genero> busquedaGenero) {
        return cQEngineBibliotecaImplementation.paginaGenero(busquedaGenero);
    }

    @Override
    public Pagina<Idioma> paginaIdioma(BusquedaElemento<Idioma> busquedaIdioma) {
        return cQEngineBibliotecaImplementation.paginaIdioma(busquedaIdioma);
    }

}
