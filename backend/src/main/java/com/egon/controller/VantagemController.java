package com.egon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.egon.dto.VantagemCatalogoResponse;
import com.egon.dto.VantagemRequest;
import com.egon.dto.VantagemResponse;
import com.egon.model.Vantagem;
import com.egon.service.FotoArmazem;
import com.egon.service.VantagemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** HU08. Cadastro e edição em multipart: os campos e a foto no mesmo envio. */
@RestController
@RequiredArgsConstructor
public class VantagemController {

    private final VantagemService vantagens;
    private final FotoArmazem fotos;

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPRESA') and #id == principal.id)")
    @GetMapping("/empresas/{id}/vantagens")
    List<VantagemResponse> listar(@PathVariable UUID id) {
        return vantagens.listar(id);
    }

    @PreAuthorize("hasRole('EMPRESA') and #id == principal.id")
    @PostMapping(path = "/empresas/{id}/vantagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    VantagemResponse cadastrar(@PathVariable UUID id, @Valid @ModelAttribute VantagemRequest requisicao,
            @RequestPart(name = "foto", required = false) MultipartFile foto) {
        return vantagens.cadastrar(id, requisicao, foto);
    }

    @PreAuthorize("hasRole('EMPRESA') and #id == principal.id")
    @PutMapping(path = "/empresas/{id}/vantagens/{vantagem}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    VantagemResponse atualizar(@PathVariable UUID id, @PathVariable UUID vantagem,
            @Valid @ModelAttribute VantagemRequest requisicao,
            @RequestPart(name = "foto", required = false) MultipartFile foto) {
        return vantagens.atualizar(id, vantagem, requisicao, foto);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPRESA') and #id == principal.id)")
    @DeleteMapping("/empresas/{id}/vantagens/{vantagem}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable UUID id, @PathVariable UUID vantagem) {
        vantagens.excluir(id, vantagem);
    }

    /** O catálogo (HU09): o aluno vê o que existe e o que já cabe no saldo; o admin acompanha. */
    @PreAuthorize("hasAnyRole('ALUNO', 'ADMIN')")
    @GetMapping("/vantagens")
    List<VantagemCatalogoResponse> catalogo() {
        return vantagens.catalogo();
    }

    @GetMapping("/vantagens/{vantagem}/foto")
    ResponseEntity<Resource> foto(@PathVariable UUID vantagem) {
        Vantagem encontrada = vantagens.buscar(vantagem);
        return ResponseEntity.ok().contentType(fotos.tipo(encontrada.getFoto())).body(fotos.abrir(encontrada.getFoto()));
    }

}
