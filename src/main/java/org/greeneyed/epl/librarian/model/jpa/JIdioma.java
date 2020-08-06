package org.greeneyed.epl.librarian.model.jpa;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "IDIOMA")
public class JIdioma {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "IDI_ID")
    private Long id;

    @Column(name = "IDI_NOMBRE")
    private String nombre;

    @Column(name = "IDI_NOMBRE_NORMALIZADO")
    private String nombreNormalizado;

    @Column(name = "IDI_FAVORITO")
    private boolean favorito = false;

    public void normalizar() {
        this.nombreNormalizado = JLibro.flattenToAscii(nombre);
    }
//
//    @OneToMany(targetEntity = JLibro.class, mappedBy = "idioma", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<JLibro> libros;

    public static JIdioma fromNombre(String nombre) {
        JIdioma autor = new JIdioma();
        autor.setNombre(nombre);
        autor.normalizar();
        return autor;
    }
}
