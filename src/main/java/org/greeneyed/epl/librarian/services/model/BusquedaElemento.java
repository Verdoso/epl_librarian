package org.greeneyed.epl.librarian.services.model;

import static com.googlecode.cqengine.query.QueryFactory.all;
import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.contains;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.services.BibliotecaService.ElementOrdering;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class BusquedaElemento<T extends ElementOrdering<O>, O> {
	private final int numeroPagina;
	private final int porPagina;
	private final ElementOrdering<O> ordering;
	private final boolean reversed;
	private final String filtro;

	public QueryOptions getQueryOptions() {
		return reversed ? ordering.getQueryOptionsDescending() : ordering.getQueryOptionsAscending();
	}

	public Query<O> getQuery() {
		Query<O> query;
		List<Query<O>> partialQueries = Arrays.asList(getPartialQuery(getFiltro(), ordering.getSortAttribute()))
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

	private Query<O> getPartialQuery(final String criterio, final Attribute<O, String> campo) {
		if (StringUtils.isNotBlank(criterio)) {
			return contains(campo, Libro.flattenToAscii(criterio));
		} else
			return null;
	}
}