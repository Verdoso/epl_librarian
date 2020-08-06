package org.greeneyed.epl.librarian.repositories;

import org.greeneyed.epl.librarian.model.jpa.JIdioma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdiomaRepository extends JpaRepository<JIdioma, Long> {
    JIdioma findByNombre(String nombre);
}
