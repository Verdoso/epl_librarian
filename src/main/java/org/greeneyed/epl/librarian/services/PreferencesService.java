package org.greeneyed.epl.librarian.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class PreferencesService {

	private static final String ERROR_DETALLADO = "Error detallado";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	private File preferencesFile;
	private final Properties preferences = new Properties();

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final WriteLock writeLock = lock.writeLock();
	private final ReadLock readLock = lock.readLock();

	@PostConstruct
	public void postConstruct() {
		preferencesFile = getPreferencesFile();
		log.info("Leyendo preferencias desde {}", preferencesFile.getAbsolutePath());
		if (preferencesFile.exists()) {
			cargarPreferencias();
		} else {
			log.info("Inicializando fichero de preferencias...");
			if (preferencesFile.getParentFile().mkdirs()) {
				guardarPreferencias();
			}
		}
	}

	private void cargarPreferencias() {
		readLock.lock();
		try (FileInputStream theFIS = new FileInputStream(preferencesFile);
				InputStreamReader theISW = new InputStreamReader(theFIS)) {
			preferences.load(theISW);
		} catch (Exception e) {
			log.error("No se han podido leer las preferencias: {}", e.getMessage());
			log.trace(ERROR_DETALLADO, e);
		} finally {
			readLock.unlock();
		}
	}

	private void guardarPreferencias() {
		writeLock.lock();
		try (FileOutputStream theFOS = new FileOutputStream(preferencesFile);
				OutputStreamWriter theOSW = new OutputStreamWriter(theFOS)) {
			preferences.store(theOSW, "Preferencias de EPL Librarian, edítalas manualmente bajo tu propio riesgo");
			theOSW.flush();
			theFOS.flush();
		} catch (Exception e) {
			log.error("No se han podido guardar las preferencias: {}", e.getMessage());
			log.trace(ERROR_DETALLADO, e);
		} finally {
			writeLock.unlock();
		}
	}

	public static File getPreferencesFile() {
		return new File(
				String.join(File.separator, System.getProperty("user.home"), ".librarian", "preferences.properties"));
	}

	public Optional<LocalDate> getFechaBase() {
		LocalDate fechaBase = null;
		final String fechaBaseString = preferences.getProperty("fecha.base");
		if (fechaBaseString != null) {
			try {
				fechaBase = LocalDate.parse(fechaBaseString, FORMATTER);
			} catch (Exception e) {
				log.error("Error parseando fecha base. el formato es DateTimeFormatter.ISO_LOCAL_DATE: {}",
						e.getMessage());
				log.trace(ERROR_DETALLADO, e);
			}
		}
		return Optional.ofNullable(fechaBase);
	}

	public void setFechaBase(LocalDate fechaBase) {
		writeLock.lock();
		try {
			preferences.setProperty("fecha.base", FORMATTER.format(fechaBase));
			guardarPreferencias();
		} finally {
			writeLock.unlock();
		}
	}
}
