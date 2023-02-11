package org.greeneyed.epl.librarian.model;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

import com.googlecode.cqengine.attribute.SimpleAttribute;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Autor implements PuedeSerFavorito {
  private static final String AUTOR_ID_PARAM = "autorID";
  private static final String AUTOR_NOMBRE_PARAM = "autorNOMBRE";
  private static final String AUTOR_FAVORITO_PARAM = "autorFAVORITO";
  private static final String AUTOR_LIBROS_PARAM = "autorLIBROS";

  public static final SimpleAttribute<Autor, String> AUTOR_ID = attribute(Autor.class, String.class, AUTOR_ID_PARAM, Autor::getNombre);

  public static final SimpleAttribute<Autor, String> AUTOR_NOMBRE = attribute(Autor.class, String.class, AUTOR_NOMBRE_PARAM,
      Autor::getNombreNormalizado);
  public static final SimpleAttribute<Autor, Boolean> AUTOR_FAVORITO = attribute(Autor.class, Boolean.class, AUTOR_FAVORITO_PARAM,
      Autor::getFavorito);
  public static final SimpleAttribute<Autor, Integer> AUTOR_LIBROS = attribute(Autor.class, Integer.class, AUTOR_LIBROS_PARAM, Autor::getLibros);

  @EqualsAndHashCode.Include
  private final String nombre;

  private final int libros;

  private Boolean favorito = Boolean.FALSE;

  public boolean tieneLibros() {
    return libros > 0;
  }

  public String getNombreNormalizado() {
    return Libro.flattenToAscii(nombre);
  }

  public static String getNombreComparable(String unNombre) {
    final String flattened = Libro.flattenToAscii(unNombre);
    return flattened.replaceAll("\\s+\\w\\.\\s+", " ");
  }
}
