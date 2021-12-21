package org.greeneyed.epl.librarian.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

import org.greeneyed.epl.librarian.services.BibliotecaService;
import org.greeneyed.epl.librarian.services.PreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@Data
@RequestMapping(value = "/librarian/preferences")
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class PreferencesAPIController {

    private static final BodyBuilder OK_BUILDER = ResponseEntity.ok();

    private final PreferencesService preferencesService;
    private final BibliotecaService bibliotecaService;

    @GetMapping(value = "/fecha_base")
    public ResponseEntity<Long> fechaBase() {
        return preferencesService.getFechaBase()
                .map(ldt -> ldt.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .map(ResponseEntity::ok)
                .orElseGet(OK_BUILDER::build);
    }

    @PostMapping(value = "/fecha_base", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> guardarFechaBase(@RequestParam(name = "fechaBase") long fechaBaseLong) {
        preferencesService
                .setFechaBase(Instant.ofEpochMilli(fechaBaseLong).atZone(ZoneId.systemDefault()).toLocalDate());
        return OK_BUILDER.build();
    }

    @PostMapping(value = "/descarte", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> descartarLibro(@RequestParam(name = "id") Integer libroId,
            @RequestParam(name = "descartado") boolean descartado) {
        preferencesService.setDescarte(libroId, descartado);
        bibliotecaService.setDescarte(libroId, descartado);
        return OK_BUILDER.build();
    }

    @PostMapping(value = "/autoresFavoritos", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> guardarAutoresFavoritos(
            @RequestParam(name = "autoresFavoritos") Set<String> autoresFavoritos,
            @RequestParam(name = "autoresNoFavoritos") Set<String> autoresNoFavoritos) {
        preferencesService.actualizarAutoresPreferidos(autoresFavoritos, autoresNoFavoritos);
        bibliotecaService.actualizaAutoresPreferidos(preferencesService.getAutoresPreferidos());
        return OK_BUILDER.body("OK");
    }

    @PostMapping(value = "/idiomasFavoritos", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> guardarIdiomasFavoritos(
            @RequestParam(name = "idiomasFavoritos") Set<String> idiomasFavoritos,
            @RequestParam(name = "idiomasNoFavoritos") Set<String> idiomasNoFavoritos) {
        preferencesService.actualizarIdiomasPreferidos(idiomasFavoritos, idiomasNoFavoritos);
        bibliotecaService.actualizaIdiomasFavoritos(preferencesService.getIdiomasPreferidos());
        return OK_BUILDER.body("OK");
    }

    @PostMapping(value = "/generosFavoritos", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> guardarGenerosFavoritos(
            @RequestParam(name = "generosFavoritos") Set<String> generosFavoritos,
            @RequestParam(name = "generosNoFavoritos") Set<String> generosNoFavoritos) {
        preferencesService.actualizarGenerosPreferidos(generosFavoritos, generosNoFavoritos);
        bibliotecaService.actualizaGenerosFavoritos(preferencesService.getGenerosPreferidos());
        return OK_BUILDER.body("OK");
    }
}