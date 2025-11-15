package org.greeneyed.epl.librarian.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

public class PreferencesServiceTest {

  @InjectMocks
  private PreferencesService preferencesService;

  @Mock
  Environment environment;

  @BeforeEach
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    preferencesService = new PreferencesService();
    preferencesService.setEnvironment(environment);
    Mockito.reset(environment);
  }

  @Test
  public void testPreferencesFileLocation() throws IOException {
    when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(Boolean.FALSE);
    File userHomepreferencesFile = PreferencesService.findPreferencesFile(false, environment);
    File userHome = new File(System.getProperty("user.home"));

    assertThat("El fichero de preferencias no se creará en el $home del usuario.", userHomepreferencesFile.getAbsolutePath(),
        startsWith(userHome.getAbsolutePath()));

    File currentDirPreferencesFile = PreferencesService.findPreferencesFile(true, environment);
    File currentDir = new File(System.getProperty("user.dir"));
    assertThat("El fichero de preferencias no se creará en el directorio actual.", currentDirPreferencesFile.getAbsolutePath(),
        startsWith(currentDir.getAbsolutePath()));

  }

  @Test
  public void comprobarPreferenciasSeCarganAdecuadamente() {
    final String idiomas = "ca,es,en";
    preferencesService.getPreferences()
        .setProperty(PreferencesService.IDIOMAS_PREFERIDOS_KEY, idiomas);
    assertThat("Empezamos sin idiomas preferidos", preferencesService.getIdiomasPreferidos()
        .size(), equalTo(0));
    preferencesService.cargarPreferencias();
    assertThat("Recuperamos tantos idiomas como hemos metido", preferencesService.getIdiomasPreferidos()
        .size(), equalTo(idiomas.split(",").length));
    //
    final String autores = "juand,pedro,paco,miguel";
    preferencesService.getPreferences()
        .setProperty(PreferencesService.AUTORES_PREFERIDOS_KEY, autores);
    assertThat("Empezamos sin autores preferidos", preferencesService.getAutoresPreferidos()
        .size(), equalTo(0));
    preferencesService.cargarPreferencias();
    assertThat("Recuperamos tantos autores como hemos metido", preferencesService.getAutoresPreferidos()
        .size(), equalTo(autores.split(",").length));
    //
    final String generos = "ficcion,novela";
    preferencesService.getPreferences()
        .setProperty(PreferencesService.GENEROS_PREFERIDOS_KEY, generos);
    assertThat("Empezamos sin generos preferidos", preferencesService.getGenerosPreferidos()
        .size(), equalTo(0));
    preferencesService.cargarPreferencias();
    assertThat("Recuperamos tantos generos como hemos metido", preferencesService.getGenerosPreferidos()
        .size(), equalTo(generos.split(",").length));
    //
    final String descartados = "0,1,2,3,4,5,6";
    preferencesService.getPreferences()
        .setProperty(PreferencesService.DESCARTADOS_OCULTOS_KEY, descartados);
    assertThat("Empezamos sin libros descartados", preferencesService.getDescartadosOcultos()
        .size(), equalTo(0));
    preferencesService.cargarPreferencias();
    assertThat("Recuperamos tantos libros descartados como hemos metido", preferencesService.getDescartadosOcultos()
        .size(), equalTo(descartados.split(",").length));
  }
}
