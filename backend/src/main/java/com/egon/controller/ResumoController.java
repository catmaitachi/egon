package com.egon.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.ResumoResponse;
import com.egon.service.ResumoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ResumoController {

    private final ResumoService resumos;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/resumo")
    ResumoResponse resumo() {
        return resumos.resumo();
    }

}
