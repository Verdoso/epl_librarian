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
    private static final String LIBRO_AUTOR_PARAM = "libroAUTOR";
    private static final String LIBRO_AUTOR_FAVORITO_PARAM = "libroAUTOR_FAVORITO";
    private static final String LIBRO_COLECCION_PARAM = "libroCOLECCION";
    private static final String LIBRO_GENERO_PARAM = "libroGENERO";
    private static final String LIBRO_GENERO_FAVORITO_PARAM = "libroGENERO_FAVORITO";
    private static final String LIBRO_IDIOMA_PARAM = "libroIDIOMA";
    private static final String LIBRO_IDIOMA_FAVORITO_PARAM = "libroIDIOMA_FAVORITO";
    private static final String LIBRO_VOLUMEN_PARAM = "libroVOLUMEN";
    private static final String LIBRO_PUBLICADO_PARAM = "libroPUBLICADO";
    private static final String LIBRO_EN_CALIBRE_PARAM = "libroEN_CALIBRE";

    public static final SimpleAttribute<Libro, Integer> LIBRO_ID = attribute(LIBRO_ID_PARAM, Libro::getId);
    public static final SimpleAttribute<Libro, String> LIBRO_TITULO = attribute(LIBRO_TITULO_PARAM,
            Libro::getTituloNormalizado);
    public static final SimpleAttribute<Libro, String> LIBRO_AUTOR = attribute(LIBRO_AUTOR_PARAM,
            Libro::getAutorNormalizado);
    public static final SimpleAttribute<Libro, Boolean> LIBRO_AUTOR_FAVORITO = attribute(LIBRO_AUTOR_FAVORITO_PARAM,
            Libro::getAutorFavorito);
    public static final SimpleAttribute<Libro, String> LIBRO_IDIOMA = attribute(LIBRO_IDIOMA_PARAM,
            Libro::getIdiomaNormalizado);
    public static final SimpleAttribute<Libro, Boolean> LIBRO_IDIOMA_FAVORITO = attribute(LIBRO_IDIOMA_FAVORITO_PARAM,
            Libro::getIdiomaFavorito);
    public static final SimpleAttribute<Libro, Boolean> LIBRO_EN_CALIBRE = attribute(LIBRO_EN_CALIBRE_PARAM,
            Libro::getInCalibre);
    public static final SimpleNullableAttribute<Libro, ChronoLocalDate> LIBRO_PUBLICADO = nullableAttribute(
            LIBRO_PUBLICADO_PARAM, Libro::getFechaPublicacion);
    public static final SimpleNullableAttribute<Libro, String> LIBRO_COLECCION = nullableAttribute(
            LIBRO_COLECCION_PARAM, Libro::getColeccionNormalizada);
    public static final SimpleNullableAttribute<Libro, BigDecimal> LIBRO_VOLUMEN = nullableAttribute(
            LIBRO_VOLUMEN_PARAM, Libro::getVolumen);
    public static final SimpleNullableAttribute<Libro, String> LIBRO_GENERO = nullableAttribute(LIBRO_GENERO_PARAM,
            Libro::getGeneroNormalizado);
    public static final SimpleAttribute<Libro, Boolean> LIBRO_GENERO_FAVORITO = attribute(LIBRO_GENERO_FAVORITO_PARAM,
            Libro::getGeneroFavorito);

    @EqualsAndHashCode.Include
    private int id;

    private String titulo;

    private String autor;

    private String generos;

    private String coleccion;

    private BigDecimal volumen;

    //private int anyoPublicacion;

    private String sinopsis;

    private Integer paginas;

    private BigDecimal revision;

    private String idioma;

    private String publicado;

    private LocalDate fechaPublicacion;

    private String estado;

    //private BigDecimal valoracion;

    //private Integer votos;

    private String magnetId;

    private Boolean inCalibre = Boolean.FALSE;

    private Boolean autorFavorito = Boolean.FALSE;

    private Boolean generoFavorito = Boolean.FALSE;

    private Boolean idiomaFavorito = Boolean.FALSE;

    @JsonIgnore
    public List<String> getListaAutores() {
        if (autor != null) {
            return Arrays.asList(autor.split(" & "));
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

    public static String flattenToAscii(String string) {
        if (string != null) {
            StringBuilder sb = new StringBuilder(string.length());
            string = Normalizer.normalize(string.toLowerCase(), Normalizer.Form.NFD);
            for (char c : string.toCharArray()) {
                if (c <= '\u007F') {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }
}
