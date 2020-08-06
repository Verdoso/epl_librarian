package org.greeneyed.epl.librarian.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.Query;
import javax.transaction.Transactional;

import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.model.jpa.JAutor;
import org.greeneyed.epl.librarian.model.jpa.JGenero;
import org.greeneyed.epl.librarian.model.jpa.JIdioma;
import org.greeneyed.epl.librarian.model.jpa.JLibro;
import org.greeneyed.epl.librarian.repositories.AutorRepository;
import org.greeneyed.epl.librarian.repositories.GeneroRepository;
import org.greeneyed.epl.librarian.repositories.IdiomaRepository;
import org.greeneyed.epl.librarian.repositories.LibroRepository;
import org.greeneyed.epl.librarian.services.CalibreService;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.LibroCSV;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.UpdateSpec;
import org.greeneyed.epl.librarian.services.MapperService;
import org.greeneyed.epl.librarian.services.PreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class JPABibliotecaImplementation {

    private final AutorRepository autorRepository;
    private final GeneroRepository generoRepository;
    private final IdiomaRepository idiomaRepository;
    private final PreferencesService preferencesService;
    private final CalibreService calibreService;
    private final MapperService mapperService;

    @Autowired
    private LibroRepository libroRepository;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReadLock readLock = lock.readLock();
    private final WriteLock writeLock = lock.writeLock();

    @Data
    private class LibroSpec {
        private final LibroCSV libroCSV;
        private final String idioma;
        private final String autores;
        private final String generos;
        private final JLibro libro;

        public LibroSpec(LibroCSV libroCSV) {
            this.libroCSV = libroCSV;
            this.idioma = libroCSV.getIdioma();
            this.autores = libroCSV.getAutor();
            this.generos = libroCSV.getGeneros();
            this.libro = mapperService.toJPA(libroCSV);
        }
    }

    @Transactional
    public void update(UpdateSpec updateSpec) {
        writeLock.lock();
        try {
            // Inicializamos los autores
            updateSpec.getLibroCSVs()
                    .stream()
                    .map(LibroCSV::getAutor)
                    .flatMap(autor -> Stream.of(autor.split(" & ")))
                    .map(String::trim)
                    .distinct()
                    .map(JAutor::fromNombre)
                    .peek(autor -> autor.setFavorito(preferencesService.checkAutorFavorito(autor.getNombre())))
                    .forEach(autorRepository::save);
            // Inicializamos los generos
            updateSpec.getLibroCSVs()
                    .stream()
                    .map(LibroCSV::getGeneros)
                    .flatMap(autor -> Stream.of(autor.split(",")))
                    .map(String::trim)
                    .distinct()
                    .map(JGenero::fromNombre)
                    .peek(genero -> genero.setFavorito(preferencesService.checkGeneroFavorito(genero.getNombre())))
                    .forEach(generoRepository::save);
            // Inicializamos los idiomas
            updateSpec.getLibroCSVs()
                    .stream()
                    .map(LibroCSV::getIdioma)
                    .map(String::trim)
                    .distinct()
                    .map(JIdioma::fromNombre)
                    .peek(idioma -> idioma.setFavorito(preferencesService.checkIdiomaFavorito(idioma.getNombre())))
                    .forEach(idiomaRepository::save);
            // Inicializamos los libros
            List<JLibro> libros = updateSpec.getLibroCSVs()
                    .stream()
                    .map(LibroSpec::new)
//                    .peek(this::configuraIdioma)
//                    .peek(this::configuraAutores)
//                    .peek(this::configuraGeneros)
                    .map(LibroSpec::getLibro)
                    .collect(Collectors.toList());
            libroRepository.saveAll(libros);
            ;
            calibreService.updateLibros(SEARCH_AND_UDPATE_BOOK);
        } catch (Exception e) {
            log.error("Error", e);
        } finally {
            writeLock.unlock();
        }
    }

//    private void configuraGeneros(LibroSpec libroSpec) {
//        if (StringUtils.isNotBlank(libroSpec.getGeneros())) {
//            libroSpec.getLibro().setGeneros(new ArrayList<>());
//            Stream.of(libroSpec.getGeneros().split(",")).map(String::trim).distinct().forEach(nombreGenero -> {
//                JGenero genero = generoRepository.findByNombre(nombreGenero);
//                if (genero != null) {
//                    libroSpec.getLibro().getGeneros().add(genero);
//                } else {
//                    log.error("JGenero no encontrado: {} ", nombreGenero);
//                }
//            });
//        }
//    }
//
//    private void configuraAutores(LibroSpec libroSpec) {
//        if (StringUtils.isNotBlank(libroSpec.getAutores())) {
//            libroSpec.getLibro().setAutores(new ArrayList<>());
//            Stream.of(libroSpec.getAutores().split(" & ")).map(String::trim).distinct().forEach(nombreAutor -> {
//                JAutor autor = autorRepository.findByNombre(nombreAutor);
//                if (autor != null) {
//                    libroSpec.getLibro().getAutores().add(autor);
//                } else {
//                    log.error("JAutor no encontrado: {} ", nombreAutor);
//                }
//            });
//        }
//    }
//
//    private void configuraIdioma(LibroSpec libroSpec) {
//        if (StringUtils.isNotBlank(libroSpec.getIdioma())) {
//            JIdioma idioma = idiomaRepository.findByNombre(libroSpec.getIdioma().trim());
//            if (idioma != null) {
//                libroSpec.getLibro().setIdioma(idioma);
//            } else {
//                log.error("JIdioma no encontrado: {} ", libroSpec.getIdioma().trim());
//            }
//        }
//    }

    private void print(Object o) {
        log.info("{}", o);
    }

    private BiConsumer<String, String> SEARCH_AND_UDPATE_BOOK = (titulo, autores) -> {
        List<Libro> aModificar = new ArrayList<>();
        Set<String> autoresSet = Arrays.asList(autores.split(" & "))
                .stream()
                .map(String::trim)
                .map(Libro::flattenToAscii)
                .collect(Collectors.toSet());
        Specification<JLibro> query = Specification.where(LibroRepository.tituloIgual(titulo));
        for (String autor : autoresSet) {
            query = query.and(LibroRepository.contieneAutor(autor));
        }
        List<JLibro> libros = libroRepository.findAll(Specification.where(LibroRepository.tituloIgual(titulo)));
        log.info("Found: {}", libros.size());
//
//        try (final com.googlecode.cqengine.resultset.ResultSet<Libro> queryResult = libreria
//                .retrieve(getPartialQuery(Libro.flattenToAscii(titulo), Libro.LIBRO_TITULO))) {
//            queryResult.stream().forEach(libro -> {
//                Set<String> autoresSetLibro = libro.getListaAutores()
//                        .stream()
//                        .map(Libro::flattenToAscii)
//                        .collect(Collectors.toSet());
//                if (autoresSetLibro.containsAll(autoresSet) && autoresSet.containsAll(autoresSetLibro)) {
//                    aModificar.add(libro);
//                }
//            });
//        }
        for (Libro libro : aModificar) {
            log.debug("JLibro encontrado en Calibre: {}", libro.getTitulo());
            libro.setInCalibre(true);
//            libreria.remove(libro);
//            libreria.add(libro);
        }
    };
}
