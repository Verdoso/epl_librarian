package org.greeneyed.epl.librarian.services;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

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

	private final PreferencesService preferencesService;
	private Optional<File> calibreMetadata = null;
	private String url;
	private static final String TODOS_LOS_LIBROS_STMNT =
			//
			" SELECT DISTINCT group_concat(AUT.name, ' & '), BOK.title,  BOK.id" +
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
				SQLiteConfig sqLiteConfig = new SQLiteConfig();
				sqLiteConfig.setReadOnly(true);
				log.debug("Path to DB: {}", calibreMetadata.get().getCanonicalPath());
				url = "jdbc:sqlite:" + calibreMetadata.get().getCanonicalPath();
				try (Connection con = DriverManager.getConnection(url, sqLiteConfig.toProperties());
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

	public Set<Integer> updateLibros(BiFunction<String, String, List<Integer>> searchAndUpdateAction) {
		Set<Integer> inCalibre = new HashSet<>();
		if (enabled) {
			try (Connection con = DriverManager.getConnection(url);
					PreparedStatement ps = con.prepareStatement(TODOS_LOS_LIBROS_STMNT);
					ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String titulo = rs.getString(2);
					String autores = rs.getString(1);
					List<Integer> ids = searchAndUpdateAction.apply(titulo, autores);
					if (ids != null) {
						inCalibre.addAll(ids);
					}
				}
			} catch (Exception e) {
				log.error("Error comprobando libros en base de datos de Calibre: {}", e.getMessage());
			}
		}
		return inCalibre;
	}
}
