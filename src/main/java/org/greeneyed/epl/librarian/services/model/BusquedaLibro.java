package org.greeneyed.epl.librarian.services.model;

import static com.googlecode.cqengine.query.QueryFactory.all;
import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.existsIn;
import static com.googlecode.cqengine.query.QueryFactory.contains;
import static com.googlecode.cqengine.query.QueryFactory.greaterThanOrEqualTo;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.greeneyed.epl.librarian.model.Autor;
import org.greeneyed.epl.librarian.model.Genero;
import org.greeneyed.epl.librarian.model.Idioma;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.services.BibliotecaService;
import org.greeneyed.epl.librarian.services.BibliotecaService.BOOK_ORDERING;
import org.greeneyed.epl.librarian.services.PreferencesService;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class BusquedaLibro {
  private final BibliotecaService bibliotecaService;
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
  private final Boolean ocultarDescartados;

  public QueryOptions getQueryOptions() {
    return reversed ? ordering.getQueryOptionsDescending() : ordering.getQueryOptionsAscending();
  }

  public Query<Libro> getQuery(PreferencesService preferencesService) {
    Query<Libro> query;
    List<Query<Libro>> partialQueries = Arrays.asList(getPartialQuery(getFiltroTitulo(), Libro.LIBRO_TITULO),
        getPartialQuery(getFiltroAutor(), Libro.LIBRO_AUTOR), getPartialQuery(getFiltroIdioma(), Libro.LIBRO_IDIOMA),
        getPartialQuery(getFiltroColeccion(), Libro.LIBRO_COLECCION), getPartialQuery(getFiltroGenero(), Libro.LIBRO_GENERO),
        //
        getNoEnCalibreQuery(),
        //
        getNoDescartadosQuery(),
        //
        getAutoresFavoritosQuery(preferencesService),
        //
        getIdiomasFavoritosQuery(preferencesService),
        //
        getGenerosFavoritosQuery(preferencesService),
        //
        getFechaQuery(getFiltroFecha(), Libro.LIBRO_PUBLICADO)
    //
    )
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

  private static Query<Libro> getFechaQuery(final LocalDate criterio,
      final Attribute<Libro, ChronoLocalDate> campo) {
    if (criterio != null) {
      return greaterThanOrEqualTo(campo, criterio);
    } else
      return null;
  }

  public Query<Libro> getNoEnCalibreQuery() {
    if (Boolean.TRUE.equals(soloNoEnPropiedad)) {
      return equal(Libro.LIBRO_EN_CALIBRE, Boolean.FALSE);
    } else
      return null;
  }

  public Query<Libro> getNoDescartadosQuery() {
    if (Boolean.TRUE.equals(ocultarDescartados)) {
      return equal(Libro.LIBRO_DESCARTADO, Boolean.FALSE);
    } else
      return null;
  }

  public Query<Libro> getAutoresFavoritosQuery(PreferencesService preferencesService) {
    if (preferencesService.canAplyAutoresFavoritos() && Boolean.TRUE.equals(soloAutoresFavoritos)) {
      return existsIn(bibliotecaService.getAutores(), Libro.LIBRO_AUTORES, Autor.AUTOR_NOMBRE, equal(Autor.AUTOR_FAVORITO, Boolean.TRUE));
    } else
      return null;
  }

  public Query<Libro> getIdiomasFavoritosQuery(PreferencesService preferencesService) {
    if (preferencesService.canAplyIdiomasFavoritos() && Boolean.TRUE.equals(soloIdiomasFavoritos)) {
      return existsIn(bibliotecaService.getIdiomas(), Libro.LIBRO_IDIOMA, Idioma.IDIOMA_NOMBRE, equal(Idioma.IDIOMA_FAVORITO, Boolean.TRUE));
    } else
      return null;
  }

  public Query<Libro> getGenerosFavoritosQuery(PreferencesService preferencesService) {
    if (preferencesService.canAplyGenerosFavoritos() && Boolean.TRUE.equals(soloGenerosFavoritos)) {
      return existsIn(bibliotecaService.getGeneros(), Libro.LIBRO_GENEROS, Genero.GENERO_NOMBRE, equal(Genero.GENERO_FAVORITO, Boolean.TRUE));
    } else
      return null;
  }

  public static Query<Libro> getPartialQuery(final String criterio,
      final Attribute<Libro, String> campo) {
    if (StringUtils.isNotBlank(criterio)) {
      return contains(campo, Libro.flattenToAscii(criterio));
    } else
      return null;
  }
}