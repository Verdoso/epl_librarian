package org.greeneyed.epl.librarian.services.model;

import static com.googlecode.cqengine.query.QueryFactory.all;
import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.contains;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.services.BibliotecaService.BOOK_ORDERING;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
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

	public QueryOptions getQueryOptions() {
		return reversed ? ordering.getQueryOptionsDescending() : ordering.getQueryOptionsAscending();
	}

	public Query<Libro> getQuery() {
		Query<Libro> query;
		List<Query<Libro>> partialQueries = Arrays
				.asList(getPartialQuery(getFiltroTitulo(), Libro.LIBRO_TITULO),
						getPartialQuery(getFiltroAutor(), Libro.LIBRO_AUTOR),
						getPartialQuery(getFiltroIdioma(), Libro.LIBRO_IDIOMA),
						getPartialQuery(getFiltroColeccion(), Libro.LIBRO_COLECCION),
						getPartialQuery(getFiltroGenero(), Libro.LIBRO_GENERO),
						getFechaQuery(getFiltroFecha(), Libro.LIBRO_PUBLICADO))
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
		return query;
	}

	private Query<Libro> getFechaQuery(final LocalDate criterio, final Attribute<Libro, ChronoLocalDate> campo) {
		if (criterio != null) {
			return QueryFactory.greaterThanOrEqualTo(campo, criterio);
		} else
			return null;
	}

	private Query<Libro> getPartialQuery(final String criterio, final Attribute<Libro, String> campo) {
		if (StringUtils.isNotBlank(criterio)) {
			return contains(campo, Libro.flattenToAscii(criterio));
		} else
			return null;
	}
}