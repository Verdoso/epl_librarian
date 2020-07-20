package org.greeneyed.epl.librarian.services;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.contains;
import static com.googlecode.cqengine.query.QueryFactory.descending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.greeneyed.epl.librarian.model.Autor;
import org.greeneyed.epl.librarian.model.Genero;
import org.greeneyed.epl.librarian.model.Idioma;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.model.Pagina;
import org.greeneyed.epl.librarian.model.Sumario;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.LibroCSV;
import org.greeneyed.epl.librarian.services.model.BusquedaElemento;
import org.greeneyed.epl.librarian.services.model.BusquedaLibro;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final MapperService mapperService;

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
		//
		autores.addIndex(UniqueIndex.onAttribute(Autor.AUTOR_ID));
		autores.addIndex(SuffixTreeIndex.onAttribute(Autor.AUTOR_NOMBRE));
		//
		generos.addIndex(UniqueIndex.onAttribute(Genero.GENERO_ID));
		generos.addIndex(SuffixTreeIndex.onAttribute(Genero.GENERO_NOMBRE));
		//
		idiomas.addIndex(UniqueIndex.onAttribute(Idioma.IDIOMA_ID));
		idiomas.addIndex(SuffixTreeIndex.onAttribute(Idioma.IDIOMA_NOMBRE));
	}

	public void update(List<LibroCSV> newLibros) {
		writeLock.lock();
		try {
			// Limpiamos y añadimos los elementos individuales para que no haya referencias
			// sueltas
			libreria.clear();
			libreria.addAll(newLibros.stream().map(mapperService::from).collect(Collectors.toList()));
			//
			autores.clear();
			autores.addAll(libreria.stream()
					.map(Libro::getAutor)
					.flatMap(autor -> Stream.of(autor.split(" & ")))
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
			generos.addAll(
					libreria.stream().flatMap(libro -> libro.getListaGeneros().stream()).distinct().map(nombre -> {
						try (final ResultSet<Libro> queryResult = libreria
								.retrieve(contains(Libro.LIBRO_GENERO, Libro.flattenToAscii(nombre)))) {
							return new Genero(nombre, queryResult.size());
						}
					}).collect(Collectors.toList()));
			//
			idiomas.clear();
			idiomas.addAll(libreria.stream().map(Libro::getIdioma).distinct().map(nombre -> {
				try (final ResultSet<Libro> queryResult = libreria
						.retrieve(contains(Libro.LIBRO_IDIOMA, Libro.flattenToAscii(nombre)))) {
					return new Idioma(nombre, queryResult.size());
				}
			}).collect(Collectors.toList()));
			//
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
			sumario = new Sumario(numLibros, numIdiomas, numAutores, numGeneros);
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
					.skip((busquedaLibro.getNumeroPagina() - 1) * (long)busquedaLibro.getPorPagina())
					.limit(busquedaLibro.getPorPagina())
					.collect(Collectors.toList()));
		} finally {
			readLock.unlock();
		}
		return pagina;
	}

	public Pagina<Autor> paginaAutor(BusquedaElemento<Autor> busquedaAutor) {
		return paginaElementos(autores, busquedaAutor);
	}

	public Pagina<Genero> paginaGenero(BusquedaElemento<Genero> busquedaGenero) {
		return paginaElementos(generos, busquedaGenero);
	}

	public Pagina<Idioma> paginaIdioma(BusquedaElemento<Idioma> busquedaIdioma) {
		return paginaElementos(idiomas, busquedaIdioma);
	}

	private <T extends ElementOrdering<O>, O> Pagina<O> paginaElementos(IndexedCollection<O> elementos,
			BusquedaElemento<O> busqueda) {
		Pagina<O> pagina = new Pagina<>();
		readLock.lock();
		try (final ResultSet<O> queryResult = elementos.retrieve(busqueda.getQuery(), busqueda.getQueryOptions())) {
			pagina.setTotal(queryResult.size());
			pagina.setResults(queryResult.stream()
					.skip((busqueda.getNumeroPagina() - 1) * (long)busqueda.getPorPagina())
					.limit(busqueda.getPorPagina())
					.collect(Collectors.toList()));
		} finally {
			readLock.unlock();
		}
		return pagina;
	}

}
