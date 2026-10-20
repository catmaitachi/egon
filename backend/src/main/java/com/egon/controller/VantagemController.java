package com.egon.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.egon.auth.UsuarioLogado;
import com.egon.dto.VantagemForm;
import com.egon.dto.VantagemResponse;
import com.egon.service.VantagemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vantagens")
@RequiredArgsConstructor
public class VantagemController {

    private final VantagemService vantagens;

    /** HU09: o catálogo, do custo menor para o maior. Com {@code cnpjEmpresa}, só as daquela empresa. */
    @GetMapping
    List<VantagemResponse> listar(@RequestParam(required = false) String cnpjEmpresa) {
        return vantagens.listar(cnpjEmpresa);
    }

    /** HU08: multipart, com os campos e o arquivo da foto. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    VantagemResponse cadastrar(UsuarioLogado usuario, @Valid @ModelAttribute VantagemForm form) {
        return vantagens.cadastrar(usuario, form);
    }

    /** Pública: a tag {@code <img>} não manda o token. */
    @GetMapping("/{id}/foto")
    ResponseEntity<Resource> foto(@PathVariable Integer id) {
        VantagemService.Foto foto = vantagens.foto(id);
        return ResponseEntity.ok()
                .contentType(foto.tipo())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                .body(foto.arquivo());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remover(UsuarioLogado usuario, @PathVariable Integer id) {
        vantagens.remover(usuario, id);
    }

}
