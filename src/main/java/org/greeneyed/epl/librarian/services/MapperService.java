package org.greeneyed.epl.librarian.services;

import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.LibroCSV;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MapperService {

  @Mapping(ignore = true, target = "listaGeneros")
  Libro from(
      LibroCSV libroCSV);

}
