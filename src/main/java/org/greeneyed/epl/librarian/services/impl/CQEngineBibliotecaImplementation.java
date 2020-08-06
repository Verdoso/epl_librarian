package org.greeneyed.epl.librarian.services.impl;

import static com.googlecode.cqengine.query.QueryFactory.all;
import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.contains;
import static com.googlecode.cqengine.query.QueryFactory.descending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.greaterThanOrEqualTo;
import static com.googlecode.cqengine.query.QueryFactory.not;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
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

import org.apache.commons.lang3.StringUtils;
import org.greeneyed.epl.librarian.model.Autor;
import org.greeneyed.epl.librarian.model.Genero;
import org.greeneyed.epl.librarian.model.Idioma;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.model.Pagina;
import org.greeneyed.epl.librarian.model.PuedeSerFavorito;
import org.greeneyed.epl.librarian.model.Sumario;
import org.greeneyed.epl.librarian.services.CalibreService;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.UpdateSpec;
import org.greeneyed.epl.librarian.services.MapperService;
import org.greeneyed.epl.librarian.services.PreferencesService;
import org.greeneyed.epl.librarian.services.model.BusquedaElemento;
import org.greeneyed.epl.librarian.services.model.BusquedaElemento.ElementOrdering;
import org.greeneyed.epl.librarian.services.model.BusquedaLibro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class CQEngineBibliotecaImplementation {

    @Getter
    @AllArgsConstructor
    private enum AUTOR_ORDERING implements ElementCQEngineOrdering<Autor> {
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
    private enum BOOK_ORDERING implements ElementCQEngineOrdering<Libro> {
        POR_AUTOR(queryOptions(orderBy(ascending(Libro.LIBRO_AUTOR))),
                queryOptions(orderBy(descending(Libro.LIBRO_AUTOR))))
        //
        , POR_CALIBRE(queryOptions(orderBy(ascending(Libro.LIBRO_EN_CALIBRE), ascending(Libro.LIBRO_TITULO))),
                queryOptions(orderBy(descending(Libro.LIBRO_EN_CALIBRE), descending(Libro.LIBRO_TITULO))))
        //
        ,
        POR_COLECCION(
                queryOptions(orderBy(ascending(Libro.LIBRO_COLECCION), ascending(Libro.LIBRO_VOLUMEN),
                        ascending(Libro.LIBRO_TITULO))),
                queryOptions(orderBy(descending(Libro.LIBRO_COLECCION), descending(Libro.LIBRO_VOLUMEN),
                        descending(Libro.LIBRO_TITULO))))
        //
        , POR_IDIOMA(queryOptions(orderBy(ascending(Libro.LIBRO_IDIOMA))),
                queryOptions(orderBy(descending(Libro.LIBRO_IDIOMA))))
        //
        //
        ,
        POR_PUBLICADO(queryOptions(orderBy(ascending(Libro.LIBRO_PUBLICADO), ascending(Libro.LIBRO_TITULO))),
                queryOptions(orderBy(descending(Libro.LIBRO_PUBLICADO), descending(Libro.LIBRO_TITULO)))),
        POR_TITULO(queryOptions(orderBy(ascending(Libro.LIBRO_TITULO))),
                queryOptions(orderBy(descending(Libro.LIBRO_TITULO))))
        //
        ;

        private final QueryOptions queryOptionsAscending;
        private final QueryOptions queryOptionsDescending;
        private final SimpleAttribute<Libro, String> sortAttribute = Libro.LIBRO_TITULO;
    }

    private static interface ElementCQEngineOrdering<T> {
        QueryOptions getQueryOptionsAscending();

        QueryOptions getQueryOptionsDescending();

        SimpleAttribute<T, String> getSortAttribute();
    }

    @Getter
    @AllArgsConstructor
    private enum GENERO_ORDERING implements ElementCQEngineOrdering<Genero> {
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
    private enum IDIOMA_ORDERING implements ElementCQEngineOrdering<Idioma> {
        POR_IDIOMA(queryOptions(orderBy(ascending(Idioma.IDIOMA_NOMBRE))),
                queryOptions(orderBy(descending(Idioma.IDIOMA_NOMBRE)))),
        POR_LIBROS(queryOptions(orderBy(ascending(Idioma.IDIOMA_LIBROS))),
                queryOptions(orderBy(descending(Idioma.IDIOMA_LIBROS))));

        private final QueryOptions queryOptionsAscending;
        private final QueryOptions queryOptionsDescending;
        private final SimpleAttribute<Idioma, String> sortAttribute = Idioma.IDIOMA_NOMBRE;

    }

    private final IndexedCollection<Autor> autores = new ConcurrentIndexedCollection<>();

    @Value("${git.build.version}")
    private String buildVersion;

    private final CalibreService calibreService;
    private LocalDateTime fechaActualizacion = null;
    private final IndexedCollection<Genero> generos = new ConcurrentIndexedCollection<>();

    private final IndexedCollection<Idioma> idiomas = new ConcurrentIndexedCollection<>();
    private final IndexedCollection<Libro> libreria = new ConcurrentIndexedCollection<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final MapperService mapperService;

    private final PreferencesService preferencesService;
    private final ReadLock readLock = lock.readLock();
    private BiConsumer<String, String> SEARCH_AND_UDPATE_BOOK = (titulo, autores) -> {
        List<Libro> aModificar = new ArrayList<>();
        Set<String> autoresSet = Arrays.asList(autores.split(" & "))
                .stream()
                .map(Libro::flattenToAscii)
                .collect(Collectors.toSet());
        try (final com.googlecode.cqengine.resultset.ResultSet<Libro> queryResult = libreria
                .retrieve(getPartialQuery(Libro.flattenToAscii(titulo), Libro.LIBRO_TITULO))) {
            queryResult.stream().forEach(libro -> {
                Set<String> autoresSetLibro = libro.getListaAutores()
                        .stream()
                        .map(Libro::flattenToAscii)
                        .collect(Collectors.toSet());
                if (autoresSetLibro.containsAll(autoresSet) && autoresSet.containsAll(autoresSetLibro)) {
                    aModificar.add(libro);
                }
            });
        }
        for (Libro libro : aModificar) {
            log.debug("JLibro encontrado en Calibre: {}", libro.getTitulo());
            libro.setInCalibre(true);
            libreria.remove(libro);
            libreria.add(libro);
        }
    };

    private Sumario sumario = null;

    private final WriteLock writeLock = lock.writeLock();

//    @Override
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

    private boolean isAutorFavorito(Libro libro) {
        return preferencesService.checkAutorFavorito(libro.getListaAutores());
    }

    private boolean isGeneroFavorito(Libro libro) {
        return preferencesService.checkGeneroFavorito(libro.getListaGeneros());
    }

    private boolean isIdiomaFavorito(Libro libro) {
        return preferencesService.checkIdiomaFavorito(libro.getIdioma());
    }

//    @Override
    public Pagina<Autor> paginaAutor(BusquedaElemento<Autor> busquedaAutor) {
        return paginaElementos(autores, busquedaAutor, preferencesService::checkAutorFavorito);
    }

    private <O extends PuedeSerFavorito> Pagina<O> paginaElementos(IndexedCollection<O> elementos,
            BusquedaElemento<O> busqueda, Function<String, Boolean> esFavorito) {
        Pagina<O> pagina = new Pagina<>();
        readLock.lock();
        try (final ResultSet<O> queryResult = elementos.retrieve(getQuery(busqueda), getQueryOptions(busqueda))) {
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

    // @Override
    public Pagina<Genero> paginaGenero(BusquedaElemento<Genero> busquedaGenero) {
        return paginaElementos(generos, busquedaGenero, preferencesService::checkGeneroFavorito);
    }

    // @Override
    public Pagina<Idioma> paginaIdioma(BusquedaElemento<Idioma> busquedaIdioma) {
        return paginaElementos(idiomas, busquedaIdioma, preferencesService::checkIdiomaFavorito);
    }

    // @Override
    public Pagina<Libro> paginaLibros(BusquedaLibro busquedaLibro) {
        Pagina<Libro> pagina = new Pagina<>();
        readLock.lock();
        final Query<Libro> query = getQuery(busquedaLibro);
        try (final ResultSet<Libro> queryResult = libreria.retrieve(query, getQueryOptions(busquedaLibro))) {
            Stream<Libro> resultadosBusqueda = queryResult.stream();
            pagina.setTotal(queryResult.size());
            if (busquedaLibro.isSoloAutoresFavoritos() || busquedaLibro.isSoloIdiomasFavoritos()
                    || busquedaLibro.isSoloGenerosFavoritos()) {
                Predicate<Libro> filterPredicate = libro -> true;
                if (preferencesService.canAplyAutoresFavoritos() && busquedaLibro.isSoloAutoresFavoritos()) {
                    filterPredicate = filterPredicate.and(this::isAutorFavorito);
                }
                if (preferencesService.canAplyIdiomasFavoritos() && busquedaLibro.isSoloIdiomasFavoritos()) {
                    filterPredicate = filterPredicate.and(this::isIdiomaFavorito);
                }
                if (preferencesService.canAplyGenerosFavoritos() && busquedaLibro.isSoloGenerosFavoritos()) {
                    filterPredicate = filterPredicate.and(this::isGeneroFavorito);
                }
                pagina.setTotal((int) queryResult.stream().filter(filterPredicate).count());
                resultadosBusqueda = queryResult.stream().filter(filterPredicate);
            }
            pagina.setResults(
                    resultadosBusqueda.skip((busquedaLibro.getNumeroPagina() - 1) * (long) busquedaLibro.getPorPagina())
                            .limit(busquedaLibro.getPorPagina())
                            .collect(Collectors.toList()));
        } finally {
            readLock.unlock();
        }
        return pagina;
    }

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

    // @Override
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
            calibreService.updateLibros(SEARCH_AND_UDPATE_BOOK);
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

    public QueryOptions getQueryOptions(BusquedaLibro busquedaLibro) {
        BOOK_ORDERING ordering = BOOK_ORDERING.valueOf(busquedaLibro.getOrdering().name());
        return busquedaLibro.isReversed() ? ordering.getQueryOptionsDescending() : ordering.getQueryOptionsAscending();
    }

    public Query<Libro> getQuery(BusquedaLibro busquedaLibro) {
        Query<Libro> query;
        List<Query<Libro>> partialQueries = Arrays
                .asList(getPartialQuery(busquedaLibro.getFiltroTitulo(), Libro.LIBRO_TITULO),
                        getPartialQuery(busquedaLibro.getFiltroAutor(), Libro.LIBRO_AUTOR),
                        getPartialQuery(busquedaLibro.getFiltroIdioma(), Libro.LIBRO_IDIOMA),
                        getPartialQuery(busquedaLibro.getFiltroColeccion(), Libro.LIBRO_COLECCION),
                        getPartialQuery(busquedaLibro.getFiltroGenero(), Libro.LIBRO_GENERO),
                        getEnCalibreQuery(busquedaLibro),
                        getFechaQuery(busquedaLibro.getFiltroFecha(), Libro.LIBRO_PUBLICADO))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (partialQueries.isEmpty()) {
            query = all(Libro.class);
        } else {
            query = partialQueries.get(0);
            for (int i = 1; i < partialQueries.size(); i++) {
                query = and(query, partialQueries.get(i));
            }
        }
        log.debug("Query: {}", query);
        return query;
    }

    public Query<Libro> getEnCalibreQuery(BusquedaLibro busquedaLibro) {
        if (Boolean.TRUE.equals(busquedaLibro.getSoloNoEnPropiedad())) {
            return not(equal(Libro.LIBRO_EN_CALIBRE, Boolean.TRUE));
        } else
            return null;
    }

    private static Query<Libro> getFechaQuery(final LocalDate criterio, final Attribute<Libro, ChronoLocalDate> campo) {
        if (criterio != null) {
            return greaterThanOrEqualTo(campo, criterio);
        } else
            return null;
    }

    private <O> Query<O> getPartialQuery(final String criterio, final Attribute<O, String> campo) {
        if (StringUtils.isNotBlank(criterio)) {
            return contains(campo, Libro.flattenToAscii(criterio));
        } else
            return null;
    }

    public <O> QueryOptions getQueryOptions(BusquedaElemento<O> busquedaElemento) {
        final ElementCQEngineOrdering<O> ordering = getCQEngineOrdering(busquedaElemento.getOrdering());
        return busquedaElemento.isReversed() ? ordering.getQueryOptionsDescending()
                : ordering.getQueryOptionsAscending();
    }

    public <O> Query<O> getQuery(BusquedaElemento<O> busquedaElemento) {
        final ElementCQEngineOrdering<O> ordering = getCQEngineOrdering(busquedaElemento.getOrdering());
        Query<O> query;
        List<Query<O>> partialQueries = Arrays
                .asList(getPartialQuery(busquedaElemento.getFiltro(), ordering.getSortAttribute()))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (partialQueries.isEmpty()) {
            query = all(ordering.getSortAttribute().getObjectType());
        } else {
            query = partialQueries.get(0);
            for (int i = 1; i < partialQueries.size(); i++) {
                query = and(query, partialQueries.get(i));
            }
        }
        log.trace("Query: {}", query);
        return query;
    }

    @SuppressWarnings("unchecked")
    private <O> ElementCQEngineOrdering<O> getCQEngineOrdering(ElementOrdering<O> ordering) {
        if (ordering.getOrderingClass() == Autor.class) {
            return (ElementCQEngineOrdering<O>) AUTOR_ORDERING.valueOf(ordering.name());
        } else if (ordering.getOrderingClass() == Idioma.class) {
            return (ElementCQEngineOrdering<O>) IDIOMA_ORDERING.valueOf(ordering.name());
        } else if (ordering.getOrderingClass() == Genero.class) {
            return (ElementCQEngineOrdering<O>) GENERO_ORDERING.valueOf(ordering.name());
        }
        throw new IllegalArgumentException("Ordenacion desconocida para los parametros "
                + ordering.getOrderingClass().getName() + ": " + ordering.name());
    }

}
