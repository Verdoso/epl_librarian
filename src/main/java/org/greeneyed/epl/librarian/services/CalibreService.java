package org.greeneyed.epl.librarian.services;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.greeneyed.epl.librarian.model.Libro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sqlite.SQLiteConfig;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class CalibreService {

  @Getter
  enum CALIBRE_LANGUAGE {
    spa("Español"), eng("Inglés"), cat("Catalán"), deu("Alemán"), ita("Italiano"), fra("Francés"), epo("Esperanto"), eus("Euskera"), glg("Gallego"),
    por("Portugués"), swe("Sueco");

    private CALIBRE_LANGUAGE(String eplLabel) {
      this.eplLabel = Libro.flattenToAscii(eplLabel);
    }

    private final String eplLabel;
  }

  @FunctionalInterface
  public static interface TriFunction<T, U, V, R> {

    R apply(T t,
        U u,
        V v);

    default <K> TriFunction<T, U, V, K> andThen(Function<? super R, ? extends K> after) {
      Objects.requireNonNull(after);
      return (T t,
          U u,
          V v) -> after.apply(apply(t, u, v));
    }
  }

  private final PreferencesService preferencesService;
  private Optional<File> calibreMetadata = null;
  private String url;
  private static final String TODOS_LOS_LIBROS_STMNT = """
       SELECT DISTINCT
       	group_concat(AUT.name, ' & '),
       	BOK.title,
       	BOK.id,
      	LNG.lang_code,
      	IDE.id epl_id
       FROM books BOK
       INNER JOIN books_authors_link LNK
         ON LNK.book = BOK.id
       INNER JOIN authors AUT
         ON AUT.id = LNK.author
       INNER JOIN books_languages_link LNGK
         ON LNGK.book = BOK.id
       INNER JOIN languages LNG
         ON LNG.id = LNGK.lang_code
       LEFT JOIN (SELECT * FROM identifiers WHERE type='epl') IDE
         ON IDE.book = BOK.ID
       GROUP BY BOK.id
       ORDER BY BOK.id, LNK.id
      """;

  private static final String INSERT_EPL_ID = """
      INSERT INTO identifiers
      	(book,type,val)
      VALUES
      	(?,'epl',?)
      """;

  @Getter
  private boolean enabled = false;
  @Getter
  private boolean updating = false;

  @PostConstruct
  public void postConstruct() {
    calibreMetadata = preferencesService.getBDDCalibre();
    if (calibreMetadata.isPresent()) {
      try {
        log.debug("Path to DB: {}", calibreMetadata.get()
            .getCanonicalPath());
        url = "jdbc:sqlite:" + calibreMetadata.get()
            .getCanonicalPath();
        try (Connection con = DriverManager.getConnection(url, sqliteConfig().toProperties());
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(1) FROM books");
            ResultSet rs = ps.executeQuery()) {
          enabled = true;
          log.info("Integraci\u00f3n con Calibre activada{}. ( Detectados {} libros )",
              preferencesService.isUpdatingCalibre() ? "( con actualización )" : "( solo lectura )", rs.getInt(1));
        }
      } catch (Exception e) {
        log.error("Error comprobando integraci\u00f3n con Calibre: {}", e.getMessage());
      }
    }
  }

  private SQLiteConfig sqliteConfig() {
    SQLiteConfig sqLiteConfig = new SQLiteConfig();
    sqLiteConfig.setReadOnly(!preferencesService.isUpdatingCalibre());
    return sqLiteConfig;
  }

  public Set<Integer> updateLibros(TriFunction<String, String, CALIBRE_LANGUAGE, List<Integer>> searchAndUpdateAction) {
    Set<Integer> inCalibre = new HashSet<>();
    if (enabled) {
      try (Connection con = DriverManager.getConnection(url, sqliteConfig().toProperties());
          PreparedStatement insert = con.prepareStatement(INSERT_EPL_ID);
          PreparedStatement ps = con.prepareStatement(TODOS_LOS_LIBROS_STMNT);
          ResultSet rs = ps.executeQuery()) {
        boolean needsToInsert = false;
        while (rs.next()) {
          String autores = rs.getString(1);
          String titulo = rs.getString(2);
          Integer calibreId = (Integer) rs.getObject(3);
          String languageCode = rs.getString(4);
          CALIBRE_LANGUAGE calibreLanguage = null;
          try {
            calibreLanguage = CALIBRE_LANGUAGE.valueOf(languageCode);
          } catch (Exception e) {
            log.error("Lenguaje de Calibre no detectado, notifícalo y lo añadiremos a la lista: {}", languageCode);
          }
          Integer eplId = (Integer) rs.getObject(5);
          if (eplId != null) {
            inCalibre.add(eplId);
          } else {
            List<Integer> ids = searchAndUpdateAction.apply(titulo, autores, calibreLanguage);
            if (ids != null) {
              inCalibre.addAll(ids);
              if (preferencesService.isUpdatingCalibre()) {
                Integer detectedEplId = ids.get(0);
                try {
                  log.debug("Detectado el libro {}({}) como el libro {} en Calibre", detectedEplId, titulo, calibreId);
                  insert.setInt(1, calibreId);
                  insert.setInt(2, detectedEplId);
                  insert.addBatch();
                  needsToInsert |= true;
                } catch (SQLException e) {
                  log.error("Error actualizando identificador en Calibre: {}({}), {} -> {}", calibreId, titulo, detectedEplId, e.getMessage());
                }
              }
            }
          }
        }
        if (needsToInsert) {
          try {
            insert.executeBatch();
          } catch (SQLException e) {
            log.error("Error actualizando Calibre: {}", e.getMessage());
          }
        }
      } catch (Exception e) {
        log.error("Error comprobando libros en base de datos de Calibre: {}", e.getMessage());
      }
    }
    return inCalibre;
  }
}
