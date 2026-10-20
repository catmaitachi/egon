package com.egon.service;

/** E-mail registrado, à espera do commit para sair. */
public record EmailPendente(String para, String assunto, String corpo) {
}
