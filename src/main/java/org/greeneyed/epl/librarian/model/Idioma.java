package org.greeneyed.epl.librarian.model;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

import com.googlecode.cqengine.attribute.SimpleAttribute;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Idioma implements PuedeSerFavorito {
	private static final String IDIOMA_ID_PARAM = "idiomaID";
	private static final String IDIOMA_NOMBRE_PARAM = "idiomaNOMBRE";
	private static final String IDIOMA_LIBROS_PARAM = "idiomaLIBROS";

	public static final SimpleAttribute<Idioma, String> IDIOMA_ID = attribute(Idioma.class, String.class,
			IDIOMA_ID_PARAM, Idioma::getNombre);

	public static final SimpleAttribute<Idioma, String> IDIOMA_NOMBRE = attribute(Idioma.class, String.class,
			IDIOMA_NOMBRE_PARAM, Idioma::getNombreNormalizado);

	public static final SimpleAttribute<Idioma, Integer> IDIOMA_LIBROS = attribute(Idioma.class, Integer.class,
			IDIOMA_LIBROS_PARAM, Idioma::getLibros);

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
