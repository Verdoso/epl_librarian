package org.greeneyed.epl.librarian.model;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

import com.googlecode.cqengine.attribute.SimpleAttribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Idioma {
	private static final String IDIOMA_ID_PARAM = "idiomaID";
	private static final String IDIOMA_NOMBRE_PARAM = "idiomaNOMBRE";
	private static final String IDIOMA_LIBROS_PARAM = "idiomaLIBROS";

	public static final SimpleAttribute<Idioma, String> IDIOMA_ID = attribute(IDIOMA_ID_PARAM, Idioma::getNombre);

	public static final SimpleAttribute<Idioma, String> IDIOMA_NOMBRE = attribute(IDIOMA_NOMBRE_PARAM,
			Idioma::getNombreNormalizado);

	public static final SimpleAttribute<Idioma, Integer> IDIOMA_LIBROS = attribute(IDIOMA_LIBROS_PARAM, Idioma::getLibros);

	private String nombre;

	private int libros;

	public String getNombreNormalizado() {
		return Libro.flattenToAscii(nombre);
	}
}
