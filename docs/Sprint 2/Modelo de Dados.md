# Modelo de Dados

Os diagramas das Sprints 1 e 2 divergiam entre si e das [histórias de usuário](../Sprint%201/Historias%20de%20Usuario.md) em vários pontos. Este documento registra cada divergência e a decisão tomada, e descreve o modelo reconciliado, que o código em `backend/` segue. Os diagramas foram [atualizados](#diagramas-atualizados) de acordo.

Critério usado em cada divergência: valem as histórias de usuário, que são a especificação mais completa, e entre os diagramas, a versão que as atende.

## Divergências e decisões

| # | Divergência | Onde aparece | Decisão |
| :-: | :--- | :--- | :--- |
| 1 | `Moeda` (uma por professor, com assinatura e critérios), `Carteira` (saldo do aluno por moeda) e `Transferencia` entre alunos, com aprovação (`StatusTransferencia`) | Diagrama de Classes e Diagrama Relacional | **Removidas.** Existe uma moeda só (o Ego) e o saldo é uma coluna de `ALUNO` e de `PROFESSOR`, como no MER (nas duas versões que existiram). Nenhuma história, caso de uso ou o diagrama de componentes prevê transferência entre alunos ou moeda por professor, e a `Troca` "paga com" uma única `Moeda` contradiz a HU09: o aluno precisa poder trocar tudo o que recebeu, de qualquer professor, por uma vantagem. |
| 2 | RG e endereço do aluno | Pedidos pela HU02; ausentes no Relacional e no MER, e o Diagrama de Classes não tem endereço | Incluídos em `ALUNO`. |
| 3 | Login e senha | A HU01 dá login a aluno, professor e empresa; o Relacional só os tem em `ALUNO` e `PROFESSOR`; o `Mer.drawio.png` não os tem em nenhuma entidade | `login` e `senha` em `ALUNO`, `PROFESSOR` e `EMPRESA`. Não há tabela `USUARIO`: a classe abstrata `Usuario` vira colunas em cada perfil. O login é único no sistema todo, entre os três perfis (a tela de login é uma só). A senha é guardada como hash BCrypt. |
| 4 | Saldo do aluno | O MER tem `saldo` em Aluno; o Relacional usa `CARTEIRA` | Coluna `saldo` em `ALUNO`, começando em 0 (HU02, HU11). |
| 5 | Descrição, foto e custo da vantagem | Pedidos pelas HU08/HU09 e presentes no Diagrama de Classes; ausentes no Relacional | Incluídos em `VANTAGEM`. |
| 6 | Mensagem da transação | Pedida pelas HU06, HU07, HU11 e HU12 (`motivo` no Diagrama de Classes); ausente no Relacional | Coluna `motivo` em `TRANSACAO`. |
| 7 | Identificador de transação e de troca | UUID na HU06 e no Diagrama de Classes; `INT` no Relacional | UUID. |
| 8 | Instituição do professor como atributo e como relacionamento "Trabalha em" ao mesmo tempo | MER | Só o relacionamento (chave estrangeira `cnpj_instituicao`). |
| 9 | Curso do aluno | A HU02 o lista entre os campos digitados; o Relacional tem a tabela `CURSO` | O curso é escolhido numa lista, como a instituição. |
| 10 | Registro da cota semestral | A HU05 pede que o crédito de 1.000 moedas fique no histórico para auditoria; nenhum diagrama tem onde guardá-lo | Proposta: tabela `COTA_SEMESTRAL`, uma linha por professor e semestre. A confirmar na sprint da HU05. |
| 11 | Duas versões do MER | `Entidade Relacionamento.png` era cópia, byte a byte, do `DiagramaNãoRelacional.png` que a própria equipe substituiu pelo `Mer.drawio.png` no commit `ce5164e` | A cópia antiga foi removida para não haver dois MERs contraditórios. O que ela tinha a mais (login e senha) está incorporado aqui. |

## Esquema relacional

`PK` chave primária, `FK` chave estrangeira, `UK` valor único.

```mermaid
erDiagram
    INSTITUICAO ||--o{ ALUNO : "estuda em"
    CURSO ||--o{ ALUNO : "cursa"
    INSTITUICAO ||--o{ PROFESSOR : "trabalha em"
    PROFESSOR ||--o{ PROFESSOR_CURSO : "dá aula"
    CURSO ||--o{ PROFESSOR_CURSO : "tem"
    PROFESSOR ||--o{ COTA_SEMESTRAL : "recebe"
    PROFESSOR ||--o{ TRANSACAO : "envia"
    ALUNO ||--o{ TRANSACAO : "recebe"
    EMPRESA ||--o{ VANTAGEM : "oferece"
    ALUNO ||--o{ TROCA : "faz"
    VANTAGEM ||--o{ TROCA : "é resgatada em"
    TROCA ||--|| CUPOM : "gera"
    ALUNO ||--o{ NOTIFICACAO : "recebe"
    EMPRESA ||--o{ NOTIFICACAO : "recebe"
    CUPOM ||--o{ NOTIFICACAO : "avisa"

    INSTITUICAO {
        CHAR(14) cnpj PK
        VARCHAR(100) nome
        VARCHAR(30) tipo
    }
    CURSO {
        INT id_curso PK
        VARCHAR(100) nome
    }
    ALUNO {
        CHAR(11) cpf PK
        VARCHAR(100) nome
        VARCHAR(100) email UK
        VARCHAR(20) rg
        VARCHAR(255) endereco
        VARCHAR(50) login UK "único entre aluno, professor e empresa"
        VARCHAR(255) senha "hash BCrypt"
        INT saldo "começa em 0"
        CHAR(14) cnpj_instituicao FK
        INT id_curso FK
    }
    EMPRESA {
        CHAR(14) cnpj PK "numérico ou alfanumérico"
        VARCHAR(100) nome
        VARCHAR(100) email UK
        VARCHAR(50) login UK "único entre aluno, professor e empresa"
        VARCHAR(255) senha "hash BCrypt"
    }
    PROFESSOR {
        CHAR(11) cpf PK
        VARCHAR(100) nome
        VARCHAR(80) departamento
        VARCHAR(50) login UK "único entre aluno, professor e empresa"
        VARCHAR(255) senha "hash BCrypt"
        INT saldo "acumula entre semestres"
        CHAR(14) cnpj_instituicao FK
    }
    PROFESSOR_CURSO {
        CHAR(11) cpf_professor PK, FK
        INT id_curso PK, FK
    }
    COTA_SEMESTRAL {
        INT id_cota PK
        CHAR(11) cpf_professor FK
        VARCHAR(6) semestre "ex. 2026.2, único por professor"
        INT valor "1.000"
        DATETIME data
    }
    TRANSACAO {
        UUID id_transacao PK
        DATETIME data
        INT valor "maior que 0"
        TEXT motivo "obrigatório"
        CHAR(11) cpf_professor FK
        CHAR(11) cpf_aluno FK
    }
    VANTAGEM {
        INT id_vantagem PK
        VARCHAR(100) nome
        TEXT descricao
        VARCHAR(255) foto "endereço da imagem"
        INT custo "maior que 0"
        VARCHAR(30) tipo "DESCONTO, PRODUTO ou SERVICO"
        CHAR(14) cnpj_empresa FK
    }
    TROCA {
        UUID id_troca PK
        DATETIME data
        INT valor "custo debitado"
        CHAR(11) cpf_aluno FK
        INT id_vantagem FK
    }
    CUPOM {
        VARCHAR(20) codigo PK "alfanumérico"
        UUID id_troca FK, UK
    }
    NOTIFICACAO {
        INT id_notificacao PK
        VARCHAR(255) descricao
        VARCHAR(20) codigo_cupom FK
        CHAR(11) cpf_aluno FK
        CHAR(14) cnpj_empresa FK
    }
```

## O que já está implementado

Sprint 2: as tabelas `INSTITUICAO`, `CURSO`, `ALUNO` e `EMPRESA`, com o CRUD de aluno e de empresa parceira. As demais entram com as histórias que as usam.

| Recurso | Endpoints | Observações |
| :--- | :--- | :--- |
| Instituições | `GET /instituicoes` | Só leitura: pré-cadastradas (HU04). Três fictícias são carregadas na primeira subida. |
| Cursos | `GET /cursos` | Só leitura: a lista do cadastro de aluno. |
| Alunos | `GET /alunos`, `POST /alunos`, `GET` / `PUT` / `DELETE /alunos/{cpf}` | O CPF é a chave e não muda na edição. O saldo é só leitura. |
| Empresas | `GET /empresas`, `POST /empresas`, `GET` / `PUT` / `DELETE /empresas/{cnpj}` | O CNPJ é a chave e não muda na edição. |

Regras aplicadas no cadastro e na edição:

- CPF e CNPJ com dígitos verificadores corretos, com ou sem máscara. O CNPJ alfanumérico também é aceito.
- CPF, e-mail e login de aluno não se repetem; CNPJ, e-mail e login de empresa também não. O login não se repete nem entre perfis.
- Login de 3 a 50 caracteres entre letras minúsculas, números, ponto, hífen e sublinhado. E-mail com domínio completo (`nome@dominio.com`).
- Senha de 8 a 72 bytes, gravada como hash BCrypt e nunca devolvida pela API. Na edição, senha em branco mantém a atual.
- Todo erro sai como `{ "codigo", "mensagem", "campos" }`, com `campos` ligando cada campo inválido ao seu aviso.

## Diagramas atualizados

Todas as imagens levam o arquivo do draw.io embutido: para editar, abra o PNG no [diagrams.net](https://app.diagrams.net) (Arquivo › Abrir) e, ao exportar de novo, marque a opção de incluir uma cópia do diagrama. O Diagrama de Classes, o de Componentes e o de Casos de Uso não tinham arquivo-fonte no repositório e foram redesenhados no draw.io a partir das imagens anteriores, com o mesmo conteúdo e arranjo, mais as correções abaixo.

- **[Diagrama Relacional](DiagramaRelacional.png)**: saíram `MOEDA`, `CARTEIRA` e `TRANSFERENCIA`, e `id_moeda` de `TROCA`; `ALUNO` ganhou `rg`, `endereco` e `saldo`; `EMPRESA`, `login` (UK) e `senha`; `VANTAGEM`, `descricao`, `foto` e `custo`; `TRANSACAO`, `motivo`; `id_transacao` e `id_troca` passaram a UUID; entrou `COTA_SEMESTRAL`, no lugar que era da `MOEDA`.
- **[MER](Mer.drawio.png)**: `login` e `senha` em Aluno, Professor e Empresa; `rg` e `endereco` em Aluno; `descricao`, `foto` e `custo` em Vantagens; `motivo` em Transação; a entidade Cota Semestral, que o Professor recebe; saiu o atributo Instituição de Professor, que já é o relacionamento "Trabalha em".
- **[Diagrama de Classes](../Sprint%201/Diagrama%20de%20Classes.png)**: saíram `Moeda`, `Carteira`, `Transferencia`, `StatusTransferencia` e `Aluno.solicitarTransferencia()`, e com a `Moeda` saiu a associação "paga com" de `Troca`; `Aluno` ganhou `endereco` e `saldo`; entrou a classe `CotaSemestral`, que o `Professor` recebe.
- **[Diagrama de Componentes](Diagrama%20de%20Componentes.png)**: entraram as listas do cadastro de aluno (`/instituicoes` e `/cursos`, com `InstituicaoController`, `CursoController`, seus services e o `CursoRepository`) e, no front-end, as telas de gestão de alunos e de empresas.
- **[Casos de Uso](../Sprint%201/Casos%20de%20Uso.png)**: "Realizar cadstro de empresa" corrigido para "Realizar cadastro de empresa".
