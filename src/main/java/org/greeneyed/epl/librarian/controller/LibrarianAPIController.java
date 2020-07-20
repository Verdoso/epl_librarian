package org.greeneyed.epl.librarian.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.greeneyed.epl.librarian.model.Autor;
import org.greeneyed.epl.librarian.model.Genero;
import org.greeneyed.epl.librarian.model.Idioma;
import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.model.Pagina;
import org.greeneyed.epl.librarian.model.Sumario;
import org.greeneyed.epl.librarian.services.BibliotecaService;
import org.greeneyed.epl.librarian.services.BibliotecaService.AUTOR_ORDERING;
import org.greeneyed.epl.librarian.services.BibliotecaService.BOOK_ORDERING;
import org.greeneyed.epl.librarian.services.BibliotecaService.GENERO_ORDERING;
import org.greeneyed.epl.librarian.services.BibliotecaService.IDIOMA_ORDERING;
import org.greeneyed.epl.librarian.services.model.BusquedaElemento;
import org.greeneyed.epl.librarian.services.model.BusquedaLibro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Data
@RequestMapping(value = "/librarian")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class LibrarianAPIController {

	private static final String ERROR_DETALLADO = "Error detallado";
	private final BibliotecaService bibliotecaService;
	private static final String DEFAULT_ORDER = "POR_TITULO";// BOOK_ORDERING.POR_TITULO.name();
	private static final String DEFAULT_ORDER_AUTOR = "POR_AUTOR";// BOOK_ORDERING.POR_AUTOR.name();
	private static final String DEFAULT_ORDER_GENERO = "POR_GENERO";// BOOK_ORDERING.POR_GENERO.name();
	private static final String DEFAULT_ORDER_IDIOMA = "POR_IDIOMA";// BOOK_ORDERING.POR_IDIOMA.name();

	@GetMapping(value = "/sumario")
	public ResponseEntity<Sumario> sumario() {
		return ResponseEntity.ok(bibliotecaService.getSumario());
	}

	@GetMapping(value = "/libros")
	public ResponseEntity<Pagina<Libro>> paginaLibro(
			@RequestParam(name = "numero_pagina", defaultValue = "0") int numeroPagina,
			@RequestParam(name = "por_pagina", defaultValue = "10") int porPagina,
			@RequestParam(name = "desc", defaultValue = "false") boolean reversed,
			@RequestParam(name = "orden", defaultValue = DEFAULT_ORDER) BOOK_ORDERING ordering,
			@RequestParam(name = "filtro_titulo", required = false) String filtroTitulo,
			@RequestParam(name = "filtro_coleccion", required = false) String filtroColeccion,
			@RequestParam(name = "filtro_autor", required = false) String filtroAutor,
			@RequestParam(name = "filtro_idioma", required = false) String filtroIdioma,
			@RequestParam(name = "filtro_genero", required = false) String filtroGenero,
			@RequestParam(name = "filtro_fecha", required = false) Long filtroFechaLong) {
		LocalDate filtroFecha = null;
		if (filtroFechaLong != null) {
			try {
				filtroFecha = Instant.ofEpochMilli(filtroFechaLong).atZone(ZoneId.systemDefault()).toLocalDate();
			} catch (Exception e) {
				log.error("Filtro fecha con formato incorrecto: {}", e.getMessage());
				log.trace(ERROR_DETALLADO, e);
			}
		}
		BusquedaLibro busquedaLibro = new BusquedaLibro(numeroPagina, porPagina, ordering, reversed, filtroTitulo,
				filtroColeccion, filtroAutor, filtroGenero, filtroIdioma, filtroFecha);
		log.trace("BusquedaLibro: {}", busquedaLibro);
		return ResponseEntity.ok(bibliotecaService.paginaLibros(busquedaLibro));
	}

	@GetMapping(value = "/autores")
	public ResponseEntity<Pagina<Autor>> paginaAutores(
			@RequestParam(name = "numero_pagina", defaultValue = "0") int numeroPagina,
			@RequestParam(name = "por_pagina", defaultValue = "10") int porPagina,
			@RequestParam(name = "desc", defaultValue = "false") boolean reversed,
			@RequestParam(name = "orden", defaultValue = DEFAULT_ORDER_AUTOR) AUTOR_ORDERING ordering,
			@RequestParam(name = "filtro_autor", required = false) String filtroAutor) {
		BusquedaElemento<AUTOR_ORDERING, Autor> busquedaAutor = new BusquedaElemento<>(numeroPagina, porPagina,
				ordering, reversed, filtroAutor);
		log.trace("BusquedaAutor: {}", busquedaAutor);
		return ResponseEntity.ok(bibliotecaService.paginaAutor(busquedaAutor));
	}

	@GetMapping(value = "/generos")
	public ResponseEntity<Pagina<Genero>> paginaGeneros(
			@RequestParam(name = "numero_pagina", defaultValue = "0") int numeroPagina,
			@RequestParam(name = "por_pagina", defaultValue = "10") int porPagina,
			@RequestParam(name = "desc", defaultValue = "false") boolean reversed,
			@RequestParam(name = "orden", defaultValue = DEFAULT_ORDER_GENERO) GENERO_ORDERING ordering,
			@RequestParam(name = "filtro_genero", required = false) String filtroGenero) {
		BusquedaElemento<GENERO_ORDERING, Genero> busquedaGenero = new BusquedaElemento<>(numeroPagina, porPagina,
				ordering, reversed, filtroGenero);
		log.trace("BusquedaGenero: {}", busquedaGenero);
		return ResponseEntity.ok(bibliotecaService.paginaGenero(busquedaGenero));
	}

	@GetMapping(value = "/idiomas")
	public ResponseEntity<Pagina<Idioma>> paginaIdiomas(
			@RequestParam(name = "numero_pagina", defaultValue = "0") int numeroPagina,
			@RequestParam(name = "por_pagina", defaultValue = "10") int porPagina,
			@RequestParam(name = "desc", defaultValue = "false") boolean reversed,
			@RequestParam(name = "orden", defaultValue = DEFAULT_ORDER_IDIOMA) IDIOMA_ORDERING ordering,
			@RequestParam(name = "filtro_idioma", required = false) String filtroIdioma) {
		BusquedaElemento<IDIOMA_ORDERING, Idioma> busquedaIdioma = new BusquedaElemento<>(numeroPagina, porPagina,
				ordering, reversed, filtroIdioma);
		log.trace("BusquedaIdioma: {}", busquedaIdioma);
		return ResponseEntity.ok(bibliotecaService.paginaIdioma(busquedaIdioma));
	}
}