package org.greeneyed.epl.librarian.services;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import org.greeneyed.epl.librarian.services.EplCSVProcessor.UpdateSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Data
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class DataLoaderService implements ApplicationRunner, EnvironmentAware {

	@Value("${descarga_epl:true}")
	private boolean descargarDeEPL;

	@Value("${actualizacion_automatica:true}")
	private boolean actualizacionAutomatica;

	@Value("${antiguedad_maxima:24}")
	private int antiguedadMaxima;

	private Environment environment;

	private final EplCSVProcessor eplCSVProcessor;

	private final BibliotecaService bibliotecaService;

	@Override
	public void run(ApplicationArguments arguments) throws Exception {
		log.info("Inicializando datos...");
		boolean comprobaremosActualizacionAutomatica = actualizacionAutomatica;
		UpdateSpec updateSpec = eplCSVProcessor.processBackup();
		if (updateSpec.isEmpty()) {
			if (descargarDeEPL) {
				updateSpec = eplCSVProcessor.updateFromEPL();
			}
			if (updateSpec.isEmpty()) {
				updateSpec = eplCSVProcessor.updateFromEPLManual();
			}
		} else {
			if (descargarDeEPL && comprobaremosActualizacionAutomatica) {
				updateSpec = comprobarActualizacionAutomatica(updateSpec);
			} else {
				if (!descargarDeEPL) {
					log.info("La descarga desde EPL est\u00e1 deshabilitada");
				}
				if (!comprobaremosActualizacionAutomatica) {
					log.info("La comprobaci\u00f3n de actualizaciones autom\u00e1ticas est\u00e1 deshablitada");
				}
			}
		}
		//
		if (updateSpec.isEmpty()) {
			throw new IOException("Sin datos, para qu\u00e9 arrancar.");
		} else {
			log.info("Preparando {} libros de la descarga con fecha {}", updateSpec.getLibroCSVs().size(),
					DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(updateSpec.getFechaActualizacion()));
			bibliotecaService.update(updateSpec);
			log.info("EPL Librarian inicializado");
			abrirEnNavegador();
		}
	}

	private void abrirEnNavegador() {
		if (!environment.acceptsProfiles(Profiles.of("devel"))) {
			try {
				final URI appURI = new URI(
						"http://localhost:" + environment.getProperty("local.server.port") + "/librarian/");
				try {
					Desktop.getDesktop().browse(appURI);
				} catch (java.awt.HeadlessException e) {
					log.warn(
							"El sistema es Headless, no se puede abrir el navegador automaticamente. La dirección para acceder es {}", appURI);
				}
			} catch (Exception e) {
				log.error("Error abriendo navegador autom\u00e1ticamente", e);
			}
		}
	}

	private UpdateSpec comprobarActualizacionAutomatica(UpdateSpec updateSpec) {
		log.info("El backup tiene una antiguedad de {} horas...", updateSpec.getAntiguedad().toHours());
		if (updateSpec.getAntiguedad().compareTo(Duration.ofHours(antiguedadMaxima)) > 0) {
			log.info("El backup tiene m\u00e1s de {} horas. Intentando actualizaci\u00f3n desde EPL", antiguedadMaxima);
			UpdateSpec tempUpdateSpec = eplCSVProcessor.updateFromEPL();
			if (tempUpdateSpec.isEmpty()) {
				log.info("La descarga no funcion\u00f3, usando backup");
			} else {
				log.info("Descargada versi\u00f3n actualizada desde EPL");
				updateSpec = tempUpdateSpec;
			}
		} else {
			log.info("Usando informaci\u00f3n almacenada en el backup");
		}
		return updateSpec;
	}
}
