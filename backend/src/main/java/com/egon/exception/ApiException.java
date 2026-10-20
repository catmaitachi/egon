package com.egon.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Erro de regra de negócio com código estável, para o cliente tratar pelo código e não
 * pelo texto. O {@code campo}, quando há, diz qual campo do formulário causou o erro, para
 * a tela mostrar o aviso ao lado dele.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;
    private final String campo;

    public ApiException(HttpStatus status, String codigo, String campo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
        this.campo = campo;
    }

    public static ApiException alunoNaoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "ALUNO_NAO_ENCONTRADO", null,
                "Aluno não encontrado.");
    }

    public static ApiException empresaNaoEncontrada() {
        return new ApiException(HttpStatus.NOT_FOUND, "EMPRESA_NAO_ENCONTRADA", null,
                "Empresa não encontrada.");
    }

    public static ApiException cpfEmUso() {
        return new ApiException(HttpStatus.CONFLICT, "CPF_EM_USO", "cpf",
                "Já existe um aluno cadastrado com este CPF.");
    }

    public static ApiException cnpjEmUso() {
        return new ApiException(HttpStatus.CONFLICT, "CNPJ_EM_USO", "cnpj",
                "Já existe uma empresa cadastrada com este CNPJ.");
    }

    public static ApiException emailEmUso() {
        return new ApiException(HttpStatus.CONFLICT, "EMAIL_EM_USO", "email",
                "Este e-mail já está cadastrado.");
    }

    public static ApiException loginEmUso() {
        return new ApiException(HttpStatus.CONFLICT, "LOGIN_EM_USO", "login",
                "Este login já está em uso. Escolha outro.");
    }

    public static ApiException instituicaoNaoEncontrada() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INSTITUICAO_NAO_ENCONTRADA", "cnpjInstituicao",
                "Escolha uma instituição da lista.");
    }

    public static ApiException cursoNaoEncontrado() {
        return new ApiException(HttpStatus.BAD_REQUEST, "CURSO_NAO_ENCONTRADO", "idCurso",
                "Escolha um curso da lista.");
    }

}
