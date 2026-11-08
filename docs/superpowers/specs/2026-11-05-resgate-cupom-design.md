# Resgate de vantagem e cupom (HU09, HU10)

Status: aprovado na conversa (o usuário escolheu o prompt reescrito). Descartável.

## Pedido

O aluno resgata uma vantagem do catálogo; o resgate gera um cupom com código aleatório e QR code; a empresa digita o código (ou abre o link do QR), vê o resgate e confirma o uso.

## Decisões

- **Débito por carteira:** maior saldo primeiro (empate pelo nome do professor), gravando quanto saiu de cada carteira (`TROCA_DEBITO`). Precisa de saldo total ≥ custo.
- **Tabelas:** `TROCA` (id, data, valor, aluno, vantagem), `TROCA_DEBITO` (troca, professor, valor), `CUPOM` (código PK, troca, status `EMITIDO|USADO`, data de uso).
- **Código:** 8 caracteres do alfabeto Crockford base32 por `SecureRandom` (40 bits), no formato `EGON-XXXX-XXXX`. Na busca, aceita minúsculas, espaços e sem hífen; O vira 0, I e L viram 1.
- **QR:** gerado no front com `qrcode-generator` (MIT, sem dependências; dependência nova). Conteúdo: o link `<origem>/painel?cupom=<código>`; a empresa logada que abre esse link cai na confirmação com o código preenchido.
- **Endpoints:** `POST /alunos/{id}/resgates` `{ vantagemId }` (o próprio aluno); `GET /alunos/{id}/cupons`; `GET /empresas/{id}/cupons` (resgates das vantagens dela); `GET /empresas/{id}/cupons/{codigo}`; `POST /empresas/{id}/cupons/{codigo}/confirmar` (a própria empresa). Cupom de outra empresa responde 404, como se não existisse.
- **Erros:** `SALDO_INSUFICIENTE` (422), `VANTAGEM_NAO_ENCONTRADA` (404), `CUPOM_NAO_ENCONTRADO` (404), `CUPOM_USADO` (409).
- **Concorrência:** o resgate trava os professores das carteiras do aluno em ordem de id e depois as carteiras (mesma ordem professor → carteira das outras operações); confirmar trava o cupom.
- **Avisos ao vivo:** tipo `CUPOM` com `evento` `EMITIDO` (aluno e empresa) e `USADO` (aluno); a empresa passa a poder conectar no WebSocket.
- **Extrato do aluno:** movimento `RESGATE` (saída), com a vantagem, a empresa e o código do cupom.
- **Conta dos Egos:** `emitidos = comProfessores + comAlunos + reservados + resgatados`.
- **Exclusões:** vantagem ou empresa com resgates não sai (409 `VANTAGEM_COM_RESGATES`, `EMPRESA_COM_RESGATES`).

## Fora

Expiração e cancelamento de cupom, leitura do QR pela câmera dentro do app, tabela `NOTIFICACAO` (os avisos são ao vivo; o histórico está nas listas de cupons).
