package com.egon.vantagem;

import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoAluno;
import static com.egon.Logins.comoEmpresa;
import static com.egon.Logins.comoProfessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.egon.Dados;
import com.egon.dto.TransacaoRequest;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.service.AlunoService;
import com.egon.service.EmpresaService;
import com.egon.service.TransacaoService;
import com.jayway.jsonpath.JsonPath;

/**
 * HU08: a empresa cadastra vantagens com foto. Sem {@code @Transactional}: o arquivo da foto só é
 * apagado depois do commit, e numa transação desfeita isso nunca aconteceria. O que o teste grava
 * é apagado no fim.
 */
@SpringBootTest
@AutoConfigureMockMvc
class VantagemTest {

    @TempDir
    static Path pasta;

    @DynamicPropertySource
    static void fotos(DynamicPropertyRegistry registro) {
        registro.add("egon.fotos.pasta", () -> pasta.toString());
    }

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};

    @Autowired MockMvc mvc;
    @Autowired EmpresaService empresas;
    @Autowired AlunoService alunos;
    @Autowired TransacaoService transacoes;
    @Autowired TransacaoRepository transacaoRepository;
    @Autowired CarteiraRepository carteiras;
    @Autowired ProfessorRepository professores;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired CursoRepository cursos;

    private UUID livraria;
    private UUID papelaria;
    private UUID ana;
    private int saldoHelena;

    @BeforeEach
    void cadastros() throws Exception {
        livraria = Dados.cadastrarEmpresa(mvc, "Livraria Central", "45218073000192", "livraria");
        papelaria = Dados.cadastrarEmpresa(mvc, "Papelaria", "28039156000103", "papelaria");
        saldoHelena = professores.findByCpf(Dados.HELENA).orElseThrow().getSaldo();
    }

    @AfterEach
    void limpar() {
        empresas.excluir(livraria);
        empresas.excluir(papelaria);
        if (ana != null) {
            transacaoRepository.deleteAll(transacaoRepository.findByAlunoIdOrderByDataDesc(ana));
            carteiras.deleteAll(carteiras.findByAlunoIdOrderByProfessorNomeAsc(ana));
            alunos.excluir(ana);
            var helena = professores.findByCpf(Dados.HELENA).orElseThrow();
            helena.setSaldo(saldoHelena);
            professores.save(helena);
        }
    }

    private MockMultipartHttpServletRequestBuilder vantagem(HttpMethod metodo, String caminho, String custo, String tipo,
            byte[] foto) {
        MockMultipartHttpServletRequestBuilder requisicao = multipart(metodo, caminho);
        if (foto != null) {
            requisicao.file(new MockMultipartFile("foto", "foto.png", "image/png", foto));
        }
        requisicao.param("nome", "Livro da bibliografia")
                .param("descricao", "Um livro da bibliografia do curso, à escolha.")
                .param("custo", custo)
                .param("tipo", tipo);
        return requisicao;
    }

    private ResultActions cadastrar(byte[] foto) throws Exception {
        return mvc.perform(vantagem(HttpMethod.POST, "/empresas/" + livraria + "/vantagens", "300", "PRODUTO", foto)
                .with(comoEmpresa(livraria)));
    }

    private UUID criar() throws Exception {
        return Dados.id(cadastrar(PNG).andExpect(status().isCreated()).andReturn());
    }

    private String arquivo(UUID vantagem) throws Exception {
        String corpo = mvc.perform(get("/empresas/" + livraria + "/vantagens").with(comoEmpresa(livraria)))
                .andReturn().getResponse().getContentAsString();
        String url = JsonPath.<java.util.List<String>>read(corpo, "$[?(@.id == '" + vantagem + "')].fotoUrl").getFirst();
        return url;
    }

    private long arquivosNaPasta() throws Exception {
        try (var lista = Files.list(pasta)) {
            return lista.count();
        }
    }

    @Test
    @DisplayName("cadastra a vantagem com foto e serve a foto com o tipo certo")
    void cadastra() throws Exception {
        UUID id = Dados.id(cadastrar(PNG)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Livro da bibliografia"))
                .andExpect(jsonPath("$.custo").value(300))
                .andExpect(jsonPath("$.tipo").value("PRODUTO"))
                .andExpect(jsonPath("$.fotoUrl").exists())
                .andExpect(jsonPath("$.alunosComSaldo").value(0))
                .andReturn());

        mvc.perform(get("/vantagens/" + id + "/foto").with(comoAluno(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/vantagens/" + id + "/foto").with(comoEmpresa(livraria)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes(PNG));
        assertThat(arquivosNaPasta()).isEqualTo(1);
    }

    @Test
    @DisplayName("recusa foto que não é imagem (bytes de texto com nome .png), grande demais ou ausente")
    void recusaFoto() throws Exception {
        cadastrar("isto não é uma imagem".getBytes())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FOTO_INVALIDA"))
                .andExpect(jsonPath("$.campos.foto").exists());
        byte[] grande = Arrays.copyOf(PNG, 2 * 1024 * 1024 + 1);
        cadastrar(grande)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FOTO_GRANDE"))
                .andExpect(jsonPath("$.campos.foto").exists());
        cadastrar(null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.foto").exists());
        assertThat(arquivosNaPasta()).isZero();
    }

    @Test
    @DisplayName("recusa custo zero ou negativo, tipo fora da lista e campos em branco")
    void recusaCampos() throws Exception {
        for (String custo : new String[] {"0", "-1", "abc"}) {
            mvc.perform(vantagem(HttpMethod.POST, "/empresas/" + livraria + "/vantagens", custo, "PRODUTO", PNG)
                            .with(comoEmpresa(livraria)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.custo").exists());
        }
        mvc.perform(vantagem(HttpMethod.POST, "/empresas/" + livraria + "/vantagens", "10", "XYZ", PNG)
                        .with(comoEmpresa(livraria)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tipo").exists());
        mvc.perform(multipart("/empresas/" + livraria + "/vantagens")
                        .file(new MockMultipartFile("foto", "f.png", "image/png", PNG))
                        .with(comoEmpresa(livraria)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.descricao").exists());
    }

    @Test
    @DisplayName("editar sem foto mantém o arquivo; com foto nova, troca e apaga o antigo")
    void edita() throws Exception {
        UUID id = criar();
        String antes = arquivo(id);

        mvc.perform(vantagem(HttpMethod.PUT, "/empresas/" + livraria + "/vantagens/" + id, "250", "DESCONTO", null)
                        .with(comoEmpresa(livraria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custo").value(250))
                .andExpect(jsonPath("$.tipo").value("DESCONTO"));
        mvc.perform(get("/vantagens/" + id + "/foto").with(comoEmpresa(livraria))).andExpect(content().bytes(PNG));
        assertThat(arquivo(id)).isEqualTo(antes);

        mvc.perform(vantagem(HttpMethod.PUT, "/empresas/" + livraria + "/vantagens/" + id, "250", "DESCONTO", JPEG)
                        .with(comoEmpresa(livraria)))
                .andExpect(status().isOk());
        mvc.perform(get("/vantagens/" + id + "/foto").with(comoEmpresa(livraria)))
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(JPEG));
        assertThat(arquivosNaPasta()).isEqualTo(1);
    }

    @Test
    @DisplayName("excluir a vantagem apaga o arquivo; excluir a empresa apaga as vantagens e as fotos")
    void exclui() throws Exception {
        UUID id = criar();
        criar();
        assertThat(arquivosNaPasta()).isEqualTo(2);

        mvc.perform(delete("/empresas/" + livraria + "/vantagens/" + id).with(comoEmpresa(livraria)))
                .andExpect(status().isNoContent());
        assertThat(arquivosNaPasta()).isEqualTo(1);

        mvc.perform(get("/empresas/" + livraria + "/vantagens").with(comoEmpresa(livraria)))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(delete("/empresas/" + livraria).with(comoAdmin())).andExpect(status().isNoContent());
        livraria = Dados.cadastrarEmpresa(mvc, "Livraria Central", "45218073000192", "livraria");
        assertThat(arquivosNaPasta()).isZero();
    }

    @Test
    @DisplayName("só a própria empresa mexe nas vantagens dela; o admin vê e exclui; a vantagem de outra empresa não é achada")
    void acesso() throws Exception {
        UUID id = criar();

        mvc.perform(vantagem(HttpMethod.POST, "/empresas/" + livraria + "/vantagens", "10", "PRODUTO", PNG)
                .with(comoEmpresa(papelaria))).andExpect(status().isForbidden());
        mvc.perform(get("/empresas/" + livraria + "/vantagens").with(comoEmpresa(papelaria)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/empresas/" + livraria + "/vantagens").with(comoAdmin())).andExpect(status().isOk());
        mvc.perform(delete("/empresas/" + papelaria + "/vantagens/" + id).with(comoEmpresa(papelaria)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("VANTAGEM_NAO_ENCONTRADA"));
        mvc.perform(delete("/empresas/" + livraria + "/vantagens/" + id).with(comoAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("catálogo: aluno e admin veem todas as vantagens com a empresa; quantos alunos podem pagar conta só quem tem saldo")
    void catalogoEAlunosComSaldo() throws Exception {
        criar();
        UUID horizonte = instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
        ana = alunos.cadastrar(new com.egon.dto.AlunoCadastroRequest("Ana Souza", "ana@email.com", Dados.ANA, "1",
                "Rua A", horizonte, cursos.findAllByOrderByNomeAsc().getFirst().getId(), "ana", "segredo123")).id();
        UUID helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        transacoes.enviar(helena, new TransacaoRequest(ana, 300, "Monitoria"));

        mvc.perform(get("/empresas/" + livraria + "/vantagens").with(comoEmpresa(livraria)))
                .andExpect(jsonPath("$[0].alunosComSaldo").value(1));
        mvc.perform(get("/vantagens").with(comoAluno(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].empresa.nome").value("Livraria Central"))
                .andExpect(jsonPath("$[0].custo").value(300));
        mvc.perform(get("/vantagens").with(comoProfessor(helena))).andExpect(status().isForbidden());
        mvc.perform(post("/vantagens").with(comoAluno(ana)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

}
