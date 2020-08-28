package org.greeneyed.epl.librarian.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Sumario {
	private final String buildVersion;
	private final long fechaActualizacion;
	private final int libros;
	private final int idiomas;
	private final int autores;
	private final int generos;
	private final boolean integracionCalibreHabilitada;
}