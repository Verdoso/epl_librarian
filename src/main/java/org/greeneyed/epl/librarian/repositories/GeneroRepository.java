package org.greeneyed.epl.librarian.repositories;

import org.greeneyed.epl.librarian.model.jpa.JGenero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneroRepository extends JpaRepository<JGenero, Long> {
    JGenero findByNombre(String nombre);
}
