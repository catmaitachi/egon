package com.egon.exception;

import java.util.Arrays;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.exc.MismatchedInputException;

/** Toda resposta de erro da API sai como {@link ErroResponse}, para o cliente tratar sempre pelo código. */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Campo vazio pede "Informe o CPF.", e não a regra de formato que também falhou por estar vazio. */
    private static final Set<String> OBRIGATORIEDADE = Set.of("NotBlank", "NotNull");

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
                .sorted(Comparator.comparing((FieldError erro) -> !OBRIGATORIEDADE.contains(erro.getCode())))
                .forEach(erro -> campos.putIfAbsent(erro.getField(), mensagem(erro)));
        return new ErroResponse("DADOS_INVALIDOS", "Confira os campos destacados.", campos);
    }

    /** Valor que nem converte para o tipo do campo (formulário multipart): aviso legível, não o do Spring. */
    private static String mensagem(FieldError erro) {
        if (!erro.isBindingFailure()) {
            return erro.getDefaultMessage();
        }
        return Arrays.stream(erro.getCodes()).anyMatch(c -> c.endsWith("java.lang.Integer"))
                ? "Use um número inteiro." : "Escolha uma opção da lista.";
    }

    /** Arquivo acima do limite do multipart: barrado antes de chegar ao controller. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErroResponse tratarArquivoGrande() {
        return new ErroResponse("FOTO_GRANDE", "A foto pode ter até 2 MB.", Map.of("foto", "A foto pode ter até 2 MB."));
    }

    /**
     * JSON válido com um campo do tipo errado (1.5 ou "abc" onde vai inteiro) é aviso nesse campo.
     * JSON quebrado ou número fora do limite não apontam um campo e ficam como corpo inválido.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErroResponse tratarCorpoIlegivel(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof MismatchedInputException erro && !erro.getPath().isEmpty()) {
            String mensagem = erro.getTargetType() == Integer.class ? "Use um número inteiro." : "Formato inválido.";
            return new ErroResponse("DADOS_INVALIDOS", "Confira os campos destacados.",
                    Map.of(erro.getPath().getFirst().getPropertyName(), mensagem));
        }
        return new ErroResponse("CORPO_INVALIDO", "O corpo da requisição não é um JSON válido.");
    }

    /** Regra de acesso de um endpoint ({@code @PreAuthorize}) que não deixou passar. */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErroResponse tratarAcessoNegado() {
        return new ErroResponse("ACESSO_NEGADO", "Você não tem acesso a isto.");
    }

    /** Um id que nem é UUID não pode existir: o caminho não aponta para nada. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErroResponse tratarIdInvalido() {
        return paraStatus(HttpStatus.NOT_FOUND);
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
