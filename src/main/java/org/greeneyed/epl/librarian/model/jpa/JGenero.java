package org.greeneyed.epl.librarian.model.jpa;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "GENERO")
public class JGenero {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "GEN_ID")
    private Long id;

    @Column(name = "GEN_NOMBRE")
    private String nombre;

    @Column(name = "GEN_NOMBRE_NORMALIZADO")
    private String nombreNormalizado;

    @Column(name = "GEN_FAVORITO")
    private boolean favorito = false;

    public void normalizar() {
        this.nombreNormalizado = JLibro.flattenToAscii(nombre);
    }
//
//    @ManyToMany(targetEntity = JLibro.class, mappedBy = "generos", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    private List<JLibro> libros;

    public static JGenero fromNombre(String nombre) {
        JGenero genero = new JGenero();
        genero.setNombre(nombre);
        genero.normalizar();
        return genero;
    }
}
