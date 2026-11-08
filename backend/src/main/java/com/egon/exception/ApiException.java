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

    public static ApiException credenciaisInvalidas() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", null,
                "Login ou senha inválidos.");
    }

    public static ApiException naoAutenticado() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", null,
                "Entre com seu login para continuar.");
    }

    public static ApiException alunoNaoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "ALUNO_NAO_ENCONTRADO", null,
                "Aluno não encontrado.");
    }

    public static ApiException professorNaoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "PROFESSOR_NAO_ENCONTRADO", null,
                "Professor não encontrado.");
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

    /** No envio o aluno é um campo do formulário, não o recurso do caminho: 400, e não 404. */
    public static ApiException alunoNaoEncontradoNoEnvio() {
        return new ApiException(HttpStatus.BAD_REQUEST, "ALUNO_NAO_ENCONTRADO", "idAluno",
                "Aluno não encontrado.");
    }

    public static ApiException alunoDeOutraInstituicao() {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "ALUNO_DE_OUTRA_INSTITUICAO", "idAluno",
                "Este aluno é de outra instituição. Você só pode enviar para alunos da sua.");
    }

    public static ApiException saldoInsuficiente(int saldo) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "SALDO_INSUFICIENTE", "valor",
                "Saldo insuficiente. Você tem " + saldo + " disponíveis.");
    }

    public static ApiException alunoComTransacoes() {
        return new ApiException(HttpStatus.CONFLICT, "ALUNO_COM_TRANSACOES", null,
                "Este aluno já recebeu moedas e não pode ser excluído: o histórico dos professores depende dele.");
    }

    public static ApiException instituicaoNaoEncontrada() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INSTITUICAO_NAO_ENCONTRADA", "idInstituicao",
                "Escolha uma instituição da lista.");
    }

    public static ApiException cursoNaoEncontrado() {
        return new ApiException(HttpStatus.BAD_REQUEST, "CURSO_NAO_ENCONTRADO", "idCurso",
                "Escolha um curso da lista.");
    }

    public static ApiException cnpjDeInstituicaoEmUso() {
        return new ApiException(HttpStatus.CONFLICT, "CNPJ_EM_USO", "cnpj",
                "Já existe uma instituição com este CNPJ.");
    }

    public static ApiException instituicaoInexistente() {
        return new ApiException(HttpStatus.NOT_FOUND, "INSTITUICAO_NAO_ENCONTRADA", null,
                "Instituição não encontrada.");
    }

    public static ApiException instituicaoEmUso() {
        return new ApiException(HttpStatus.CONFLICT, "INSTITUICAO_EM_USO", null,
                "Esta instituição tem alunos ou professores e não pode ser excluída.");
    }

    public static ApiException cursoInexistente() {
        return new ApiException(HttpStatus.NOT_FOUND, "CURSO_NAO_ENCONTRADO", null,
                "Curso não encontrado.");
    }

    public static ApiException cursoRepetido() {
        return new ApiException(HttpStatus.CONFLICT, "CURSO_REPETIDO", "nome",
                "Já existe um curso com este nome.");
    }

    public static ApiException cursoEmUso() {
        return new ApiException(HttpStatus.CONFLICT, "CURSO_EM_USO", null,
                "Este curso tem alunos e não pode ser excluído.");
    }

    public static ApiException cpfDeProfessorEmUso() {
        return new ApiException(HttpStatus.CONFLICT, "CPF_EM_USO", "cpf",
                "Já existe um professor cadastrado com este CPF.");
    }

    public static ApiException professorComTransacoes() {
        return new ApiException(HttpStatus.CONFLICT, "PROFESSOR_COM_TRANSACOES", null,
                "Este professor já enviou moedas e não pode ser excluído: o histórico dos alunos depende dele.");
    }

    public static ApiException adminNaoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "ADMIN_NAO_ENCONTRADO", null,
                "Admin não encontrado.");
    }

    public static ApiException adminProtegido() {
        return new ApiException(HttpStatus.CONFLICT, "ADMIN_PROTEGIDO", null,
                "Você não pode excluir a si mesmo nem o último admin.");
    }

    public static ApiException vantagemNaoEncontrada() {
        return new ApiException(HttpStatus.NOT_FOUND, "VANTAGEM_NAO_ENCONTRADA", null,
                "Vantagem não encontrada.");
    }

    public static ApiException fotoObrigatoria() {
        return new ApiException(HttpStatus.BAD_REQUEST, "FOTO_OBRIGATORIA", "foto",
                "Escolha uma foto da vantagem.");
    }

    public static ApiException fotoInvalida() {
        return new ApiException(HttpStatus.BAD_REQUEST, "FOTO_INVALIDA", "foto",
                "Use uma imagem JPEG, PNG ou WebP.");
    }

    public static ApiException fotoGrande() {
        return new ApiException(HttpStatus.BAD_REQUEST, "FOTO_GRANDE", "foto",
                "A foto pode ter até 2 MB.");
    }

    public static ApiException transferenciaNaoEncontrada() {
        return new ApiException(HttpStatus.NOT_FOUND, "TRANSFERENCIA_NAO_ENCONTRADA", null,
                "Transferência não encontrada.");
    }

    public static ApiException transferenciaDecidida() {
        return new ApiException(HttpStatus.CONFLICT, "TRANSFERENCIA_DECIDIDA", null,
                "Esta transferência já foi decidida.");
    }

    public static ApiException destinatarioInvalido(String mensagem) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "DESTINATARIO_INVALIDO", "destinatarioId", mensagem);
    }

    public static ApiException repasseDesativado() {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "REPASSE_DESATIVADO", "professorId",
                "Este professor não permite repassar os Egos dele.");
    }

    public static ApiException acimaDoMaximo(int maximo) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "ACIMA_DO_MAXIMO", "valor",
                "Os Egos deste professor podem ser repassados até " + maximo + " por vez.");
    }

    public static ApiException limiteMensal(int limite, int usado) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "LIMITE_MENSAL", "valor",
                "Você já repassou " + usado + " de " + limite + " permitidos neste mês com os Egos deste professor.");
    }

    public static ApiException cursoDiferente() {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "CURSO_DIFERENTE", "destinatarioId",
                "Os Egos deste professor só podem ir para colegas do seu curso.");
    }

    public static ApiException saldoDaCarteiraInsuficiente(int disponivel) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "SALDO_INSUFICIENTE", "valor",
                "Você tem " + disponivel + " disponíveis deste professor.");
    }

    public static ApiException cupomNaoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "CUPOM_NAO_ENCONTRADO", "codigo",
                "Nenhum cupom com este código nas suas vantagens. Confira o código.");
    }

    public static ApiException cupomUsado() {
        return new ApiException(HttpStatus.CONFLICT, "CUPOM_USADO", "codigo",
                "Este cupom já foi usado.");
    }

    public static ApiException saldoParaResgate(int total) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "SALDO_INSUFICIENTE", null,
                "Saldo insuficiente para esta vantagem. Você tem " + total + " disponíveis.");
    }

    public static ApiException vantagemComResgates() {
        return new ApiException(HttpStatus.CONFLICT, "VANTAGEM_COM_RESGATES", null,
                "Esta vantagem já foi resgatada e não pode ser excluída: os cupons dos alunos dependem dela.");
    }

    public static ApiException empresaComResgates() {
        return new ApiException(HttpStatus.CONFLICT, "EMPRESA_COM_RESGATES", null,
                "Esta empresa tem vantagens resgatadas e não pode ser excluída: os cupons dos alunos dependem dela.");
    }

}
