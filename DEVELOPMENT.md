# Trucos para el desarrollo de la versión nativa

## Para actualizar los descriptores necesarios para generar la imagen
Para actualizar los descriptores hace falta ejecutar la aplicación con Graal VM y configurar el agente para que escriba los descritores en el sitio donde el plugin de la generación del nativo los encontrará.
Así pues, lo primero es generar un .jar del nativo ejecutable y una vez hecho, acceder al directorio target y, suponiendo que GRAAL_HOME contiene el path a la versión de Graal instalada, ejecutar algo asi como (cambiando la versión correspondiente al jar):

`$epl-librarian\target>%GRAAL_HOME%\bin\java -agentlib:native-image-agent=config-output-dir=../src/main/resources/META-INF/native-image/ -jar epl_librarian-1.17.10-SNAPSHOT.jar`


## Para generar la versión nativa
Para generar el nativo necesitamos una consola de tipo "x64 Native Tools Command Prompt" (no basta un simple "command prompt"). Establecemos como JDK a GralVM a través de JAVA_HOME y luego podemos ejecutar los comandos maven para empaquetar la aplicación, por si acaso no lo está antes, y luego generar el nativo. Por ejemplo:

```
set JAVA_HOME=%GRAAL_HOME%
mvn -Pdeployment -DskipTests -Dassembly.skipAssembly=true package
mvn -DskipTests -Dassembly.skipAssembly=true -Pnative native:compile
```

## Referencias

* https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html
* https://www.graalvm.org/22.2/reference-manual/native-image/guides/build-spring-boot-app-into-native-executable/
* https://developer.okta.com/blog/2022/04/22/github-actions-graalvm#the-environmental-impact-of-graalvm-builds
* https://www.infoq.com/articles/native-java-spring-boot/
* https://blogs.oracle.com/java/post/go-native-with-spring-boot-3-and-graalvm
