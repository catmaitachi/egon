package com.egon.exception;

import java.util.Arrays;
import java.util.Locale;

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

    /** HU01: o aviso não diz se o erro foi no login ou na senha. */
    public static ApiException credenciaisInvalidas() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", null,
                "Login ou senha incorretos.");
    }

    public static ApiException naoAutenticado() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", null,
                "Entre com seu login para continuar.");
    }

    public static ApiException acessoNegado() {
        return new ApiException(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", null,
                "Seu perfil não tem acesso a esta função.");
    }

    public static ApiException alunoDestinatarioNaoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "ALUNO_NAO_ENCONTRADO", "cpfAluno",
                "Aluno não encontrado.");
    }

    public static ApiException alunoDeOutraInstituicao() {
        return new ApiException(HttpStatus.BAD_REQUEST, "ALUNO_DE_OUTRA_INSTITUICAO", "cpfAluno",
                "Você só pode reconhecer alunos da sua instituição.");
    }

    /** HU06 e HU09. Os números saem como na tela: 1.000, e não 1000. */
    public static ApiException saldoInsuficiente(String campo, String mensagem, int... valores) {
        Object[] formatados = Arrays.stream(valores).mapToObj(v -> String.format(Locale.of("pt", "BR"), "%,d", v)).toArray();
        return new ApiException(HttpStatus.CONFLICT, "SALDO_INSUFICIENTE", campo, mensagem.formatted(formatados));
    }

    public static ApiException vantagemNaoEncontrada() {
        return new ApiException(HttpStatus.NOT_FOUND, "VANTAGEM_NAO_ENCONTRADA", null,
                "Vantagem não encontrada.");
    }

    public static ApiException vantagemComTrocas() {
        return new ApiException(HttpStatus.CONFLICT, "VANTAGEM_COM_TROCAS", null,
                "Esta vantagem já foi resgatada e não pode ser removida: os cupons emitidos dependem dela.");
    }

    public static ApiException alunoComHistorico() {
        return new ApiException(HttpStatus.CONFLICT, "ALUNO_COM_HISTORICO", null,
                "Este aluno já recebeu moedas ou fez trocas, e o histórico não pode ser apagado.");
    }

    public static ApiException empresaComResgates() {
        return new ApiException(HttpStatus.CONFLICT, "EMPRESA_COM_RESGATES", null,
                "Esta empresa tem vantagens já resgatadas, e os cupons emitidos dependem dela.");
    }

}
