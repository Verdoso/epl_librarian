package org.greeneyed.epl.librarian.repositories;

import org.greeneyed.epl.librarian.model.jpa.JLibro;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroRepository extends JpaRepository<JLibro, Long>, JpaSpecificationExecutor<JLibro> {
    public static Specification<JLibro> tituloIgual(String titulo) {
        return (root, query, builder) -> builder.equal(root.get("titulo"), JLibro.flattenToAscii(titulo));
    }

    public static Specification<JLibro> contieneAutor(String autor) {
        return (root, query, builder) -> {
            return builder.equal(root.join("autores").get("nombre"), autor);
        };
    }
}
