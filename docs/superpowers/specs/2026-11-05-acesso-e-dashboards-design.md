# Rodada 2: login, admin, ids, CRUDs, vantagens, moeda por professor e dashboards em bento

Status: aprovado na conversa ("suas recomendações podem ser uma base por hora, implemente"). Arquivo de trabalho, descartável como o REVISAO.md.

## Intenção

Hoje qualquer pessoa abre qualquer painel e a gestão, porque não há login, e o CPF de todo mundo aparece nas URLs. A rodada fecha isso: login para os quatro perfis (aluno, professor, empresa e um admin novo), ids próprios em vez de CPF/CNPJ, CRUD completo do que cada perfil possui, vantagens da empresa (HU08), Egos que guardam de qual professor vieram e podem ser repassados entre alunos sob as regras desse professor, e dashboards em bento em que cada bloco responde a uma pergunta real de quem olha.

## Metas

1. Login único (HU01) para aluno, professor, empresa e admin; cada tela e endpoint aberto só ao perfil e à pessoa certos.
2. Id próprio (UUID) como chave de cada tabela de pessoa, instituição e vantagem; CPF e CNPJ viram colunas únicas e saem das URLs.
3. Admin (operação do Egon, vê tudo) com CRUD de instituições, cursos, professores, alunos, empresas e admins. Faz o pré-cadastro da HU04.
4. Empresa com CRUD das próprias vantagens (HU08, com edição). Aluno e empresa editam o próprio cadastro.
5. Moeda por professor: cada Ego guarda o professor de origem; o aluno tem uma carteira por professor.
6. Transferência de Egos entre alunos, sob regras que cada professor configura para os Egos dele, com aprovação do professor (desligável).
7. Dashboards em bento para os quatro perfis, só com dados reais. DESIGN.md com as regras do bento.
8. Testes, documentação e REVISAO.md atualizados.

## Fora do escopo

Resgate e cupom (HU09, HU10), troca e recuperação de senha por e-mail, PROFESSOR_CURSO, admin por instituição, professor editando o próprio cadastro (é do admin), regras de carência, de repasse em cadeia e por turma.

## Não pode acontecer

- Rota ou endpoint sem proteção, ou alguém vendo dados de outra pessoa trocando o id.
- Ego criado ou perdido: a soma tem de fechar sempre (invariante abaixo).
- O mesmo Ego gasto duas vezes enquanto uma transferência espera aprovação.
- Edição ou exclusão de transação, cota ou transferência decidida (é o histórico que sustenta os saldos).
- Número inventado ou bloco decorativo nos dashboards; bento de cards iguais ou painel dentro de painel.
- Professor ou aluno recebendo e-mail, endereço ou RG de terceiros para escolher destinatário.
- A landing mudar (o link "Entrar" já aponta para /entrar).

## Abordagens consideradas

| | Abordagem | Troca | Decisão |
| :-: | :--- | :--- | :--- |
| Login A | Spring Security, sessão no servidor (cookie HttpOnly), regras por perfil | Dependência nova da família Spring (já no repositório local do Maven); testes precisam de usuário logado; CSRF configurado | **Escolhida**: tudo nasce fechado, padrão testado, sessão integra com o WebSocket |
| Login B | Sessão feita à mão | Sem dependência, mas fixação de sessão, CSRF e rotas esquecidas por nossa conta | Descartada |
| Login C | JWT no navegador | Dependência, token exposto a XSS e na URL do WebSocket | Descartada |
| Ids A | UUID como chave primária; CPF/CNPJ únicos; relações pelo id | Mexe nas chaves de quase tudo; banco de dev recriado; testes e diagrama relacional mudam | **Escolhida** pelo usuário: CPF sai também das relações internas |
| Ids B | UUID público ao lado do CPF como chave | Menor mudança, dois identificadores por tabela | Descartada |

Bento em CSS Grid nativo (Tailwind v4), sem biblioteca de grid nem de gráfico.

## Decisões

### 1. Ids e banco

- Chave `id` UUID gerada em `instituicao`, `aluno`, `professor`, `empresa`, `admin`, `vantagem`, `carteira`, `transferencia` (a `transacao` já é UUID). `curso` mantém o id inteiro; `cota_semestral` mantém o id gerado.
- CPF (aluno, professor) e CNPJ (empresa, instituição) viram colunas `UNIQUE NOT NULL`. As chaves estrangeiras passam a ser `*_id`.
- `Usuario` deixa de precisar do truque do `Persistable` (era para chave atribuída); a unicidade de CPF/CNPJ/e-mail/login continua conferida antes e garantida pelo banco.
- O banco de dev muda de arquivo (`./data/egon2`) porque o `ddl-auto=update` não troca chave primária: o arquivo antigo fica intocado e a primeira subida cria tudo do zero com a carga inicial.

