package com.egon.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.egon.auth.Perfil;
import com.egon.auth.UsuarioLogado;
import com.egon.dto.VantagemForm;
import com.egon.dto.VantagemResponse;
import com.egon.exception.ApiException;
import com.egon.model.Empresa;
import com.egon.model.Vantagem;
import com.egon.repository.EmpresaRepository;
import com.egon.repository.TrocaRepository;
import com.egon.repository.VantagemRepository;
import com.egon.validation.ImagemValidator;

/**
 * Vantagens das empresas parceiras (HU08) e o catálogo que os alunos consultam (HU09).
 * As fotos ficam em arquivos na pasta egon.fotos.pasta; o banco guarda só o nome do arquivo.
 */
@Service
public class VantagemService {

    private final VantagemRepository vantagens;
    private final EmpresaRepository empresas;
    private final TrocaRepository trocas;
    private final Path pasta;

    public VantagemService(VantagemRepository vantagens, EmpresaRepository empresas, TrocaRepository trocas,
            @Value("${egon.fotos.pasta}") String pasta) throws IOException {
        this.vantagens = vantagens;
        this.empresas = empresas;
        this.trocas = trocas;
        this.pasta = Files.createDirectories(Path.of(pasta)).toAbsolutePath().normalize();
    }

    public record Foto(Resource arquivo, MediaType tipo) {
    }

    @Transactional(readOnly = true)
    public List<VantagemResponse> listar(String cnpjEmpresa) {
        List<Vantagem> lista = cnpjEmpresa == null || cnpjEmpresa.isBlank()
                ? vantagens.findAllByOrderByCustoAscNomeAsc()
                : vantagens.findByEmpresaCnpjOrderByNomeAsc(cnpjEmpresa);
        return lista.stream().map(VantagemResponse::de).toList();
    }

    @Transactional
    public VantagemResponse cadastrar(UsuarioLogado usuario, VantagemForm form) {
        Empresa empresa = empresas.findById(usuario.exigir(Perfil.EMPRESA)).orElseThrow(ApiException::naoAutenticado);
        Vantagem vantagem = new Vantagem();
        vantagem.setNome(form.nome());
        vantagem.setDescricao(form.descricao());
        vantagem.setCusto(form.custo());
        vantagem.setTipo(form.tipo());
        vantagem.setEmpresa(empresa);
        vantagem.setFoto(salvarFoto(form.foto()));
        return VantagemResponse.de(vantagens.save(vantagem));
    }

    @Transactional(readOnly = true)
    public Foto foto(Integer id) {
        Vantagem vantagem = vantagens.findById(id).orElseThrow(ApiException::vantagemNaoEncontrada);
        Path arquivo = pasta.resolve(vantagem.getFoto());
        if (!Files.isReadable(arquivo)) {
            throw ApiException.vantagemNaoEncontrada();
        }
        return new Foto(new FileSystemResource(arquivo),
                MediaTypeFactory.getMediaType(arquivo.toString()).orElse(MediaType.APPLICATION_OCTET_STREAM));
    }

    /** Só a dona remove, e só enquanto ninguém a resgatou: os cupons emitidos apontam para ela. */
    @Transactional
    public void remover(UsuarioLogado usuario, Integer id) {
        String cnpj = usuario.exigir(Perfil.EMPRESA);
        Vantagem vantagem = vantagens.findById(id)
                .filter(v -> v.getEmpresa().getCnpj().equals(cnpj))
                .orElseThrow(ApiException::vantagemNaoEncontrada);
        if (trocas.existsByVantagemId(id)) {
            throw ApiException.vantagemComTrocas();
        }
        vantagens.delete(vantagem);
        apagarFotoNoCommit(vantagem.getFoto());
    }

    /** Para a exclusão da empresa, que já conferiu que nenhuma vantagem dela foi resgatada. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void removerTodasDa(String cnpjEmpresa) {
        List<Vantagem> daEmpresa = vantagens.findByEmpresaCnpj(cnpjEmpresa);
        vantagens.deleteAll(daEmpresa);
        daEmpresa.forEach(v -> apagarFotoNoCommit(v.getFoto()));
    }

    @Transactional(readOnly = true)
    public Optional<Vantagem> buscar(Integer id) {
        return vantagens.findById(id);
    }

    /** Grava o arquivo e o apaga se a transação for desfeita, para não sobrar foto sem vantagem. */
    private String salvarFoto(MultipartFile foto) {
        String nome = UUID.randomUUID() + "." + ImagemValidator.formato(foto).orElseThrow();
        Path destino = pasta.resolve(nome);
        try {
            foto.transferTo(destino);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar a foto", e);
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    apagar(destino);
                }
            }
        });
        return nome;
    }

    private void apagarFotoNoCommit(String nome) {
        Path arquivo = pasta.resolve(nome);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                apagar(arquivo);
            }
        });
    }

    private static void apagar(Path arquivo) {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            // Uma foto órfã na pasta não atrapalha nada; não vale derrubar a requisição por ela.
        }
    }

}
