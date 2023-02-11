package org.greeneyed.epl.librarian.services;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.contains;
import static com.googlecode.cqengine.query.QueryFactory.descending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.in;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

import jakarta.annotation.PostConstruct;

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
	private static final Predicate<? super Autor> AUTOR_INDETERMINADO = autor -> "AA. VV.".equals(autor.getNombre());

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

	private LocalDateTime fechaActualizacion = LocalDateTime.now();

	private final MapperService mapperService;
	private final PreferencesService preferencesService;
	private final CalibreService calibreService;

	private final IndexedCollection<Libro> libreria = new ConcurrentIndexedCollection<>();
	@Getter
	private final IndexedCollection<Autor> autores = new ConcurrentIndexedCollection<>();
	@Getter
	private final IndexedCollection<Genero> generos = new ConcurrentIndexedCollection<>();
	@Getter
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
		libreria.addIndex(HashIndex.onAttribute(Libro.LIBRO_AUTORES));
		libreria.addIndex(HashIndex.onAttribute(Libro.LIBRO_GENEROS));
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
			updateCalibre(false);
			log.info("Recopilando autores...");
			autores.clear();
			autores.addAll(libreria.stream()
					.map(Libro::getAutor)
					.flatMap(autor -> Stream.of(autor.split(" & ")))
					.map(String::trim)
					.distinct()
					.map(nombre -> {
						try (final ResultSet<Libro> queryResult = libreria
								.retrieve(in(Libro.LIBRO_AUTORES, Libro.flattenToAscii(nombre)))) {
							return new Autor(nombre, queryResult.size());
						}
					})
					.toList());
			//
			log.info("Autores recopilados.");
			log.info("Recopilando generos...");
			generos.clear();
			generos.addAll(libreria.stream()
					.flatMap(libro -> libro.getListaGeneros().stream())
					.map(String::trim)
					.distinct()
					.map(nombre -> {
						try (final ResultSet<Libro> queryResult = libreria
								.retrieve(in(Libro.LIBRO_GENEROS, Libro.flattenToAscii(nombre)))) {
							return new Genero(nombre, queryResult.size());
						}
					})
					.collect(Collectors.toList()));
			//
			log.info("Géneros recopilados.");
			log.info("Recopilando idiomas...");
			idiomas.clear();
			idiomas.addAll(libreria.stream().map(Libro::getIdioma).map(String::trim).distinct().map(nombre -> {
				try (final ResultSet<Libro> queryResult = libreria
						.retrieve(contains(Libro.LIBRO_IDIOMA, Libro.flattenToAscii(nombre)))) {
					return new Idioma(nombre, queryResult.size());
				}
			}).collect(Collectors.toList()));
			//
			log.info("Idiomas recopilados.");
			//
			actualizaAutoresPreferidos(preferencesService.getAutoresPreferidos());
			actualizaLibrosDescartados(preferencesService.getLibrosDescartados());
			actualizaIdiomasFavoritos(preferencesService.getIdiomasPreferidos());
			actualizaGenerosFavoritos(preferencesService.getGenerosPreferidos());
			//
			this.fechaActualizacion = updateSpec.getFechaActualizacion();
			if (this.fechaActualizacion == null) {
				this.fechaActualizacion = LocalDateTime.now();
			}
			log.info("Libros almacenados en la biblioteca: {}", libreria.size());
			//
			// Inicializamos el sumario
			sumario = null;
		} catch (Exception e) {
			log.error("Error reconstruyendo biblioteca", e);
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
		try (final ResultSet<Libro> queryResult = libreria.retrieve(busquedaLibro.getQuery(preferencesService),
				busquedaLibro.getQueryOptions())) {
			pagina.setTotal(queryResult.size());
			pagina.setResults(queryResult.stream()
					.skip((busquedaLibro.getNumeroPagina() - 1) * (long) busquedaLibro.getPorPagina())
					.limit(busquedaLibro.getPorPagina())
					.toList());
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

	public void setDescarte(Integer libroId, boolean descartado) {
		log.info("Actualizando libros descarte...");
		writeLock.lock();
		try {
			try (final ResultSet<Libro> queryResult = libreria.retrieve(in(Libro.LIBRO_ID, libroId))) {
				queryResult.stream().forEach(libro -> {
					libreria.remove(libro);
					libro.setDescartado(descartado);
					libreria.add(libro);
				});
			}
		} finally {
			writeLock.unlock();
		}
		log.info("...actualizado");
	}

	public void actualizaLibrosDescartados(Set<Integer> libros) {
		log.info("Actualizando libros descartados...");
		writeLock.lock();
		try {
			List<Libro> all = libreria.stream()
					.peek(libro -> libro.setDescartado(libros.contains(libro.getId())))
					.collect(Collectors.toList());
			libreria.clear();
			libreria.addAll(all);
		} finally {
			writeLock.unlock();
		}
		log.info("...actualizados");
	}

	public void actualizaAutoresPreferidos(Set<String> autoresFavoritos) {
		log.info("Actualizando autores preferidos...");
		writeLock.lock();
		try {
			List<Autor> autoresActualizados = autores.stream().map(autor -> {
				autor.setFavorito(autoresFavoritos.contains(autor.getNombre()));
				return autor;
			}).toList();
			autores.clear();
			autores.addAll(autoresActualizados);
		} finally {
			writeLock.unlock();
		}
		log.info("...actualizados");
	}

	public void actualizaIdiomasFavoritos(Set<String> idiomasFavoritos) {
		log.info("Actualizando idiomas preferidos...");
		writeLock.lock();
		try {
			List<Idioma> idiomasActualizados = idiomas.stream().map(idioma -> {
				idioma.setFavorito(idiomasFavoritos.contains(idioma.getNombre()));
				return idioma;
			}).toList();
			idiomas.clear();
			idiomas.addAll(idiomasActualizados);
		} finally {
			writeLock.unlock();
		}
		log.info("...actualizados");
	}

	public void actualizaGenerosFavoritos(Set<String> generosFavoritos) {
		log.info("Actualizando generos preferidos...");
		writeLock.lock();
		try {
			List<Genero> generosActualizados = generos.stream().map(genero -> {
				genero.setFavorito(generosFavoritos.contains(genero.getNombre()));
				return genero;
			}).toList();
			generos.clear();
			generos.addAll(generosActualizados);
		} finally {
			writeLock.unlock();
		}
		log.info("...actualizados");
	}

	private BiConsumer<String, String> SEARCH_AND_UDPATE_BOOK = (titulo, autores) -> {
		List<Libro> aModificar = new ArrayList<>();
		Set<String> autoresSet = Arrays.asList(autores.split(" & "))
				.stream()
				.map(Libro::flattenToAscii)
				.collect(Collectors.toSet());
		try (ResultSet<Libro> queryResult = libreria
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

	public Pagina<Autor> getTopAutores(int porPagina) {
		List<Autor> topAutores = autores.stream().filter(AUTOR_INDETERMINADO.negate()).map(autor -> {
			try (ResultSet<Libro> queryResult = libreria
					.retrieve(and(in(Libro.LIBRO_AUTORES, Libro.flattenToAscii(autor.getNombre())),
							equal(Libro.LIBRO_EN_CALIBRE, Boolean.TRUE)))) {
				return new Autor(autor.getNombre(), queryResult.size());
			}
		})
				.sorted(Comparator.comparing(Autor::getLibros).thenComparing(Autor::getNombre).reversed())
				.filter(Autor::tieneLibros)
				.limit(porPagina)
				.toList();
		return new Pagina<Autor>(topAutores, topAutores.size());
	}

	public Pagina<Idioma> getTopIdiomas(int porPagina) {
		List<Idioma> topIdiomas = idiomas.stream().map(idioma -> {
			try (final ResultSet<Libro> queryResult = libreria
					.retrieve(and(equal(Libro.LIBRO_IDIOMA, Libro.flattenToAscii(idioma.getNombre())),
							equal(Libro.LIBRO_EN_CALIBRE, Boolean.TRUE)))) {
				return new Idioma(idioma.getNombre(), queryResult.size());
			}
		})
				.sorted(Comparator.comparing(Idioma::getLibros).thenComparing(Idioma::getNombre).reversed())
				.filter(Idioma::tieneLibros)
				.limit(porPagina)
				.toList();
		return new Pagina<Idioma>(topIdiomas, topIdiomas.size());
	}

	public Pagina<Genero> getTopGeneros(int porPagina) {
		List<Genero> topGeneros = generos.stream().map(genero -> {
			try (final ResultSet<Libro> queryResult = libreria
					.retrieve(and(in(Libro.LIBRO_GENEROS, Libro.flattenToAscii(genero.getNombre())),
							equal(Libro.LIBRO_EN_CALIBRE, Boolean.TRUE)))) {
				return new Genero(genero.getNombre(), queryResult.size());
			}
		})
				.sorted(Comparator.comparing(Genero::getLibros).thenComparing(Genero::getNombre).reversed())
				.filter(Genero::tieneLibros)
				.limit(porPagina)
				.toList();
		return new Pagina<Genero>(topGeneros, topGeneros.size());
	}

	public void updateCalibre(boolean withReset) {
		writeLock.lock();
		try {
			if (withReset) {
				List<Libro> all = libreria.stream().map(libro -> {
					libro.setInCalibre(Boolean.FALSE);
					return libro;
				}).collect(Collectors.toList());
				libreria.clear();
				libreria.addAll(all);
			}
			// Actualizamos segun los datos que tenemos
			calibreService.updateLibros(SEARCH_AND_UDPATE_BOOK);
		} finally {
			writeLock.unlock();
		}
	}
}