### 2. Autenticação (HU01)

- Spring Security com sessão HTTP; cookie HttpOnly, SameSite=Lax; troca do id de sessão no login (padrão).
- `POST /auth/login` `{ login, senha }` → 200 `{ perfil: "ALUNO"|"PROFESSOR"|"EMPRESA"|"ADMIN", id, nome }`. Login inexistente ou senha errada → 401 `CREDENCIAIS_INVALIDAS`, mesma mensagem.
- `GET /auth/eu` → a sessão atual, ou 401; sempre entrega o cookie de CSRF. `POST /auth/logout` → 204.
- CSRF: cookie `XSRF-TOKEN` legível, devolvido no cabeçalho `X-XSRF-TOKEN` em POST/PUT/DELETE (inclusive no login).
- Sem sessão → 401 `NAO_AUTENTICADO`; perfil ou pessoa errados → 403 `ACESSO_NEGADO`; formato `{ codigo, mensagem, campos }`.
- Pessoa excluída enquanto logada: a próxima requisição dá 401.

### 3. Admin e CRUDs

- `Admin extends Usuario` (tabela `admin`: id, nome, login UK, senha). Um admin na carga inicial: login `admin`, senha de `egon.admin.senha` (padrão de dev `admin12345`).
- Admin faz CRUD de: instituições (CNPJ válido e único; excluir com alunos ou professores → 409 `INSTITUICAO_EM_USO`), cursos (nome único; excluir com alunos → 409 `CURSO_EM_USO`), professores (CPF válido e único, login único entre perfis, senha 8–72 bytes; **ao ser criado recebe na hora a cota do semestre**; excluir com transações → 409 `PROFESSOR_COM_TRANSACOES`, sem transações leva as cotas junto), alunos (regras atuais; excluir com transação ou transferência → 409 `ALUNO_COM_TRANSACOES`), empresas (excluir apaga vantagens e fotos), admins (não exclui a si mesmo nem o último → 409 `ADMIN_PROTEGIDO`).
- Aluno e empresa editam o próprio cadastro (CPF/CNPJ não mudam). Professor é editado pelo admin.
- Transações, cotas e transferências decididas não têm edição nem exclusão.

### 4. Moeda por professor e carteiras

- Tabela `carteira` (id, aluno_id, professor_id, saldo ≥ 0; única por aluno+professor). Criada no primeiro crédito.
- O saldo do aluno deixa de ser coluna: é a soma das carteiras dele. A API mostra o total e as carteiras `[{ professor: {id, nome}, saldo }]`.
- Envio do professor (HU06) credita a carteira aluno+professor.
- **Invariante:** Egos emitidos por cota = saldo dos professores + soma das carteiras + valor das transferências pendentes.
- Concorrência: toda operação sobre Egos de um professor trava antes a linha desse professor (PESSIMISTIC_WRITE) e depois as carteiras, sempre nessa ordem; isso serializa por professor e não forma ciclo.

### 5. Transferência entre alunos

- Tabela `transferencia` (id, data, valor > 0, motivo obrigatório até 500, remetente_id, destinatario_id, professor_id = origem dos Egos, status `PENDENTE|APROVADA|RECUSADA|CANCELADA`, data_decisao).
- `POST /alunos/{id}/transferencias` `{ destinatarioId, professorId, valor, motivo }`. O aluno escolhe de qual carteira sai.
- Regras **fixas**: destinatário existe, é diferente do remetente e é da instituição do professor de origem; valor inteiro > 0 que caiba na carteira; motivo obrigatório.
- Regras **do professor** (`GET/PUT /professores/{id}/regras`, embutidas na tabela professor):
  - `permiteRepasse` (padrão ligado),
  - `exigeAprovacao` (padrão ligado),
  - `valorMaximo` (opcional),
  - `limiteMensal` por aluno (opcional; soma das transferências PENDENTE e APROVADA do remetente com Egos desse professor no mês, fuso America/Sao_Paulo),
  - `mesmoCurso` (padrão desligado: quando ligado, destinatário do mesmo curso do remetente).
