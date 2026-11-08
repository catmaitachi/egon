package com.egon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.egon.dto.VantagemCatalogoResponse;
import com.egon.dto.VantagemRequest;
import com.egon.dto.VantagemResponse;
import com.egon.exception.ApiException;
import com.egon.model.Empresa;
import com.egon.model.Vantagem;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.EmpresaRepository;
import com.egon.repository.TrocaRepository;
import com.egon.repository.VantagemRepository;

import lombok.RequiredArgsConstructor;

/** HU08: a empresa cadastra, edita e exclui as vantagens dela; os alunos veem o catálogo (HU09). */
@Service
@RequiredArgsConstructor
public class VantagemService {

    private final VantagemRepository vantagens;
    private final EmpresaRepository empresas;
    private final CarteiraRepository carteiras;
    private final FotoArmazem fotos;
    private final TrocaRepository trocas;

    /** Com quantos alunos já têm saldo total para cada uma: uma consulta só para a lista inteira. */
    @Transactional(readOnly = true)
    public List<VantagemResponse> listar(UUID empresa) {
        List<Long> saldos = carteiras.saldosPorAluno();
        return vantagens.findByEmpresaIdOrderByNomeAsc(empresa).stream()
                .map(v -> VantagemResponse.de(v, saldos.stream().filter(s -> s >= v.getCusto()).count()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VantagemCatalogoResponse> catalogo() {
        return vantagens.findAllByOrderByCustoAscNomeAsc().stream().map(VantagemCatalogoResponse::de).toList();
    }

    @Transactional
    public VantagemResponse cadastrar(UUID empresa, VantagemRequest requisicao, MultipartFile foto) {
        Empresa dona = empresas.findById(empresa).orElseThrow(ApiException::empresaNaoEncontrada);
        Vantagem vantagem = new Vantagem();
        vantagem.setEmpresa(dona);
        preencher(vantagem, requisicao);
        vantagem.setFoto(fotos.gravar(foto));
        return resposta(vantagens.save(vantagem));
    }

    /** Sem foto nova, a atual fica; com foto nova, a antiga sai depois do commit. */
    @Transactional
    public VantagemResponse atualizar(UUID empresa, UUID id, VantagemRequest requisicao, MultipartFile foto) {
        Vantagem vantagem = encontrar(empresa, id);
        preencher(vantagem, requisicao);
        if (foto != null && !foto.isEmpty()) {
            String antiga = vantagem.getFoto();
            vantagem.setFoto(fotos.gravar(foto));
            fotos.apagar(antiga);
        }
        return resposta(vantagem);
    }

    /** Vantagem já resgatada fica: os cupons dos alunos apontam para ela. */
    @Transactional
    public void excluir(UUID empresa, UUID id) {
        Vantagem vantagem = encontrar(empresa, id);
        if (trocas.existsByVantagemId(id)) {
            throw ApiException.vantagemComResgates();
        }
        vantagens.delete(vantagem);
        fotos.apagar(vantagem.getFoto());
    }

    /** Ao excluir a empresa: as vantagens e as fotos vão junto. Com algum resgate, nada sai. */
    @Transactional
    public void excluirTodas(UUID empresa) {
        if (trocas.existsByVantagemEmpresaId(empresa)) {
            throw ApiException.empresaComResgates();
        }
        List<Vantagem> dela = vantagens.findByEmpresaIdOrderByNomeAsc(empresa);
        vantagens.deleteAll(dela);
        dela.forEach(v -> fotos.apagar(v.getFoto()));
    }

    @Transactional(readOnly = true)
    public Vantagem buscar(UUID id) {
        return vantagens.findById(id).orElseThrow(ApiException::vantagemNaoEncontrada);
    }

    private VantagemResponse resposta(Vantagem vantagem) {
        long alunos = carteiras.saldosPorAluno().stream().filter(s -> s >= vantagem.getCusto()).count();
        return VantagemResponse.de(vantagem, alunos);
    }

    private static void preencher(Vantagem vantagem, VantagemRequest requisicao) {
        vantagem.setNome(requisicao.nome());
        vantagem.setDescricao(requisicao.descricao());
        vantagem.setCusto(requisicao.custo());
        vantagem.setTipo(requisicao.tipo());
    }

    /** A vantagem de outra empresa não é achada pelo caminho desta. */
    private Vantagem encontrar(UUID empresa, UUID id) {
        return vantagens.findById(id)
                .filter(v -> v.getEmpresa().getId().equals(empresa))
                .orElseThrow(ApiException::vantagemNaoEncontrada);
    }

}
