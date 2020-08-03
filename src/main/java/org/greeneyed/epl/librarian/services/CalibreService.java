package org.greeneyed.epl.librarian.services;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.greeneyed.epl.librarian.model.Libro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class CalibreService {

    private final PreferencesService preferencesService;
    private Optional<File> calibreMetadata = null;
    private String url;
    private static final String BUSCA_TITULO_STMNT =
            //
            " SELECT DISTINCT group_concat(AUT.name, ' & '), BOK.id" +
            //
                    " FROM books BOK" +
                    //
                    " INNER JOIN books_authors_link LNK" +
                    //
                    "   ON LNK.book = BOK.id" +
                    //
                    " INNER JOIN authors AUT" +
                    //
                    "   ON AUT.id = LNK.author" +
                    //
                    " WHERE LOWER(title) = ?" +
                    //
                    " GROUP BY BOK.id" +
                    //
                    " ORDER BY BOK.id, LNK.id";

    @Getter
    private boolean enabled = false;

    @PostConstruct
    public void postConstruct() {
        calibreMetadata = preferencesService.getBDDCalibre();
        if (calibreMetadata.isPresent()) {
            try {
                log.debug("Path to DB: {}", calibreMetadata.get().getCanonicalPath());
                url = "jdbc:sqlite:" + calibreMetadata.get().getCanonicalPath();
                try (Connection con = DriverManager.getConnection(url);
                        PreparedStatement ps = con.prepareStatement("SELECT COUNT(1) FROM books");
                        ResultSet rs = ps.executeQuery()) {
                    enabled = true;
                    log.info("Integraci\u00f3n con Calibre activada. ( Detectados {} libros )", rs.getInt(1));
                }
            } catch (Exception e) {
                log.error("Error comprobando integraci\u00f3n con Calibre: {}", e.getMessage());
            }
        }
    }

    public void updateLibros(List<Libro> libros) {
        if (enabled) {
            try (Connection con = DriverManager.getConnection(url);
                    PreparedStatement ps = con.prepareStatement(BUSCA_TITULO_STMNT)) {
                for (Libro libro : libros) {
                    log.debug("Comprobando libro {}", libro.getTitulo());
                    ps.setString(1, libro.getTitulo().toLowerCase());
                    boolean found = false;
                    List<String> authorsFound = new ArrayList<>();
                    try (ResultSet rs = ps.executeQuery();) {
                        while (rs.next() && !found) {
                            String autores = rs.getString(1);
                            if (autores != null && libro.getAutor() !=null && autores.trim().equalsIgnoreCase(libro.getAutor().trim())) {
                                found = true;
                            } else {
                                authorsFound.add(autores);
                            }
                        }
                        if(!found && !authorsFound.isEmpty()) {
                            log.info(
                                    "Libro con mismo título ({}) encontrado en Calibre pero con distintos autores. {} != {}",
                                    libro.getTitulo(), libro.getAutor(), authorsFound.stream().collect(Collectors.joining(", ")));
                        }
                    }
                    if (found) {
                        log.debug("Libro encontrado en Calibre: {}", libro.getTitulo());
                        libro.setInCalibre(true);
                    }

                }
            } catch (Exception e) {
                log.error("Error comprobando libros en base de datos de Calibre: {}", e.getMessage());
            }
        }
    }
}