- Cada regra violada tem código próprio: `REPASSE_DESATIVADO`, `ACIMA_DO_MAXIMO`, `LIMITE_MENSAL`, `CURSO_DIFERENTE`, `DESTINATARIO_INVALIDO`, `SALDO_INSUFICIENTE` (campo do formulário correspondente).
- Sem aprovação: a transferência nasce `APROVADA`, debita a carteira do remetente e credita a do destinatário (mesmo professor de origem) na mesma transação.
- Com aprovação: nasce `PENDENTE` e o valor sai da carteira do remetente (reserva). `POST /transferencias/{id}/aprovar` (professor de origem) credita o destinatário; `…/recusar` (professor de origem) e `…/cancelar` (remetente) devolvem ao remetente. Só pendente pode ser decidida → senão 409 `TRANSFERENCIA_DECIDIDA`.
- As regras valem no momento do pedido; mudar a regra não mexe nas pendentes, que continuam esperando a decisão.
- `GET /alunos/{id}/colegas`: só `{ id, nome, curso }` dos alunos da mesma instituição (para escolher o destinatário). `GET /professores/{id}/transferencias`: as transferências com os Egos dele (pendentes primeiro).

### 6. Quem acessa o quê

| Endpoint | Público | Aluno | Professor | Empresa | Admin |
| :--- | :-: | :-: | :-: | :-: | :-: |
| `/auth/*` | sim | sim | sim | sim | sim |
| `POST /alunos`, `POST /empresas` (cadastro) | sim | | | | sim |
| `GET /instituicoes`, `GET /cursos` | sim | sim | sim | sim | sim |
| `POST/PUT/DELETE /instituicoes…`, `/cursos…`, `/admins…`, `GET /admins` | | | | | sim |
| `GET /alunos`, `DELETE /alunos/{id}` | | | | | sim |
| `GET/PUT /alunos/{id}`, `…/extrato` | | o próprio | | | sim |
| `GET …/carteiras`, `…/colegas`, `POST …/transferencias` | | o próprio | | | |
| `GET/POST /professores`, `PUT/DELETE /professores/{id}` | | | | | sim |
| `GET /professores/{id}`, `…/extrato` | | | o próprio | | sim |
| `POST …/transacoes`, `…/alunos`, `GET/PUT …/regras`, `…/transferencias` | | | o próprio | | |
| `POST /transferencias/{id}/aprovar`, `…/recusar` | | | professor de origem | | |
| `POST /transferencias/{id}/cancelar` | | remetente | | | |
| `GET /empresas`, `DELETE /empresas/{id}` | | | | | sim |
| `GET/PUT /empresas/{id}` | | | | a própria | sim |
| `GET/POST/PUT/DELETE /empresas/{id}/vantagens…` | | | | a própria | GET e DELETE |
| `GET /vantagens` | | sim | | | sim |
| `GET /vantagens/{id}/foto` | | sim | sim | sim | sim |
| `GET /admin/resumo` | | | | | sim |
| WebSocket `/ws/notificacoes` | | sim | sim | | |

### 7. Vantagens (HU08)

- Tabela `vantagem`: id, nome (até 100), descrição (até 1.000), foto (nome do arquivo), custo (inteiro > 0), tipo `DESCONTO|PRODUTO|SERVICO`, empresa_id.
- POST e PUT em multipart. Foto obrigatória no cadastro e opcional na edição (sem foto nova, mantém); JPEG, PNG ou WebP conferidos pelos bytes; até 2 MB; gravada em `egon.fotos.pasta` (padrão `data/fotos/`) com nome UUID. Trocar ou excluir apaga o arquivo antigo.
- `GET /vantagens/{id}/foto` com o tipo certo. `GET /empresas/{id}/vantagens` traz `alunosComSaldo` por vantagem (quantos alunos têm saldo total ≥ custo; só a contagem). `GET /vantagens` é o catálogo.

### 8. WebSocket

- O handshake pega a pessoa da sessão; somem os parâmetros `perfil` e `cpf`. Só aluno e professor conectam; o resto → 403. Origem continua conferida.
- Mensagens: as de hoje (`MOEDAS_RECEBIDAS`, `ENVIO_CONFIRMADO`, com ids no lugar de CPF) e `TRANSFERENCIA` com `{ evento, transferencia, saldo }`, `evento` em `SOLICITADA` (professor de origem e remetente), `APROVADA` (remetente e destinatário), `RECUSADA` (remetente), `CANCELADA` (professor de origem). Sempre depois do commit. Os textos ficam em templates do `content.ts`, um por perfil e evento.

### 9. Dashboards (bento), rota `/painel`

Cada bloco responde a uma pergunta; tamanho pela importância; celular em uma coluna na ordem abaixo.

