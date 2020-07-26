package org.greeneyed.epl.librarian.model;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

import com.googlecode.cqengine.attribute.SimpleAttribute;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Autor implements PuedeSerFavorito {
    private static final String AUTOR_ID_PARAM = "autorID";
    private static final String AUTOR_NOMBRE_PARAM = "autorNOMBRE";
    private static final String AUTOR_LIBROS_PARAM = "autorLIBROS";

    public static final SimpleAttribute<Autor, String> AUTOR_ID = attribute(AUTOR_ID_PARAM, Autor::getNombre);

    public static final SimpleAttribute<Autor, String> AUTOR_NOMBRE = attribute(AUTOR_NOMBRE_PARAM,
            Autor::getNombreNormalizado);

    public static final SimpleAttribute<Autor, Integer> AUTOR_LIBROS = attribute(AUTOR_LIBROS_PARAM, Autor::getLibros);

    @EqualsAndHashCode.Include
    private final String nombre;

    private final int libros;

    private boolean favorito = false;

    public String getNombreNormalizado() {
        return Libro.flattenToAscii(nombre);
    }
}
