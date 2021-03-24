package org.greeneyed.epl.librarian.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class PreferencesService {

    private static final String IDIOMAS_PREFERIDOS_KEY = "idiomas_preferidos";
    private static final String AUTORES_PREFERIDOS_KEY = "autores_preferidos";
    private static final String GENEROS_PREFERIDOS_KEY = "generos_preferidos";

    private static final String CALIBRE_HOME_KEY = "calibre_home";

    private static final String ERROR_DETALLADO = "Error detallado";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private File preferencesFile;
    private final Properties preferences = new Properties();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final WriteLock writeLock = lock.writeLock();
    private final ReadLock readLock = lock.readLock();

    private final Set<String> idiomasPreferidos = new HashSet<>();
    private final Set<String> autoresPreferidos = new HashSet<>();
    private final Set<String> generosPreferidos = new HashSet<>();

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
            idiomasPreferidos.addAll(leerIdiomasPreferidos(preferences));
            autoresPreferidos.addAll(leerAutoresPreferidos(preferences));
            generosPreferidos.addAll(leerGenerosPreferidos(preferences));
        } catch (Exception e) {
            log.error("No se han podido leer las preferencias: {}", e.getMessage());
            log.trace(ERROR_DETALLADO, e);
        } finally {
            readLock.unlock();
        }
    }

    private Set<String> leerIdiomasPreferidos(Properties preferences) {
        readLock.lock();
        Set<String> result = Collections.emptySet();
        try {
            if (preferences.containsKey(IDIOMAS_PREFERIDOS_KEY)) {
                result = new HashSet<>(Stream.of(preferences.getProperty(IDIOMAS_PREFERIDOS_KEY).split(","))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("Error parseando idioma preferidos: {}", e.getMessage());
            log.trace("Error detallado", e);
        } finally {
            readLock.unlock();
        }
        return result;
    }

    private Set<String> leerAutoresPreferidos(Properties preferences) {
        readLock.lock();
        Set<String> result = Collections.emptySet();
        try {
            if (preferences.containsKey(AUTORES_PREFERIDOS_KEY)) {
                result = new HashSet<>(Stream.of(preferences.getProperty(AUTORES_PREFERIDOS_KEY).split(","))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("Error parseando autores preferidos: {}", e.getMessage());
            log.trace("Error detallado", e);
        } finally {
            readLock.unlock();
        }
        return result;
    }

    private Set<String> leerGenerosPreferidos(Properties preferences) {
        readLock.lock();
        Set<String> result = Collections.emptySet();
        try {
            if (preferences.containsKey(GENEROS_PREFERIDOS_KEY)) {
                result = new HashSet<>(Stream.of(preferences.getProperty(GENEROS_PREFERIDOS_KEY).split(","))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("Error parseando autores preferidos: {}", e.getMessage());
            log.trace("Error detallado", e);
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public Optional<File> getBDDCalibre() {
        File calibre = null;
        readLock.lock();
        try {
            if (preferences.containsKey(CALIBRE_HOME_KEY)) {
                File temp = new File(preferences.getProperty(CALIBRE_HOME_KEY));
                if (temp.exists() && temp.isDirectory()) {
                    File bddCalibre = new File(temp, "metadata.db");
                    if (bddCalibre.exists() && bddCalibre.isFile() && bddCalibre.canRead()) {
                        calibre = bddCalibre;
                        log.info("Base de datos de Calibre encontrada correctamente: {}", bddCalibre.getAbsolutePath());
                    } else {
                        log.error(
                                "La BDD no se encuentra en el directorio especificado o no es legible: {}. Ignoraremos la configuraci\u00f3n de calibre",
                                bddCalibre.getAbsolutePath());
                    }
                } else {
                    log.error("El directorio especificado no existe: {}. Ignoraremos la configuraci\u00f3n de calibre",
                            temp.getAbsolutePath());
                }
            }
        } finally {
            readLock.unlock();
        }
        return Optional.ofNullable(calibre);
    }

    public void actualizarAutoresPreferidos(Set<String> autoresPreferidosToAdd, Set<String> autoresPreferidosToRemove) {
        writeLock.lock();
        try {
            if (autoresPreferidosToAdd != null) {
                this.autoresPreferidos.addAll(autoresPreferidosToAdd);
            }
            if (autoresPreferidosToRemove != null) {
                this.autoresPreferidos.removeAll(autoresPreferidosToRemove);
            }
            preferences.setProperty(AUTORES_PREFERIDOS_KEY,
                    this.autoresPreferidos.stream().collect(Collectors.joining(",")));
            guardarPreferencias();
        } catch (Exception e) {
            log.error("Error parseando autores preferidos: {}", e.getMessage());
            log.trace("Error detallado", e);
        } finally {
            writeLock.unlock();
        }
    }

    public void actualizarIdiomasPreferidos(Set<String> idiomasPreferidosToAdd,
            Set<String> idiomasNoPreferidosToRemove) {
        writeLock.lock();
        try {
            if (idiomasPreferidosToAdd != null) {
                this.idiomasPreferidos.addAll(idiomasPreferidosToAdd);
            }
            if (idiomasNoPreferidosToRemove != null) {
                this.idiomasPreferidos.removeAll(idiomasNoPreferidosToRemove);
            }
            preferences.setProperty(IDIOMAS_PREFERIDOS_KEY,
                    this.idiomasPreferidos.stream().collect(Collectors.joining(",")));
            guardarPreferencias();
        } catch (Exception e) {
            log.error("Error parseando idiomas preferidos: {}", e.getMessage());
            log.trace("Error detallado", e);
        } finally {
            writeLock.unlock();
        }
    }

    public void actualizarGenerosPreferidos(Set<String> generosPreferidosToAdd,
            Set<String> generosNoPreferidosToRemove) {
        writeLock.lock();
        try {
            if (generosPreferidosToAdd != null) {
                this.generosPreferidos.addAll(generosPreferidosToAdd);
            }
            if (generosNoPreferidosToRemove != null) {
                this.generosPreferidos.removeAll(generosNoPreferidosToRemove);
            }
            preferences.setProperty(GENEROS_PREFERIDOS_KEY,
                    this.generosPreferidos.stream().collect(Collectors.joining(",")));
            guardarPreferencias();
        } catch (Exception e) {
            log.error("Error parseando generos preferidos: {}", e.getMessage());
            log.trace("Error detallado", e);
        } finally {
            writeLock.unlock();
        }
    }

    private void guardarPreferencias() {
        writeLock.lock();
        try (FileOutputStream theFOS = new FileOutputStream(preferencesFile)) {
            preferences.store(theFOS, "Preferencias de EPL Librarian, edítalas manualmente bajo tu propio riesgo");
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

    public boolean checkIdiomaFavorito(String nombre) {
        return idiomasPreferidos.contains(nombre);
    }

    public boolean checkAutorFavorito(List<String> autores) {
        return autores.stream().map(String::trim).anyMatch(this::checkAutorFavorito);
    }

    public boolean checkAutorFavorito(String nombre) {
        return autoresPreferidos.contains(nombre);
    }

    public boolean checkGeneroFavorito(List<String> generos) {
        return generos.stream().map(String::trim).anyMatch(this::checkGeneroFavorito);
    }

    public boolean checkGeneroFavorito(String nombre) {
        return generosPreferidos.contains(nombre);
    }

    public boolean canAplyAutoresFavoritos() {
        return !autoresPreferidos.isEmpty();
    }

    public boolean canAplyIdiomasFavoritos() {
        final boolean result = idiomasPreferidos.isEmpty();
        return !result;
    }

    public boolean canAplyGenerosFavoritos() {
        return !generosPreferidos.isEmpty();
    }
}
