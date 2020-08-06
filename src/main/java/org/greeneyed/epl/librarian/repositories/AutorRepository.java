package org.greeneyed.epl.librarian.repositories;

import org.greeneyed.epl.librarian.model.jpa.JAutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutorRepository extends JpaRepository<JAutor, Long> {
    JAutor findByNombre(String nombre);
}
