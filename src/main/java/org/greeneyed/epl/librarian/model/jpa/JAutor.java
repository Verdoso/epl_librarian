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
@Table(name = "AUTOR")
public class JAutor {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "AUT_ID")
    private Long id;

    @Column(name = "AUT_NOMBRE")
    private String nombre;

    @Column(name = "AUT_NOMBRE_NORMALIZADO")
    private String nombreNormalizado;

    @Column(name = "AUT_FAVORITO")
    private boolean favorito = false;

    public void normalizar() {
        this.nombreNormalizado = JLibro.flattenToAscii(nombre);
    }
//
//    @ManyToMany(targetEntity = JLibro.class, mappedBy = "autores", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
//    private List<JLibro> libros;

    public static JAutor fromNombre(String nombre) {
        JAutor autor = new JAutor();
        autor.setNombre(nombre);
        autor.normalizar();
        return autor;
    }
}
