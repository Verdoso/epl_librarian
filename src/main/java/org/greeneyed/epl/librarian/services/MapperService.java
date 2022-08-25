package org.greeneyed.epl.librarian.services;

import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.LibroCSV;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MapperService {

  @Mapping(ignore = true, target = "listaGeneros")
  @Mapping(ignore = true, target = "inCalibre")
  @Mapping(ignore = true, target = "listaAutores")
  @Mapping(ignore = true, target = "listaMagnetIds")
  Libro from(LibroCSV libroCSV);

}
