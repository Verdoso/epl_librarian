package org.greeneyed.epl.librarian.model;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.nullableAttribute;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.MultiValueNullableAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.SimpleNullableAttribute;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Libro {

  private static final String LIBRO_ID_PARAM = "libroID";
  private static final String LIBRO_TITULO_PARAM = "libroTITULO";
  private static final String LIBRO_TITULO_COMPARABLE_PARAM = "libroTITULOCOMPARABLE";
  private static final String LIBRO_AUTOR_PARAM = "libroAUTOR";
  private static final String LIBRO_AUTORES_PARAM = "libroAUTORES";
  private static final String LIBRO_COLECCION_PARAM = "libroCOLECCION";
  private static final String LIBRO_GENERO_PARAM = "libroGENERO";
  private static final String LIBRO_GENEROS_PARAM = "libroGENEROS";
  private static final String LIBRO_IDIOMA_PARAM = "libroIDIOMA";
  private static final String LIBRO_VOLUMEN_PARAM = "libroVOLUMEN";
  private static final String LIBRO_PUBLICADO_PARAM = "libroPUBLICADO";
  private static final String LIBRO_EN_CALIBRE_PARAM = "libroEN_CALIBRE";
  private static final String LIBRO_DESCARTADO_PARAM = "libroDESCARTADO";

  public static final SimpleAttribute<Libro, Integer> LIBRO_ID = attribute(Libro.class, Integer.class, LIBRO_ID_PARAM, Libro::getId);
  public static final SimpleAttribute<Libro, String> LIBRO_TITULO = attribute(Libro.class, String.class, LIBRO_TITULO_PARAM,
      Libro::getTituloNormalizado);
  public static final SimpleAttribute<Libro, String> LIBRO_TITULO_COMPARABLE = attribute(Libro.class, String.class, LIBRO_TITULO_COMPARABLE_PARAM,
      Libro::getTituloComparable);
  public static final SimpleAttribute<Libro, String> LIBRO_AUTOR = attribute(Libro.class, String.class, LIBRO_AUTOR_PARAM,
      Libro::getAutorNormalizado);
  public static final MultiValueAttribute<Libro, String> LIBRO_AUTORES = attribute(Libro.class, String.class, LIBRO_AUTORES_PARAM,
      Libro::getListaAutoresNormalizados);
  public static final SimpleAttribute<Libro, String> LIBRO_IDIOMA = attribute(Libro.class, String.class, LIBRO_IDIOMA_PARAM,
      Libro::getIdiomaNormalizado);
  public static final SimpleAttribute<Libro, Boolean> LIBRO_EN_CALIBRE = attribute(Libro.class, Boolean.class, LIBRO_EN_CALIBRE_PARAM,
      Libro::getInCalibre);
  public static final SimpleAttribute<Libro, Boolean> LIBRO_DESCARTADO = attribute(Libro.class, Boolean.class, LIBRO_DESCARTADO_PARAM,
      Libro::getDescartado);
  public static final SimpleNullableAttribute<Libro, ChronoLocalDate> LIBRO_PUBLICADO = nullableAttribute(Libro.class, ChronoLocalDate.class,
      LIBRO_PUBLICADO_PARAM, Libro::getFechaPublicacion);
  public static final SimpleNullableAttribute<Libro, String> LIBRO_COLECCION = nullableAttribute(Libro.class, String.class, LIBRO_COLECCION_PARAM,
      Libro::getColeccionNormalizada);
  public static final SimpleNullableAttribute<Libro, BigDecimal> LIBRO_VOLUMEN = nullableAttribute(Libro.class, BigDecimal.class, LIBRO_VOLUMEN_PARAM,
      Libro::getVolumen);
  public static final SimpleNullableAttribute<Libro, String> LIBRO_GENERO = nullableAttribute(Libro.class, String.class, LIBRO_GENERO_PARAM,
      Libro::getGeneroNormalizado);
  public static final MultiValueNullableAttribute<Libro, String> LIBRO_GENEROS = nullableAttribute(Libro.class, String.class, LIBRO_GENEROS_PARAM,
      Libro::getListaGenerosNormalizados);

  @EqualsAndHashCode.Include
  private int id;

  private String titulo;

  private String autor;

  private String generos;

  private String coleccion;

  private BigDecimal volumen;

  private int anyoPublicacion;

  private String sinopsis;

  private Integer paginas;

  private BigDecimal revision;

  private String idioma;

  private String publicado;

  private LocalDate fechaPublicacion;

  private String estado;

  // private BigDecimal valoracion;

  // private Integer votos;

  private String magnetId;

  private String portada;

  private Boolean inCalibre = Boolean.FALSE;

  private Boolean descartado = Boolean.FALSE;

  @JsonIgnore
  public List<String> getListaAutores() {
    if (autor != null) {
      return Arrays.asList(autor.split(" & "));
    } else {
      return Collections.emptyList();
    }
  }

  public List<String> getListaAutoresNormalizados() {
    if (autor != null) {
      return Arrays.asList(autor.split(" & "))
          .stream()
          .map(Libro::flattenToAscii)
          .toList();
    } else {
      return Collections.emptyList();
    }
  }

  @JsonIgnore
  public List<String> getListaGeneros() {
    if (generos != null) {
      return Arrays.asList(generos.split(","));
    } else {
      return Collections.emptyList();
    }
  }

  @JsonIgnore
  public List<String> getListaGenerosNormalizados() {
    if (generos != null) {
      return Arrays.asList(generos.split(","))
          .stream()
          .map(Libro::flattenToAscii)
          .toList();
    } else {
      return Collections.emptyList();
    }
  }

  @JsonProperty("magnet_ids")
  public List<String> getListaMagnetIds() {
    if (magnetId != null) {
      return Arrays.asList(magnetId.split(", "));
    } else {
      return Collections.emptyList();
    }
  }

  public String getColeccionCompleta() {
    String completa = null;
    if (coleccion != null) {
      if (volumen != null) {
        completa = coleccion + " [" + volumen + "]";
      } else {
        completa = coleccion;
      }
    }
    return completa;
  }

  @JsonIgnore
  public String getTituloNormalizado() {
    return flattenToAscii(titulo);
  }

  @JsonIgnore
  public String getTituloComparable() {
    return tituloComparable(titulo);
  }

  @JsonIgnore
  public String getAutorNormalizado() {
    return flattenToAscii(autor);
  }

  @JsonIgnore
  public String getIdiomaNormalizado() {
    return flattenToAscii(idioma);
  }

  @JsonIgnore
  public String getColeccionNormalizada() {
    return flattenToAscii(coleccion);
  }

  @JsonIgnore
  public String getGeneroNormalizado() {
    if (generos != null) {
      return flattenToAscii(generos).replace(",", "");
    } else {
      return null;
    }
  }

  public static String tituloComparable(String unNombre) {
    final String flattened = Libro.flattenToAscii(unNombre);
    return flattened.replaceAll("[\\.\\(\\)\\-\\s]", "");
  }

  public static String flattenToAscii(String string) {
    if (string != null) {
      string = string.trim();
      StringBuilder sb = new StringBuilder(string.length());
      string = Normalizer.normalize(string.toLowerCase(), Normalizer.Form.NFD);
      for (char c : string.toCharArray()) {
        if (c <= '\u007F') {
          sb.append(c);
        }
      }
      // log.info("Nombre normalizado {}", sb);
      return sb.toString();
    } else {
      return null;
    }
  }

}
