package org.greeneyed.epl.librarian;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.greeneyed.epl.librarian.services.PreferencesService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@ComponentScan(basePackageClasses = Application.class)
@Slf4j
public class Application {

  public static void main(String[] args) {
    // Si estamos en docker y no tenemos mapeado el directorio de preferencias y
    // temporales, abort
    if (inDocker()) {
      File preferencesDirectory = PreferencesService.getDockerBasePath()
          .toFile();
      if (!preferencesDirectory.exists() || !preferencesDirectory.isDirectory() || !preferencesDirectory.canWrite()) {
        log.error(
            """
                \n*******************************************************
                \nPara poder ejecutar en docker es necesario mapear el directorio /librarian al directorio donde se deseen a almacenar la preferencias y los ficheros temporales.
                \n*******************************************************
                """);
        System.exit(-1);
      }
    }
    SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
    builder.headless(false)
        .run(args);
  }

  @Bean
  static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
    propsConfig.setLocation(new ClassPathResource("git.properties"));
    propsConfig.setIgnoreResourceNotFound(true);
    propsConfig.setIgnoreUnresolvablePlaceholders(true);
    return propsConfig;
  }

  private static boolean inDocker() {
    return StringUtils.contains(System.getProperty("spring.profiles.active"), "docker");
  }
}