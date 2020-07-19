package org.greeneyed.epl.librarian.services;

import org.greeneyed.epl.librarian.model.Libro;
import org.greeneyed.epl.librarian.services.EplCSVProcessor.LibroCSV;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapperService {
	Libro from(LibroCSV libroCSV);	
}
