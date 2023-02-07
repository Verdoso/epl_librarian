package org.greeneyed.epl.librarian;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Mientras Jetty no soporte Jakarta 6.0, debemos usar NONE en vez de MOCK
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class ApplicationIT {

  @Test
  public void contextLoads() {
  }

}
