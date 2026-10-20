package com.egon.exception;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.extern.slf4j.Slf4j;

/** Toda resposta de erro da API sai como {@link ErroResponse}, para o cliente tratar sempre pelo código. */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Campo vazio pede "Informe o CPF.", e não a regra de formato que também falhou por estar vazio. */
    private static final Set<String> OBRIGATORIEDADE = Set.of("NotBlank", "NotNull");

    /** Valor que nem converteu ("abc" num número) vem antes: dizer que o campo está vazio seria falso. */
    private static final String CONVERSAO = "typeMismatch";

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErroResponse> tratarApi(ApiException e) {
        Map<String, String> campos = e.getCampo() == null ? Map.of() : Map.of(e.getCampo(), e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(new ErroResponse(e.getCodigo(), e.getMessage(), campos));
    }

    /**
     * Um aviso por campo, o primeiro: a tela tem espaço para uma linha embaixo de cada campo.
     * O validador não garante a ordem das violações, então a de obrigatoriedade vem antes.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErroResponse tratarValidacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparingInt(ApiExceptionHandler::prioridade))
                .forEach(erro -> campos.putIfAbsent(erro.getField(), mensagem(erro)));
        return new ErroResponse("DADOS_INVALIDOS", "Confira os campos destacados.", campos);
    }

    /**
     * Num formulário multipart, "abc" no custo nem chega à validação: falha na conversão para
     * número, e a mensagem padrão do Spring é técnica demais para a tela.
     */
    private static String mensagem(FieldError erro) {
        return CONVERSAO.equals(erro.getCode()) ? "Valor em formato inválido." : erro.getDefaultMessage();
    }

    private static int prioridade(FieldError erro) {
        if (CONVERSAO.equals(erro.getCode())) {
            return 0;
        }
        return OBRIGATORIEDADE.contains(erro.getCode()) ? 1 : 2;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    ErroResponse tratarArquivoGrande() {
        String mensagem = "A foto passa de 5 MB. Use uma imagem menor.";
        return new ErroResponse("ARQUIVO_GRANDE_DEMAIS", mensagem, Map.of("foto", mensagem));
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

    /**
     * O resto: erros do próprio Spring MVC (rota inexistente, método ou tipo de conteúdo não
     * suportado), que já trazem o status certo, e falhas inesperadas, que viram 500. Sem isto
     * eles sairiam no formato padrão do Spring, sem {@code codigo}.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> tratarOutros(Exception e) {
        if (e instanceof ErrorResponse erro) {
            HttpStatusCode status = erro.getStatusCode();
            return ResponseEntity.status(status).headers(erro.getHeaders()).body(paraStatus(status));
        }
        log.error("Erro não tratado", e);
        return ResponseEntity.internalServerError()
                .body(new ErroResponse("ERRO_INTERNO", "Algo deu errado no servidor. Tente de novo."));
    }

    private static ErroResponse paraStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> new ErroResponse("RECURSO_NAO_ENCONTRADO", "Este endereço não existe na API.");
            case 405 -> new ErroResponse("METODO_NAO_PERMITIDO", "Este endereço não aceita este método.");
            case 415 -> new ErroResponse("TIPO_NAO_SUPORTADO", "Envie o corpo da requisição como JSON.");
            default -> new ErroResponse("REQUISICAO_INVALIDA", "A requisição não pôde ser atendida.");
        };
    }

}
