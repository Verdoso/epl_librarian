package org.greeneyed.epl.librarian.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.stereotype.Service;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EplCSVProcessor {

	private static final String BACKUP_FILES_PREFIX = "Libros";
	private static final String BACKUP_FILES_SUFFIX = ".epl_bck";
	private static final String EPUB_LIBRE_CSV = "https://epublibre.org/rssweb/csv";
	private static final String ERROR_DETALLADO = "Error detallado";

	@Data
	public static class LibroCSV implements Serializable {


		private static final long serialVersionUID = 1L;

		private static final DateTimeFormatter PUBLICADO_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		@CsvBindByPosition(position = 0)
		private int id;

		@CsvBindByPosition(position = 1)
		private String titulo;

		@CsvBindByPosition(position = 2)
		private String autor;

		@CsvBindByPosition(position = 3)
		private String generos;

		@CsvBindByPosition(position = 4)
		private String coleccion;

		public String getColeccion() {
			return StringUtils.isBlank(coleccion) ? "" : coleccion;
		}

		@CsvBindByPosition(position = 5)
		private BigDecimal volumen;

		@CsvBindByPosition(position = 6)
		private int anyoPublicacion;

		@CsvBindByPosition(position = 7)
		private String sinopsis;

		@CsvBindByPosition(position = 8)
		private Integer paginas;

		@CsvBindByPosition(position = 9)
		private BigDecimal revision;

		@CsvBindByPosition(position = 10)
		private String idioma;

		@CsvBindByPosition(position = 11)
		private String publicado;

		@CsvBindByPosition(position = 12)
		private String estado;

		@CsvBindByPosition(position = 13)
		private BigDecimal valoracion;

		@CsvBindByPosition(position = 14)
		private Integer votos;

		@CsvBindByPosition(position = 15)
		private String magnetId;

		public LocalDate getFechaPublicacion() {
			LocalDate temp = null;
			try {
				if (StringUtils.isNotBlank(publicado)) {
					temp = LocalDate.parse(publicado.substring(publicado.indexOf(":") + 1), PUBLICADO_FORMATTER);
				}
			} catch (Exception e) {
				log.error("Error parseando fecha de publicacion {}", publicado, e.getMessage());
				log.trace(ERROR_DETALLADO, e);
			}
			return temp;
		}
	}

	@Data
	public static class UpdateSpec {
		public static final UpdateSpec NO_SPEC = new UpdateSpec(null, Collections.emptyList());

		private final LocalDateTime fechaActualizacion;
		private final List<LibroCSV> libroCSVs;

		public boolean isEmpty() {
			return libroCSVs == null || libroCSVs.isEmpty();
		}

		public Duration getAntiguedad() {
			return Duration.ofHours(fechaActualizacion == null ? Integer.MAX_VALUE
					: fechaActualizacion.until(LocalDateTime.now(), ChronoUnit.HOURS));
		}
	}

	UpdateSpec processBackup() {
		UpdateSpec updateSpec = UpdateSpec.NO_SPEC;
		try {
			updateSpec = backupFiles().sorted(Comparator.comparing(File::lastModified).reversed())
					.findFirst()
					.map(file -> {
						log.warn("Usando el fichero de backup: {}", file.getAbsolutePath());
						LocalDateTime fechaActualizacion = Instant.ofEpochMilli(file.lastModified())
								.atZone(ZoneId.systemDefault())
								.toLocalDateTime();
						List<LibroCSV> librosCSVs = new ArrayList<>();
						try (FileInputStream theFIS = new FileInputStream(file);
								BufferedInputStream theBIS = new BufferedInputStream(theFIS);
								ObjectInputStream theOIS = new ObjectInputStream(theBIS)) {
							LibroCSV libroCSV;
							while ((libroCSV = (LibroCSV) theOIS.readObject()) != null) {
								log.trace("Leido libro: {}", libroCSV);
								librosCSVs.add(libroCSV);
							}
						} catch (EOFException e) {
							log.debug("Llegamos al final del fichero, así es como funciona el ObjectInputStream");
						} catch (Exception e) {
							log.error("Error leyendo fichero de backup", e);
							librosCSVs.clear();
						}
						log.warn("Backup Le\u00eddo con {} libros.", librosCSVs.size());
						return new UpdateSpec(fechaActualizacion, librosCSVs);
					})
					.orElse(UpdateSpec.NO_SPEC);
			// Borradmos los ficheros de backup que no sean el ultimo
			backupFiles().sorted(Comparator.comparing(File::lastModified).reversed()).skip(1).forEach(File::delete);
		} catch (IOException e) {
			log.error("Error buscando ficheros en el directorio temporal", e);
		}
		return updateSpec;
	}

	private Stream<File> backupFiles() throws IOException {
		Path tempDirectory = getTempDirectory();
		return Stream.of(tempDirectory.toFile().listFiles(file -> {
			String fileName = file.getName();
			return file.isFile() && fileName.startsWith(BACKUP_FILES_PREFIX)
					&& fileName.endsWith("." + LibroCSV.serialVersionUID + BACKUP_FILES_SUFFIX);
		}));
	}

	private Path getTempDirectory() throws IOException {
		return Files.createTempDirectory("whatever").getParent();
	}

	UpdateSpec procesarEplCSV(InputStream theIS) {
		UpdateSpec updateSpec = UpdateSpec.NO_SPEC;
		List<LibroCSV> librosCSVs = new ArrayList<>();
		if (theIS != null) {
			try (ZipInputStream theZIS = new ZipInputStream(theIS);
					InputStreamReader theISR = new InputStreamReader(theZIS);
					Reader theReader = new BufferedReader(theISR)) {
				log.info("Descomprimiendo fichero EPL");
				ZipEntry nextEntry = null;
				while ((nextEntry = theZIS.getNextEntry()) != null) {
					FileTime fileTime = nextEntry.getCreationTime();
					if (fileTime == null) {
						fileTime = nextEntry.getLastModifiedTime();
					}
					if (fileTime != null) {
						LocalDateTime fechaActualizacion = Instant.ofEpochMilli(fileTime.toMillis())
								.atZone(ZoneId.systemDefault())
								.toLocalDateTime();
						log.info("Procesando fichero {} con fecha {}", nextEntry.getName(),
								DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(fechaActualizacion));
					} else {
						log.info("Procesando fichero {}. Fecha no definida en el comprimido", nextEntry.getName());
					}
					ColumnPositionMappingStrategy<LibroCSV> ms = new ColumnPositionMappingStrategy<>();
					ms.setType(LibroCSV.class);
					CsvToBean<LibroCSV> cb = new CsvToBeanBuilder<LibroCSV>(theReader).withType(LibroCSV.class)
							.withMappingStrategy(ms)
							.withSkipLines(1)
							.build();
					librosCSVs = cb.parse();
					File librosBCKFile = Files
							.createTempFile(BACKUP_FILES_PREFIX, "." + LibroCSV.serialVersionUID + BACKUP_FILES_SUFFIX)
							.toFile();
					try (FileOutputStream theFOS = new FileOutputStream(librosBCKFile);
							BufferedOutputStream theBOS = new BufferedOutputStream(theFOS);
							ObjectOutputStream theOOS = new ObjectOutputStream(theBOS)) {
						for (LibroCSV libroCSV : librosCSVs) {
							theOOS.writeObject(libroCSV);
						}
						theOOS.flush();
						theBOS.flush();
						theFOS.flush();
						log.info("Creado fichero de backup: {}", librosBCKFile.getAbsolutePath());
					}
					return new UpdateSpec(Instant.ofEpochMilli(librosBCKFile.lastModified())
							.atZone(ZoneId.systemDefault())
							.toLocalDateTime(), librosCSVs);
				}
			} catch (IOException e) {
				log.error("Error leyendo fichero: {}", e.getMessage());
				log.debug(ERROR_DETALLADO, e);
			}
		} else {
			log.info("No hay fichero en el classpath");
		}
		return updateSpec;
	}

	public UpdateSpec updateFromEPL() {
		UpdateSpec updateSpec = UpdateSpec.NO_SPEC;
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout((int) TimeUnit.MILLISECONDS.convert(50_000, TimeUnit.SECONDS))
				.setConnectTimeout((int) TimeUnit.MILLISECONDS.convert(50_000, TimeUnit.SECONDS))
				.setSocketTimeout((int) TimeUnit.MILLISECONDS.convert(50_000, TimeUnit.SECONDS))
				.build();
		try (CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultRequestConfig(defaultRequestConfig)
				.setSSLSocketFactory(getTolerantSSLSocketFactory())
				.build();) {
			updateSpec = descargaFicheroDeEPL(httpclient);
		} catch (Exception e) {
			log.error("Error descargando actualización desde EPL", e);
		}
		return updateSpec;
	}

	private UpdateSpec descargaFicheroDeEPL(CloseableHttpClient httpclient) throws IOException {
		UpdateSpec updateSpec = UpdateSpec.NO_SPEC;
		HttpGet httpget = new HttpGet(EPUB_LIBRE_CSV);
		log.info("Descargando fichero desde EPL...");
		File librosEPL = Files.createTempFile(BACKUP_FILES_PREFIX, ".csv.zip").toFile();
		try (CloseableHttpResponse response = httpclient.execute(httpget)) {
			HttpEntity entity = response.getEntity();
			ContentType contentType = ContentType.getOrDefault(entity);
			log.debug("El fichero descargado es del tipo {}", contentType.getMimeType());
			if (!"text/html".equalsIgnoreCase(contentType.getMimeType())) {
				FileUtils.copyInputStreamToFile(entity.getContent(), librosEPL);
				log.info("Descargado el fichero en {}", librosEPL.getAbsolutePath());
				try (FileInputStream theFIS = new FileInputStream(librosEPL)) {
					updateSpec = procesarEplCSV(theFIS);
				} catch (IOException e) {
					log.error("Error procesando fichero descargado: {}", librosEPL.getAbsoluteFile(), e);
				}
			} else {
				showErrorServidorSobrecargado();
			}
			try {
				Files.delete(librosEPL.toPath());
			} catch (Exception e) {
				log.error("No se ha podido borrar el fichero {} : {}", librosEPL.getAbsolutePath(), e.getMessage());
			}
		}
		return updateSpec;
	}

	public UpdateSpec updateFromEPLManual() {
		UpdateSpec updateSpec = UpdateSpec.NO_SPEC;
		File librosEPL;
		try {
			librosEPL = new File(getTempDirectory().toFile(), "epublibre_csv.zip");
			if (librosEPL.exists()) {
				log.info("Encontrada descarga manual ({}), procesando...", librosEPL.getAbsoluteFile());
				try (FileInputStream theFIS = new FileInputStream(
						new File(getTempDirectory().toFile(), "epublibre_csv.zip"))) {
					updateSpec = procesarEplCSV(theFIS);
				} catch (IOException e) {
					log.error("Error procesando descarga manual: {} : {}", librosEPL.getAbsoluteFile(), e.getMessage());
					log.trace(ERROR_DETALLADO, e);
				}
			}
		} catch (IOException e1) {
			log.error("Error intentando leer descarga manul: {}", e1.getMessage());
			log.trace(ERROR_DETALLADO, e1);
		}
		return updateSpec;
	}

	private void showErrorServidorSobrecargado() throws IOException {
		log.error("Error descomprimiendo fichero descargado, no es un formato ZIP correcto.\n"
				+ "Seguramente EPL ha devuelto una página de \"Servidor sobrecargado\" o"
				+ " \"Se ha superado el numero maximo de descargas diarias del fichero (X)\" en su lugar.");
		log.error(
				"Si el error persiste, compruebe la URL {} manualmente\n y si consigue descargarse el fichero guardelo en el directorio {}",
				EPUB_LIBRE_CSV, getTempDirectory().toFile().getAbsolutePath());
	}

	private SSLConnectionSocketFactory getTolerantSSLSocketFactory()
			throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		}).build();
		return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
	}
}
