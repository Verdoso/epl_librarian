package org.greeneyed.epl.librarian.model.jpa;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "LIBRO")
public class JLibro {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "LIB_ID")
    private Long id;

    @Column(name = "LIB_TITULO")
    private String titulo;

    @Column(name = "LIB_COLECCION")
    private String coleccion;

    @Column(name = "LIB_VOLUMEN")
    private BigDecimal volumen;

    @Column(name = "LIB_ANYO_PUBLICACION")
    private int anyoPublicacion;

    @Column(name = "LIB_SINOPSIS", length = 25000)
    private String sinopsis;

    @Column(name = "LIB_PAGINAS")
    private Integer paginas;

    @Column(name = "LIB_REVISION")
    private BigDecimal revision;

    @Column(name = "LIB_PUBLICADO")
    private String publicado;

    @Column(name = "LIB_FECHA_PUBLICACION")
    private LocalDate fechaPublicacion;

    @Column(name = "LIB_ESTADO")
    private String estado;

    @Column(name = "LIB_VALORACION")
    private BigDecimal valoracion;

    @Column(name = "LIB_VOTOS")
    private Integer votos;

    @Column(name = "LIB_MAGNET_ID")
    private String magnetId;

    @Column(name = "LIB_EN_CALIBRE")
    private boolean inCalibre;

//    @ManyToOne(optional = false)
//    @JoinColumn(name = "LIB_IDI_ID")
//    private JIdioma idioma;
//
//    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    @JoinTable(name = "LIBRO_AUTOR", joinColumns = @JoinColumn(name = "LBA_AUT_ID"), inverseJoinColumns = @JoinColumn(name = "LBA_LIB_ID"))
//    private List<JAutor> autores;
//
//    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    @JoinTable(name = "LIBRO_GENERO", joinColumns = @JoinColumn(name = "LBG_GEN_ID"), inverseJoinColumns = @JoinColumn(name = "LBG_LIB_ID"))
//    private List<JGenero> generos;
//
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
