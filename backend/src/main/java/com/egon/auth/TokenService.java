package com.egon.auth;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Token de sessão assinado: {@code <perfil:id:expiração>.<HMAC-SHA256>}, os dois em Base64 URL.
 *
 * <p>Quem tem o token prova o login sem o servidor guardar sessão; quem não tem a chave não
 * consegue forjar nem alterar um. A expiração limita o estrago de um token vazado.
 */
@Service
public class TokenService {

    private static final String ALGORITMO = "HmacSHA256";
    private static final Base64.Encoder CODIFICADOR = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODIFICADOR = Base64.getUrlDecoder();

    private final SecretKeySpec chave;
    private final Duration validade;
    private final Clock relogio;

    public TokenService(@Value("${egon.token.segredo}") String segredo,
            @Value("${egon.token.validade}") Duration validade, Clock relogio) {
        this.chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), ALGORITMO);
        this.validade = validade;
        this.relogio = relogio;
    }

    public String emitir(UsuarioLogado usuario) {
        long expiraEm = relogio.instant().plus(validade).getEpochSecond();
        String conteudo = CODIFICADOR.encodeToString(
                (usuario.perfil() + ":" + usuario.id() + ":" + expiraEm).getBytes(StandardCharsets.UTF_8));
        return conteudo + "." + CODIFICADOR.encodeToString(assinar(conteudo));
    }

    /** Vazio para token malformado, adulterado ou vencido. */
    public Optional<UsuarioLogado> ler(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 2
                    || !MessageDigest.isEqual(assinar(partes[0]), DECODIFICADOR.decode(partes[1]))) {
                return Optional.empty();
            }
            String[] campos = new String(DECODIFICADOR.decode(partes[0]), StandardCharsets.UTF_8).split(":");
            if (campos.length != 3 || Long.parseLong(campos[2]) < relogio.instant().getEpochSecond()) {
                return Optional.empty();
            }
            return Optional.of(new UsuarioLogado(Perfil.valueOf(campos[0]), campos[1]));
        } catch (IllegalArgumentException e) {
            // Base64 inválido, número inválido ou perfil desconhecido.
            return Optional.empty();
        }
    }

    private byte[] assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(chave);
            return mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 indisponível", e);
        }
    }

}
