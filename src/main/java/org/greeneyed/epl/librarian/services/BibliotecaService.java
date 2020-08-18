package org.greeneyed.epl.librarian.services;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.contains;
import static com.googlecode.cqengine.query.QueryFactory.descending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.greeneyed.epl.librarian.model.Autor;
import org.greeneyed.epl.librarian.model.Genero;
import org.greeneyed.epl.librarian.model.Idioma;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.model.Pagina;
import org.greeneyed.epl.librarian.model.PuedeSerFavorito;
import org.greeneyed.epl.librarian.model.Sumario;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.UpdateSpec;
import org.greeneyed.epl.librarian.services.model.BusquedaElemento;
import org.greeneyed.epl.librarian.services.model.BusquedaLibro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class BibliotecaService {

    public static interface ElementOrdering<T> {
        QueryOptions getQueryOptionsAscending();

        QueryOptions getQueryOptionsDescending();

        SimpleAttribute<T, String> getSortAttribute();
    }

    @Getter
    @AllArgsConstructor
    public enum IDIOMA_ORDERING implements ElementOrdering<Idioma> {
        POR_IDIOMA(queryOptions(orderBy(ascending(Idioma.IDIOMA_NOMBRE))),
                queryOptions(orderBy(descending(Idioma.IDIOMA_NOMBRE)))),
        POR_LIBROS(queryOptions(orderBy(ascending(Idioma.IDIOMA_LIBROS))),
                queryOptions(orderBy(descending(Idioma.IDIOMA_LIBROS))));

        private final QueryOptions queryOptionsAscending;
        private final QueryOptions queryOptionsDescending;
        private final SimpleAttribute<Idioma, String> sortAttribute = Idioma.IDIOMA_NOMBRE;

    }

    @Getter
    @AllArgsConstructor
    public enum GENERO_ORDERING implements ElementOrdering<Genero> {
        POR_GENERO(queryOptions(orderBy(ascending(Genero.GENERO_NOMBRE))),
                queryOptions(orderBy(descending(Genero.GENERO_NOMBRE)))),
        POR_LIBROS(queryOptions(orderBy(ascending(Genero.GENERO_LIBROS))),
                queryOptions(orderBy(descending(Genero.GENERO_LIBROS))));

        private final QueryOptions queryOptionsAscending;
        private final QueryOptions queryOptionsDescending;
        private final SimpleAttribute<Genero, String> sortAttribute = Genero.GENERO_NOMBRE;
    }

    @Getter
    @AllArgsConstructor
    public enum AUTOR_ORDERING implements ElementOrdering<Autor> {
        POR_AUTOR(queryOptions(orderBy(ascending(Autor.AUTOR_NOMBRE))),
                queryOptions(orderBy(descending(Autor.AUTOR_NOMBRE)))),
        POR_LIBROS(queryOptions(orderBy(ascending(Autor.AUTOR_LIBROS))),
                queryOptions(orderBy(descending(Autor.AUTOR_LIBROS))));

        private final QueryOptions queryOptionsAscending;
        private final QueryOptions queryOptionsDescending;
        private final SimpleAttribute<Autor, String> sortAttribute = Autor.AUTOR_NOMBRE;
    }

    @Getter
    @AllArgsConstructor
    public enum BOOK_ORDERING implements ElementOrdering<Libro> {
        POR_TITULO(queryOptions(orderBy(ascending(Libro.LIBRO_TITULO))),
                queryOptions(orderBy(descending(Libro.LIBRO_TITULO))))
        //
        , POR_PUBLICADO(queryOptions(orderBy(ascending(Libro.LIBRO_PUBLICADO), ascending(Libro.LIBRO_TITULO))),
                queryOptions(orderBy(descending(Libro.LIBRO_PUBLICADO), descending(Libro.LIBRO_TITULO))))
        //
        , POR_CALIBRE(queryOptions(orderBy(ascending(Libro.LIBRO_EN_CALIBRE), ascending(Libro.LIBRO_TITULO))),
                queryOptions(orderBy(descending(Libro.LIBRO_EN_CALIBRE), descending(Libro.LIBRO_TITULO))))
        //
        , POR_AUTOR(queryOptions(orderBy(ascending(Libro.LIBRO_AUTOR))),
                queryOptions(orderBy(descending(Libro.LIBRO_AUTOR))))
        //
        , POR_IDIOMA(queryOptions(orderBy(ascending(Libro.LIBRO_IDIOMA))),
                queryOptions(orderBy(descending(Libro.LIBRO_IDIOMA))))
        //
        ,
        POR_COLECCION(
                queryOptions(orderBy(ascending(Libro.LIBRO_COLECCION), ascending(Libro.LIBRO_VOLUMEN),
                        ascending(Libro.LIBRO_TITULO))),
                queryOptions(orderBy(descending(Libro.LIBRO_COLECCION), descending(Libro.LIBRO_VOLUMEN),
                        descending(Libro.LIBRO_TITULO))))
        //
        ;

        private final QueryOptions queryOptionsAscending;
        private final QueryOptions queryOptionsDescending;
        private final SimpleAttribute<Libro, String> sortAttribute = Libro.LIBRO_TITULO;
    }

    @Value("${git.build.version}")
    private String buildVersion;

    private LocalDateTime fechaActualizacion = null;

    private final MapperService mapperService;
    private final PreferencesService preferencesService;
    private final CalibreService calibreService;

    private final IndexedCollection<Libro> libreria = new ConcurrentIndexedCollection<>();
    private final IndexedCollection<Autor> autores = new ConcurrentIndexedCollection<>();
    private final IndexedCollection<Genero> generos = new ConcurrentIndexedCollection<>();
    private final IndexedCollection<Idioma> idiomas = new ConcurrentIndexedCollection<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final WriteLock writeLock = lock.writeLock();
    private final ReadLock readLock = lock.readLock();

    private Sumario sumario = null;

    @PostConstruct
    public void postConstruct() {
        libreria.addIndex(UniqueIndex.onAttribute(Libro.LIBRO_ID));
        libreria.addIndex(SuffixTreeIndex.onAttribute(Libro.LIBRO_TITULO));
        libreria.addIndex(SuffixTreeIndex.onAttribute(Libro.LIBRO_AUTOR));
        libreria.addIndex(SuffixTreeIndex.onAttribute(Libro.LIBRO_IDIOMA));
        libreria.addIndex(HashIndex.onAttribute(Libro.LIBRO_PUBLICADO));
        libreria.addIndex(SuffixTreeIndex.onAttribute(Libro.LIBRO_COLECCION));
        libreria.addIndex(SuffixTreeIndex.onAttribute(Libro.LIBRO_GENERO));
        libreria.addIndex(HashIndex.onAttribute(Libro.LIBRO_EN_CALIBRE));
        libreria.addIndex(HashIndex.onAttribute(Libro.LIBRO_AUTOR_FAVORITO));
        libreria.addIndex(HashIndex.onAttribute(Libro.LIBRO_IDIOMA_FAVORITO));
        libreria.addIndex(HashIndex.onAttribute(Libro.LIBRO_GENERO_FAVORITO));
        //
        autores.addIndex(UniqueIndex.onAttribute(Autor.AUTOR_ID));
        autores.addIndex(SuffixTreeIndex.onAttribute(Autor.AUTOR_NOMBRE));
        //
        generos.addIndex(UniqueIndex.onAttribute(Genero.GENERO_ID));
        generos.addIndex(SuffixTreeIndex.onAttribute(Genero.GENERO_NOMBRE));
        //
        idiomas.addIndex(UniqueIndex.onAttribute(Idioma.IDIOMA_ID));
        idiomas.addIndex(SuffixTreeIndex.onAttribute(Idioma.IDIOMA_NOMBRE));
        //
        log.info("Inicializando versi\u00f3n: {}", buildVersion);
    }

    public void update(UpdateSpec updateSpec) {
        writeLock.lock();
        try {
            // Limpiamos y añadimos los elementos individuales para que no haya referencias
            // sueltas
            libreria.clear();
            final List<Libro> libros = updateSpec.getLibroCSVs()
                    .stream()
                    .map(mapperService::from)
                    .collect(Collectors.toList());
            libreria.addAll(libros);
            log.info("Libros a\u00f1adidos.");
            // Actualizamos segun los datos que tenemos
            calibreService.updateLibros(SEARCH_AND_UDPATE_BOOK);
            preferencesService.updateAutoresFavoritos(SEARCH_AND_UDPATE_AUTOR_FAVORITO);
            preferencesService.updateIdiomasFavoritos(SEARCH_AND_UDPATE_IDIOMA_FAVORITO);
            preferencesService.updateGenerosFavoritos(SEARCH_AND_UDPATE_GENERO_FAVORITO);
            //
            autores.clear();
            autores.addAll(libreria.stream()
                    .map(Libro::getAutor)
                    .flatMap(autor -> Stream.of(autor.split(" & ")))
                    .map(String::trim)
                    .distinct()
                    .map(nombre -> {
                        try (final ResultSet<Libro> queryResult = libreria
                                .retrieve(contains(Libro.LIBRO_AUTOR, Libro.flattenToAscii(nombre)))) {
                            return new Autor(nombre, queryResult.size());
                        }
                    })
                    .collect(Collectors.toList()));
            //
            generos.clear();
            generos.addAll(libreria.stream()
                    .flatMap(libro -> libro.getListaGeneros().stream())
                    .map(String::trim)
                    .distinct()
                    .map(nombre -> {
                        try (final ResultSet<Libro> queryResult = libreria
                                .retrieve(contains(Libro.LIBRO_GENERO, Libro.flattenToAscii(nombre)))) {
                            return new Genero(nombre, queryResult.size());
                        }
                    })
                    .collect(Collectors.toList()));
            //
            idiomas.clear();
            idiomas.addAll(libreria.stream().map(Libro::getIdioma).map(String::trim).distinct().map(nombre -> {
                try (final ResultSet<Libro> queryResult = libreria
                        .retrieve(contains(Libro.LIBRO_IDIOMA, Libro.flattenToAscii(nombre)))) {
                    return new Idioma(nombre, queryResult.size());
                }
            }).collect(Collectors.toList()));
            //
            this.fechaActualizacion = updateSpec.getFechaActualizacion();
            log.info("Libros almacenados en la biblioteca: {}", libreria.size());
            //
            // Inicializamos el sumario
            sumario = null;
        } finally {
            writeLock.unlock();
        }
    }

    public Sumario getSumario() {
        if (sumario == null) {
            int numLibros = libreria.size();
            int numIdiomas = (int) libreria.stream().map(Libro::getIdioma).distinct().count();
            int numAutores = (int) libreria.stream().map(Libro::getAutor).distinct().count();
            int numGeneros = (int) libreria.stream()
                    .map(Libro::getListaGeneros)
                    .flatMap(List::stream)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            sumario = new Sumario(buildVersion,
                    fechaActualizacion.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), numLibros, numIdiomas,
                    numAutores, numGeneros, calibreService.isEnabled());
        }
        return sumario;
    }

    public Pagina<Libro> paginaLibros(BusquedaLibro busquedaLibro) {
        Pagina<Libro> pagina = new Pagina<>();
        readLock.lock();
        try (final ResultSet<Libro> queryResult = libreria.retrieve(busquedaLibro.getQuery(),
                busquedaLibro.getQueryOptions())) {
            pagina.setTotal(queryResult.size());
            pagina.setResults(queryResult.stream()
                    .skip((busquedaLibro.getNumeroPagina() - 1) * (long) busquedaLibro.getPorPagina())
                    .limit(busquedaLibro.getPorPagina())
                    .collect(Collectors.toList()));
        } finally {
            readLock.unlock();
        }
        return pagina;
    }

    public Pagina<Autor> paginaAutor(BusquedaElemento<Autor> busquedaAutor) {
        return paginaElementos(autores, busquedaAutor, preferencesService::checkAutorFavorito);
    }

    public Pagina<Genero> paginaGenero(BusquedaElemento<Genero> busquedaGenero) {
        return paginaElementos(generos, busquedaGenero, preferencesService::checkGeneroFavorito);
    }

    public Pagina<Idioma> paginaIdioma(BusquedaElemento<Idioma> busquedaIdioma) {
        return paginaElementos(idiomas, busquedaIdioma, preferencesService::checkIdiomaFavorito);
    }

    private <O extends PuedeSerFavorito> Pagina<O> paginaElementos(IndexedCollection<O> elementos,
            BusquedaElemento<O> busqueda, Function<String, Boolean> esFavorito) {
        Pagina<O> pagina = new Pagina<>();
        readLock.lock();
        try (final ResultSet<O> queryResult = elementos.retrieve(busqueda.getQuery(), busqueda.getQueryOptions())) {
            if (busqueda.isSoloFavoritos()) {
                Predicate<O> filterPredicate = object -> esFavorito.apply(object.getNombre());
                pagina.setTotal((int) queryResult.stream().filter(filterPredicate).count());
                pagina.setResults(queryResult.stream()
                        .filter(filterPredicate)
                        .skip((busqueda.getNumeroPagina() - 1) * (long) busqueda.getPorPagina())
                        .limit(busqueda.getPorPagina())
                        .peek(object -> object.setFavorito(true))
                        .collect(Collectors.toList()));
            } else {
                pagina.setTotal(queryResult.size());
                pagina.setResults(queryResult.stream()
                        .skip((busqueda.getNumeroPagina() - 1) * (long) busqueda.getPorPagina())
                        .limit(busqueda.getPorPagina())
                        .peek(object -> object.setFavorito(esFavorito.apply(object.getNombre())))
                        .collect(Collectors.toList()));
            }
        } finally {
            readLock.unlock();
        }
        return pagina;
    }

    public void actualizaAutoresFavoritos(Set<String> autores, Boolean favorito) {
        autores.forEach(nombre -> SEARCH_AND_UDPATE_AUTOR_FAVORITO.accept(nombre, favorito));
    }

    private BiConsumer<String, Boolean> SEARCH_AND_UDPATE_AUTOR_FAVORITO = (autor, valor) -> {
        List<Libro> aModificar = new ArrayList<>();
        try (final ResultSet<Libro> queryResult = libreria
                .retrieve(contains(Libro.LIBRO_AUTOR, Libro.flattenToAscii(autor)))) {
            queryResult.stream().forEach(libro -> {
                if (!valor.equals(libro.getAutorFavorito())) {
                    libro.setAutorFavorito(valor);
                    aModificar.add(libro);
                }
            });
        }
        if (!aModificar.isEmpty()) {
            libreria.update(aModificar, aModificar);
        }
    };

    public void actualizaIdiomasFavoritos(Set<String> idiomas, Boolean favorito) {
        idiomas.forEach(nombre -> SEARCH_AND_UDPATE_IDIOMA_FAVORITO.accept(nombre, favorito));
    }

    private BiConsumer<String, Boolean> SEARCH_AND_UDPATE_IDIOMA_FAVORITO = (idioma, valor) -> {
        List<Libro> aModificar = new ArrayList<>();
        try (final ResultSet<Libro> queryResult = libreria
                .retrieve(contains(Libro.LIBRO_IDIOMA, Libro.flattenToAscii(idioma)))) {
            queryResult.stream().forEach(libro -> {
                if (!valor.equals(libro.getIdiomaFavorito())) {
                    libro.setIdiomaFavorito(valor);
                    aModificar.add(libro);
                }
            });
        }
        if (!aModificar.isEmpty()) {
            libreria.update(aModificar, aModificar);
        }
    };

    public void actualizaGenerosFavoritos(Set<String> generos, Boolean favorito) {
        generos.forEach(nombre -> SEARCH_AND_UDPATE_GENERO_FAVORITO.accept(nombre, favorito));
    }

    private BiConsumer<String, Boolean> SEARCH_AND_UDPATE_GENERO_FAVORITO = (genero, valor) -> {
        List<Libro> aModificar = new ArrayList<>();
        try (final ResultSet<Libro> queryResult = libreria
                .retrieve(contains(Libro.LIBRO_GENERO, Libro.flattenToAscii(genero)))) {
            queryResult.stream().forEach(libro -> {
                if (!valor.equals(libro.getGeneroFavorito())) {
                    libro.setGeneroFavorito(valor);
                    aModificar.add(libro);
                }
            });
        }
        if (!aModificar.isEmpty()) {
            libreria.update(aModificar, aModificar);
        }
    };

    private BiConsumer<String, String> SEARCH_AND_UDPATE_BOOK = (titulo, autores) -> {
        List<Libro> aModificar = new ArrayList<>();
        Set<String> autoresSet = Arrays.asList(autores.split(" & "))
                .stream()
                .map(Libro::flattenToAscii)
                .collect(Collectors.toSet());
        try (final com.googlecode.cqengine.resultset.ResultSet<Libro> queryResult = libreria
                .retrieve(BusquedaLibro.getPartialQuery(Libro.flattenToAscii(titulo), Libro.LIBRO_TITULO))) {
            queryResult.stream().forEach(libro -> {
                Set<String> autoresSetLibro = libro.getListaAutores()
                        .stream()
                        .map(Libro::flattenToAscii)
                        .collect(Collectors.toSet());
                if (autoresSetLibro.containsAll(autoresSet) && autoresSet.containsAll(autoresSetLibro)) {
                    libro.setInCalibre(true);
                    aModificar.add(libro);
                }
            });
        }
        libreria.update(aModificar, aModificar);
    };
}
