package org.greeneyed.epl.librarian.model;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

import com.googlecode.cqengine.attribute.SimpleAttribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genero {
	private static final String GENERO_ID_PARAM = "generoID";
	private static final String GENERO_NOMBRE_PARAM = "generoNOMBRE";
	private static final String GENERO_LIBROS_PARAM = "generoLIBROS";

	public static final SimpleAttribute<Genero, String> GENERO_ID = attribute(GENERO_ID_PARAM, Genero::getNombre);

	public static final SimpleAttribute<Genero, String> GENERO_NOMBRE = attribute(GENERO_NOMBRE_PARAM,
			Genero::getNombreNormalizado);

	public static final SimpleAttribute<Genero, Integer> GENERO_LIBROS = attribute(GENERO_LIBROS_PARAM, Genero::getLibros);

	private String nombre;

	private int libros;

	public String getNombreNormalizado() {
		return Libro.flattenToAscii(nombre);
	}
}
