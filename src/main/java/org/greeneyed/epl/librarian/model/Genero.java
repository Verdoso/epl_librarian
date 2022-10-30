package org.greeneyed.epl.librarian.model;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

import com.googlecode.cqengine.attribute.SimpleAttribute;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Genero implements PuedeSerFavorito {
	private static final String GENERO_ID_PARAM = "generoID";
	private static final String GENERO_NOMBRE_PARAM = "generoNOMBRE";
	private static final String GENERO_LIBROS_PARAM = "generoLIBROS";

	public static final SimpleAttribute<Genero, String> GENERO_ID = attribute(Genero.class, String.class,
			GENERO_ID_PARAM, Genero::getNombre);

	public static final SimpleAttribute<Genero, String> GENERO_NOMBRE = attribute(Genero.class, String.class,
			GENERO_NOMBRE_PARAM, Genero::getNombreNormalizado);

	public static final SimpleAttribute<Genero, Integer> GENERO_LIBROS = attribute(Genero.class, Integer.class,
			GENERO_LIBROS_PARAM, Genero::getLibros);

	@EqualsAndHashCode.Include
	private final String nombre;

	private final int libros;

	private boolean favorito = false;

	public boolean tieneLibros() {
		return libros > 0;
	}

	public String getNombreNormalizado() {
		return Libro.flattenToAscii(nombre);
	}
}
