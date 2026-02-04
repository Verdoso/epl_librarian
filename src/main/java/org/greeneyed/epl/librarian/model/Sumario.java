package org.greeneyed.epl.librarian.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@Builder
public class Sumario {
  private final String buildVersion;
  private final String latestVersion;
  private final long fechaActualizacion;
  private final int libros;
  private final int idiomas;
  private final int autores;
  private final int generos;
  private final boolean integracionCalibreHabilitada;
  private final boolean recargaEPLHabilitada;
  private final boolean miniaturasEnTabla;
}