- **Aluno:** quanto eu tenho (total com a moeda, o único bloco com acento, e as carteiras por professor); o que chegou por último, de quem e por quê; repassar Egos (formulário: colega, carteira, valor, motivo, mostrando as regras daquele professor); minhas transferências (pendentes com cancelar, e as decididas); o que já dá para trocar (catálogo com custo ≤ total, sem resgatar).
- **Professor:** quanto ainda posso enviar; enviar agora; pendências de aprovação (aprovar/recusar); quanto da cota do semestre já distribuí; regras dos meus Egos; para quem já enviei (extrato).
- **Empresa:** minhas vantagens (foto, tipo, custo, alunos com saldo; editar e excluir); cadastrar vantagem; meus dados.
- **Admin:** quantos Egos existem e onde estão (emitidos = professores + carteiras + reservados, igualdade à vista); envios por mês nos últimos 6 meses; por instituição (alunos, professores, Egos enviados); quem está no sistema (contagens que levam à gestão); atalhos de cadastro.

`GET /admin/resumo` → `{ egos: { emitidos, comProfessores, comAlunos, reservados }, enviosPorMes: [{ mes, quantidade, valor }], porInstituicao: [{ id, nome, alunos, professores, enviado }], contagens: { instituicoes, cursos, alunos, professores, empresas, vantagens } }`.

Gestão do admin em `/admin/instituicoes`, `/admin/cursos`, `/admin/professores`, `/admin/alunos`, `/admin/empresas`, `/admin/admins`. Saem `/aluno/:cpf`, `/professor/:cpf`, `/alunos`, `/empresas`.

### 10. Front: sessão e rotas

- `/entrar` é o login dos quatro perfis; depois `/painel` (ou a página do `?volta=`). Cadastros públicos terminam no `/entrar`.
- Rotas internas conferem `GET /auth/eu`: sem sessão → `/entrar`; perfil errado → `/painel`. 401 no meio do uso → `/entrar` com aviso de sessão expirada.
- Cabeçalho interno: nome, perfil, "Sair" (e "Meus dados" para aluno e empresa).
- Cliente da API com `credentials: 'same-origin'` e o cabeçalho de CSRF.

### 11. DESIGN.md: regras do bento

12 colunas a partir de 1024 px, 6 no tablet, 1 no celular; um painel por bloco, nunca aninhado; tamanhos pela importância, nunca todos iguais; no máximo um bloco com o acento; quatro estados por bloco; `minmax(0,1fr)` e container queries.

### 12. Documentação

Histórias: ator Admin; HU01 com quatro perfis; HU04 (admin cadastra); HU08; história nova de transferência entre alunos com regras e aprovação; matriz. Modelo de Dados: divergência 1 revertida em parte (moeda por professor via carteira e transferência entre alunos voltam, com outra forma) e novas linhas (ids UUID, admin, cota imediata, exclusões, vantagem com edição, dashboard do aluno sem resgate), esquema e "O que já está implementado". README e REVISAO.md.

## Testes

- Login de cada perfil; mesma resposta para login inexistente e senha errada; logout; `/auth/eu`.
- Matriz de acesso: anônimo → 401, perfil errado ou outra pessoa → 403, certo → 2xx, para cada linha da tabela.
- CSRF: escrita sem cabeçalho → 403.
- CRUDs do admin com as regras e os 409; autoedição de aluno e empresa; cota imediata do professor novo.
- Carteiras: envio credita a carteira certa; total do aluno = soma das carteiras.
- Transferências: cada regra fixa e configurável com o código certo; sem aprovação conclui na hora; com aprovação reserva, aprova, recusa, cancela; decisão por quem não pode → 403; decidir duas vezes → 409; concorrência (pedidos simultâneos não estouram a carteira; aprovar e cancelar ao mesmo tempo resolve um só).
- Vantagens: CRUD, foto com tipo falso, acima de 2 MB, troca de foto apaga a antiga, `alunosComSaldo`.
- Resumo do admin e **invariante** depois de cotas, envios e transferências em todos os estados.
- WebSocket: sessão obrigatória, perfis permitidos, eventos de transferência para as pessoas certas, nada antes do commit.
- Os testes atuais adaptados (ids e sessão).
- Front: build, lint, roteiro no Chrome headless para os quatro perfis a 375 e 1440 px, `impeccable audit` e `critique`.

## Chamadas minhas

- Professor criado recebe a cota do semestre na hora.
- Excluir empresa apaga vantagens e fotos.
- Senha do admin em dev `admin12345`.
- Banco de dev em arquivo novo (`data/egon2`).
- Professor não edita o próprio cadastro; configura só as regras.
- Reserva dos Egos durante a aprovação; aluno pode cancelar pendente.
- Regras de repasse: as cinco configuráveis e as fixas da seção 5, como base inicial.
