package com.egon.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErroResponse> tratarApi(ApiException e) {
        Map<String, String> campos = e.getCampo() == null ? Map.of() : Map.of(e.getCampo(), e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(new ErroResponse(e.getCodigo(), e.getMessage(), campos));
    }

    /** Um aviso por campo, o primeiro: a tela tem espaço para uma linha embaixo de cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErroResponse tratarValidacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return new ErroResponse("DADOS_INVALIDOS", "Confira os campos destacados.", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErroResponse tratarCorpoIlegivel() {
        return new ErroResponse("CORPO_INVALIDO", "O corpo da requisição não é um JSON válido.");
    }

    /**
     * Rede de segurança para a corrida entre a checagem de duplicidade no serviço e o
     * INSERT: dois cadastros iguais ao mesmo tempo passam pela checagem, e o banco barra o
     * segundo.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErroResponse tratarIntegridade() {
        return new ErroResponse("CONFLITO", "Os dados conflitam com um cadastro existente.");
    }

}
