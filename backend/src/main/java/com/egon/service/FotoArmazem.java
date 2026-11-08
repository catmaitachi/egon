package com.egon.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.egon.exception.ApiException;

/**
 * Fotos das vantagens numa pasta em disco (o modelo prevê pasta local, não serviço externo).
 *
 * <p>O tipo vem dos primeiros bytes do arquivo, não do nome nem do Content-Type, que quem envia
 * escolhe. O nome gravado é gerado aqui, então nada do que vem de fora vira caminho.
 *
 * <p>O disco segue a transação: arquivo novo de uma transação desfeita é apagado, e o antigo só sai
 * depois do commit, para o banco nunca apontar para uma foto que não existe.
 */
@Component
public class FotoArmazem {

    static final long LIMITE = 2 * 1024 * 1024;

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private final Path pasta;

    FotoArmazem(@Value("${egon.fotos.pasta}") Path pasta) throws IOException {
        this.pasta = pasta;
        Files.createDirectories(pasta);
    }

    public String gravar(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw ApiException.fotoObrigatoria();
        }
        if (foto.getSize() > LIMITE) {
            throw ApiException.fotoGrande();
        }
        try {
            byte[] bytes = foto.getBytes();
            String nome = UUID.randomUUID() + extensao(bytes);
            Files.write(pasta.resolve(nome), bytes);
            aoTerminar(TransactionSynchronization.STATUS_ROLLED_BACK, nome);
            return nome;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Só depois do commit; sem transação, na hora. */
    public void apagar(String nome) {
        aoTerminar(TransactionSynchronization.STATUS_COMMITTED, nome);
    }

    public Resource abrir(String nome) {
        return new FileSystemResource(pasta.resolve(nome));
    }

    public MediaType tipo(String nome) {
        return nome.endsWith(".png") ? MediaType.IMAGE_PNG
                : nome.endsWith(".webp") ? MediaType.parseMediaType("image/webp")
                : MediaType.IMAGE_JPEG;
    }

    private static String extensao(byte[] bytes) {
        if (comeca(bytes, JPEG)) {
            return ".jpg";
        }
        if (comeca(bytes, PNG)) {
            return ".png";
        }
        if (bytes.length >= 12 && new String(bytes, 0, 4).equals("RIFF") && new String(bytes, 8, 4).equals("WEBP")) {
            return ".webp";
        }
        throw ApiException.fotoInvalida();
    }

    private static boolean comeca(byte[] bytes, byte[] assinatura) {
        return bytes.length >= assinatura.length
                && Arrays.equals(bytes, 0, assinatura.length, assinatura, 0, assinatura.length);
    }

    private void aoTerminar(int status, String nome) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            if (status == TransactionSynchronization.STATUS_COMMITTED) {
                apagarAgora(nome);
            }
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int resultado) {
                if (resultado == status) {
                    apagarAgora(nome);
                }
            }
        });
    }

    private void apagarAgora(String nome) {
        try {
            Files.deleteIfExists(pasta.resolve(nome));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
