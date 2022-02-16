package org.greeneyed.epl.librarian.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import java.io.File;

import org.junit.jupiter.api.Test;

public class PreferencesServiceTest {

  private PreferencesService preferencesService = new PreferencesService();

  @Test
  public void testPreferencesFileLocation() {
    File userHomepreferencesFile = PreferencesService.findPreferencesFile(false);
    File userHome = new File(System.getProperty("user.home"));

    assertThat("El fichero de preferencias no se creará en el $home del usuario.", userHomepreferencesFile.getAbsolutePath(),
        startsWith(userHome.getAbsolutePath()));

    File currentDirPreferencesFile = PreferencesService.findPreferencesFile(true);
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
        .setProperty(PreferencesService.LIBROS_DESCARTADOS_KEY, descartados);
    assertThat("Empezamos sin libros descartados", preferencesService.getLibrosDescartados()
        .size(), equalTo(0));
    preferencesService.cargarPreferencias();
    assertThat("Recuperamos tantos libros descartados como hemos metido", preferencesService.getLibrosDescartados()
        .size(), equalTo(descartados.split(",").length));
  }
}
