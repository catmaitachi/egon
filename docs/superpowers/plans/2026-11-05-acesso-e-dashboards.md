# Rodada 2: login, ids, CRUDs, vantagens, moeda por professor e bento — Plano de implementação

> Execução: nativa, na sessão principal, **sem subagentes nem workflows** (pedido do usuário). Passos com `- [ ]`. **Sem commits**: o usuário versiona; onde o modelo do plano teria "Commit", aqui é "parar e conferir o diff".

**Goal:** login por perfil, ids UUID, CRUDs, HU08, carteiras por professor com transferência entre alunos sob regras e aprovação, e dashboards em bento.

**Architecture:** Spring Security com sessão e `@PreAuthorize` por perfil e dono; entidades com chave UUID; carteira aluno×professor como fonte do saldo do aluno; toda operação sobre Egos de um professor trava a linha dele antes das carteiras; avisos por WebSocket após o commit. Front React com sessão vinda de `/auth/eu`, rota `/painel` por perfil e grade bento em CSS Grid.

**Tech Stack:** Java 21, Spring Boot 4.1 (webmvc, data-jpa, validation, websocket, **security**), H2, Lombok; React 19, Vite 8, Tailwind 4, Motion, Phosphor.

**Spec:** `docs/superpowers/specs/2026-11-05-acesso-e-dashboards-design.md`

## Global Constraints

- `export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home` antes de `./mvnw` (Java 27 quebra o Lombok).
- Dependências novas: só `spring-boot-starter-security` e `spring-boot-starter-security-test` (4.1.0, já no `~/.m2`). Nada novo no `package.json`.
- Erros sempre `{ codigo, mensagem, campos }`; mensagens em português no tom das atuais; nome da moeda só em `frontend/src/content.ts`.
- Banco de dev: `jdbc:h2:file:./data/egon2`. Não tocar em `backend/data/egon.mv.db` nem no PID 90418.
- Visualização do usuário: API 8080 (PID em `scratchpad/preview/api.pid`) e vite 5180; reiniciar no fim com o código novo.
- Landing intocada (App.tsx, sections/, ui.tsx, Coin, WavesStage, PatternWaves, HeroIndex).
- Sem commit, stash, reset ou checkout.

## Review Focus

1. **Token de CSRF trocado no login**: o Spring gira o token ao autenticar; o front precisa reler o cookie depois do login, senão o primeiro POST logado dá 403. Teste no Task 3 (`postDepoisDoLoginUsaTokenNovo`) e o cliente do Task 9 relê o cookie a cada escrita.
2. **Conta excluída ou com login trocado enquanto logada**: a sessão antiga não pode continuar valendo. Teste no Task 3 (`contaExcluidaPerdeASessao`).
3. **Login com maiúsculas ou espaço** ("Helena.Duarte "): entra, como o cadastro guarda minúsculo e aparado. Teste no Task 3 (`loginIgnoraCaixaEEspacos`).
4. **Regra mudada com transferência pendente**: a pendente continua decidível e não é revalidada. Teste no Task 7 (`mudarRegraNaoMexeNasPendentes`).
5. **Professor trocado de instituição pelo admin**: as carteiras continuam com os Egos dele; o repasse passa a exigir destinatário da instituição atual dele. Teste no Task 7 (`destinoSegueInstituicaoAtualDoProfessor`).

---

### Task 1: Ids UUID em todas as tabelas de pessoa e instituição

**Files:**
- Modify: `backend/src/main/java/com/egon/model/{Usuario,Instituicao,Aluno,Professor,Empresa,Transacao,CotaSemestral}.java`, `repository/*.java`, `dto/*Response.java`, `dto/AlunoCadastroRequest.java` (`idInstituicao` no lugar de `cnpjInstituicao`), `service/*.java`, `controller/*.java`, `notificacao/*`, `config/DadosIniciais.java`, `resources/application.properties` (`egon2`)
- Test: todos os de `backend/src/test` passam a usar ids

**Interfaces:**
- Produces: `@Id @GeneratedValue(strategy = UUID) UUID id` em Instituicao, Aluno, Professor, Empresa; `cpf`/`cnpj` `@Column(unique = true, nullable = false)`; FKs `instituicao_id`, `aluno_id`, `professor_id`; `JpaRepository<X, UUID>` com `findByCpf`/`findByCnpj`, `existsByCpf`/`existsByCnpj`, `findBloqueadoById(UUID)`; `PessoaResponse(UUID id, String nome)`; respostas com `id` e o documento; rotas `/alunos/{id}`, `/empresas/{id}`, `/professores/{id}`; `Usuario` sem `Persistable`.

- [ ] Ajustar os testes existentes para criar pela API e usar o `id` devolvido nas rotas (helpers `criarAluno(...) → UUID`); rodar e ver falhar por compilação.
- [ ] Migrar entidades, repositórios, serviços, DTOs e controladores; `DadosIniciais` busca instituição por CNPJ.
- [ ] Teste novo `AlunoControllerTest.rotaComCpfNaoAcha`: `GET /alunos/52998224725` → 404 (CPF não é mais id; UUID inválido no caminho → 404 `RECURSO_NAO_ENCONTRADO` também).
- [ ] `./mvnw test` verde (78 adaptados + novo).

### Task 2: Carteiras (saldo do aluno por professor)

**Files:** Create `model/Carteira.java`, `repository/CarteiraRepository.java`, `dto/CarteiraResponse.java`; Modify `Aluno` (sai `saldo`), `TransacaoService`, `AlunoService`, `AlunoResponse`, `ExtratoResponse`; Test `service/CarteiraTest.java`

**Interfaces:**
- Produces: `Carteira(id, aluno, professor, int saldo)` única por (aluno_id, professor_id); `CarteiraRepository.findBloqueadaByAlunoIdAndProfessorId(UUID, UUID)`, `somaPorAluno(UUID) → int` (0 sem carteira), `findByAlunoIdOrderByProfessorNome`; `CarteiraResponse(PessoaResponse professor, int saldo)`; `ExtratoResponse(int saldo, List<CarteiraResponse> carteiras, List<Movimento> movimentos)` (professor: `carteiras` vazia); `TransacaoService` credita a carteira (cria se não existe) depois de travar o professor.

- [ ] Testes: `envioCreditaCarteiraDoProfessor` (Helena 100 e Ricardo 50 → carteiras [Helena 100, Ricardo 50], saldo 150); `saldoDoAlunoEhSomaDasCarteiras`; concorrência atual (`TransacaoConcorrenciaTest`) adaptada: 20 envios de dois professores ao mesmo aluno → soma das carteiras = soma das transações.
- [ ] Implementar; `alunosComSaldo` e respostas usam `somaPorAluno`.
- [ ] `./mvnw test` verde.

### Task 3: Spring Security, login, admin, acesso por perfil

**Files:** `pom.xml` (+2 starters); Create `security/{SegurancaConfig,ContaLogada,ContasService,ContaAtivaFilter,AuthController}.java`, `model/Admin.java`, `repository/AdminRepository.java`, `dto/{LoginRequest,SessaoResponse}.java`; Modify `UsuarioRepository` (+admins), `ApiException` (+`credenciaisInvalidas`, `naoAutenticado`, `acessoNegado`), `ApiExceptionHandler` (AccessDenied → 403), todos os controllers (`@PreAuthorize`), `DadosIniciais` (admin), `application.properties` (`egon.admin.senha=admin12345`); Create test helper `src/test/java/com/egon/Logins.java`; Tests `security/{AuthTest,AcessoTest}.java`

**Interfaces:**
- Produces: `enum Perfil { ALUNO, PROFESSOR, EMPRESA, ADMIN }`; `ContaLogada implements UserDetails` com `UUID id()`, `Perfil perfil()`, `String nome()`, autoridade `ROLE_<PERFIL>`; regra de dono em SpEL: `@PreAuthorize("hasRole('ADMIN') or #id == principal.id")`; `POST /auth/login`, `GET /auth/eu`, `POST /auth/logout` → `SessaoResponse(Perfil perfil, UUID id, String nome)`; CSRF `CookieCsrfTokenRepository.withHttpOnlyFalse()` + handler de SPA (token cru no cabeçalho `X-XSRF-TOKEN`), cookie entregue em todo `GET /auth/eu`; entrada 401 `NAO_AUTENTICADO` e 403 `ACESSO_NEGADO` no formato da API; `Logins.comoAdmin()`, `comoAluno(UUID)`, `comoProfessor(UUID)`, `comoEmpresa(UUID)` (`RequestPostProcessor` com `user(ContaLogada)` + `csrf()`).
- Públicos: `/auth/**`, `POST /alunos`, `POST /empresas`, `GET /instituicoes`, `GET /cursos`, `/ws/**` (o handshake confere por conta própria). Resto autenticado.

- [ ] Testes `AuthTest`: login de cada perfil devolve perfil/id/nome; senha errada e login inexistente → 401 com a mesma mensagem; `loginIgnoraCaixaEEspacos`; `eu` sem sessão → 401 e com sessão → 200; logout derruba; `escritaSemCsrfDa403`; `postDepoisDoLoginUsaTokenNovo` (MockMvc com cookies reais, sem `csrf()`); `contaExcluidaPerdeASessao` (sessão de aluno, admin exclui, próxima requisição → 401).
- [ ] Testes `AcessoTest`, tabela da seção 6 da spec para os endpoints que já existem: anônimo → 401; outro aluno → 403; dono → 200; admin → 200 onde a tabela diz.
- [ ] Implementar; adaptar os testes antigos com `Logins`.
- [ ] `./mvnw test` verde.

### Task 4: WebSocket pela sessão

**Files:** Modify `notificacao/{PerfilInterceptor,NotificacaoHandler}.java`, `config/WebSocketConfig.java`, `NotificacaoWebSocketTest`

**Interfaces:**
- Produces: destinatário `"<PERFIL>:<uuid>"` vindo do `Principal` do handshake; só ALUNO e PROFESSOR; `NotificacaoHandler.avisar(Perfil, UUID, Object mensagem)` público para os outros eventos (Task 7).

- [ ] Testes: handshake sem sessão → 403; empresa e admin → 403; aluno logado recebe `MOEDAS_RECEBIDAS` com `transacao.aluno.id`; os testes atuais adaptados (login real por `/auth/login` com cookie de sessão no handshake).
- [ ] Implementar; `./mvnw test` verde.

### Task 5: CRUDs do admin e autoedição

**Files:** Create `controller/{AdminController,CursoController(ampliar),InstituicaoController(ampliar)}`, `service/{AdminService,InstituicaoService,CursoService,ProfessorService}` (ampliar), DTOs `*CadastroRequest`/`*AtualizacaoRequest` de instituição, curso, professor, admin; Modify `ApiException` (+`instituicaoEmUso`, `cursoEmUso`, `professorComTransacoes`, `adminProtegido`, `cnpjInstituicaoEmUso`, `cpfProfessorEmUso`, `cursoRepetido`), `CotaSemestralService` (+`creditar(Professor)`); Tests `controller/{InstituicaoCrudTest,CursoCrudTest,ProfessorCrudTest,AdminCrudTest}.java`

**Interfaces:**
- Consumes: `Logins`, `Documentos`, `Senha`/`Cpf`/`Cnpj` validators.
- Produces: `POST/PUT/DELETE /instituicoes[/{id}]`, `/cursos[/{id}]`, `/professores[/{id}]`, `/admins[/{id}]`, `GET /admins`; `CotaSemestralService.creditar(Professor professor)` (cota do semestre atual, se não tiver).

- [ ] Testes: instituição (CNPJ inválido 400, repetido 409 `CNPJ_EM_USO` no campo `cnpj`, excluir com aluno 409 `INSTITUICAO_EM_USO`); curso (nome repetido 409, excluir com aluno 409 `CURSO_EM_USO`); professor (CPF inválido, repetido, login de aluno → 409 `LOGIN_EM_USO`, criado com saldo 1000 e uma COTA no extrato, editar com senha em branco mantém, excluir com envio 409, sem envio apaga e leva cotas); admin (criar, não exclui a si mesmo, não exclui o último → 409 `ADMIN_PROTEGIDO`); aluno edita o próprio (200) e o de outro (403); empresa idem; exclusão de aluno/empresa só admin.
- [ ] Implementar; `./mvnw test` verde.

### Task 6: Vantagens (HU08)

**Files:** Create `model/{Vantagem,TipoVantagem}.java`, `repository/VantagemRepository.java`, `service/{VantagemService,FotoArmazem}.java`, `controller/VantagemController.java`, `dto/{VantagemResponse,VantagemCatalogoResponse}.java`; Modify `EmpresaService.excluir` (apaga vantagens e fotos), `application.properties` (`egon.fotos.pasta=data/fotos`, `spring.servlet.multipart.max-file-size=2MB`, `max-request-size=3MB`); Test `controller/VantagemTest.java` (pasta temporária `@TempDir` via propriedade)

**Interfaces:**
- Produces: `FotoArmazem.gravar(MultipartFile) → String nomeArquivo` (UUID + extensão do tipo detectado pelos bytes: JPEG `FF D8 FF`, PNG `89 50 4E 47 0D 0A 1A 0A`, WebP `RIFF....WEBP`; senão 400 `FOTO_INVALIDA` campo `foto`), `apagar(String)`, `abrir(String) → Resource`; `POST /empresas/{id}/vantagens` (multipart `nome, descricao, custo, tipo, foto`), `PUT …/vantagens/{vid}` (foto opcional), `DELETE …/vantagens/{vid}`, `GET /empresas/{id}/vantagens` → `VantagemResponse(id, nome, descricao, custo, tipo, fotoUrl, alunosComSaldo)`, `GET /vantagens` → `VantagemCatalogoResponse(id, nome, descricao, custo, tipo, fotoUrl, PessoaResponse empresa)`, `GET /vantagens/{id}/foto`. Estouro de tamanho → 400 `FOTO_GRANDE` campo `foto`.

- [ ] Testes: cadastro válido (PNG mínimo) 201; foto com bytes de texto e nome `.png` → 400 `FOTO_INVALIDA`; 2 MB + 1 byte → 400 `FOTO_GRANDE`; sem foto no cadastro → 400; custo 0/-1 → 400 campo `custo`; tipo `XYZ` → 400 campo `tipo`; editar sem foto mantém o arquivo, com foto nova apaga o antigo; excluir apaga o arquivo; outra empresa → 403; foto servida com `Content-Type` certo; `alunosComSaldo` conta só quem tem soma ≥ custo; excluir empresa apaga vantagens e arquivos.
- [ ] Implementar; `./mvnw test` verde.

### Task 7: Transferências entre alunos

**Files:** Create `model/{Transferencia,StatusTransferencia,RegrasMoeda}.java` (`RegrasMoeda` `@Embeddable` em `Professor`), `repository/TransferenciaRepository.java`, `service/{TransferenciaService,TransferenciaDecidida}.java`, `controller/TransferenciaController.java`, `dto/{TransferenciaRequest,TransferenciaResponse,RegrasRequest,RegrasResponse,ColegaResponse}.java`; Modify `ProfessorController` (regras, transferências, alunos), `AlunoController` (colegas, transferências), `Movimento` (+ `TRANSFERENCIA_ENVIADA`, `TRANSFERENCIA_RECEBIDA` com `status` e `origem`), `NotificacaoHandler` (evento `TRANSFERENCIA`), `ApiException` (+códigos da spec seção 5); Tests `service/TransferenciaTest.java`, `service/TransferenciaConcorrenciaTest.java`

**Interfaces:**
- Consumes: `CarteiraRepository.findBloqueadaByAlunoIdAndProfessorId`, `ProfessorRepository.findBloqueadoById`, `NotificacaoHandler.avisar`.
- Produces: `RegrasMoeda(boolean permiteRepasse = true, boolean exigeAprovacao = true, Integer valorMaximo, Integer limiteMensal, boolean mesmoCurso = false)`; `POST /alunos/{id}/transferencias` `{destinatarioId, professorId, valor, motivo}` → 201 `TransferenciaResponse(id, data, valor, motivo, status, PessoaResponse remetente, destinatario, professor, Instant dataDecisao)`; `POST /transferencias/{id}/{aprovar|recusar|cancelar}` → 200; `GET /professores/{id}/transferencias` (pendentes primeiro, depois mais recentes); `GET/PUT /professores/{id}/regras`; `GET /alunos/{id}/colegas` → `ColegaResponse(id, nome, curso)`; `GET /professores/{id}/alunos` → idem.
- Ordem de trava: professor de origem → carteira do remetente → carteira do destinatário.

- [ ] Testes de regra (um por código): `REPASSE_DESATIVADO`, `ACIMA_DO_MAXIMO` (máx 50, pede 51), `LIMITE_MENSAL` (limite 100, já 80 no mês entre pendente e aprovada, pede 30; recusada e cancelada não contam; mês anterior não conta, com `Clock` fixo), `CURSO_DIFERENTE`, `DESTINATARIO_INVALIDO` (ele mesmo; outra instituição), `SALDO_INSUFICIENTE` (carteira 20, pede 21), motivo em branco 400.
- [ ] Testes de fluxo: sem aprovação → `APROVADA`, carteiras −30/+30 da mesma origem; com aprovação → `PENDENTE`, remetente −30 já, destinatário 0; aprovar → +30; recusar → remetente +30; cancelar (remetente) → +30; aprovar por outro professor → 403; cancelar por outro aluno → 403; decidir duas vezes → 409 `TRANSFERENCIA_DECIDIDA`; `mudarRegraNaoMexeNasPendentes`; `destinoSegueInstituicaoAtualDoProfessor`; extratos mostram as transferências com status e origem; avisos: `SOLICITADA` ao professor e remetente, `APROVADA` a remetente e destinatário, `RECUSADA` ao remetente, `CANCELADA` ao professor, nada em rollback.
- [ ] Concorrência: 20 pedidos simultâneos de 10 com carteira 100 → no máximo 10 aceitos e carteira nunca negativa; aprovar e cancelar a mesma pendente ao mesmo tempo → um 200 e um 409, saldo final coerente.
- [ ] Implementar; `./mvnw test` verde.

### Task 8: Resumo do admin e invariante

**Files:** Create `service/ResumoService.java`, `controller/ResumoController.java` (`GET /admin/resumo`), `dto/ResumoResponse.java`; Test `service/ResumoTest.java`

**Interfaces:**
- Produces: `ResumoResponse(Egos egos, List<EnvioMes> enviosPorMes, List<PorInstituicao> porInstituicao, Contagens contagens)` com os campos da spec seção 9; `enviosPorMes` com os 6 meses (zeros incluídos), mês `"AAAA-MM"` no fuso de São Paulo.

- [ ] Testes: números conferidos contra consultas diretas; `invarianteDosEgos` depois de cota, professor novo, envios, transferências aprovada, pendente, recusada e cancelada: `emitidos == comProfessores + comAlunos + reservados`; mês sem envio aparece com zero; só admin (403 para os outros).
- [ ] Implementar; `./mvnw test` verde. Conferir o diff do backend inteiro antes do front.

### Task 9: Front — sessão, cliente da API e rotas

**Files:** Rewrite `frontend/src/lib/api.ts` (ids, CSRF, `credentials`, 401 → evento de sessão expirada); Create `lib/sessao.tsx` (contexto `SessaoProvider`, `useSessao()`, `RotaProtegida`); Rewrite `pages/Entrar.tsx` (login); Modify `main.tsx` (rotas `/entrar`, `/painel`, `/admin/*`, saem `/aluno/:cpf`, `/professor/:cpf`, `/alunos`, `/empresas`), `content.ts` (rotas), `components/Shell.tsx` (cabeçalho com nome, perfil, Meus dados, Sair), `forms/*` (cadastros terminam no `/entrar`; `idInstituicao`), `lib/useNotificacoes.ts` (sem parâmetros na URL)

**Interfaces:**
- Produces: `api.auth.{login, eu, logout}`; `useSessao() → { sessao: Sessao | null, carregando, entrar(login, senha), sair() }`; `<RotaProtegida perfis={['ADMIN']}>`; todas as chamadas por id.

- [ ] Implementar; `npm run build` e `npm run lint` sem erro novo.
- [ ] Chrome headless: login de cada perfil cai no `/painel`; `/admin/alunos` como aluno volta ao `/painel`; sem sessão vai a `/entrar?volta=…`.

### Task 10: Bento e DESIGN.md

**Files:** Create `components/Bento.tsx`; Modify `DESIGN.md` (seção Bento), `.impeccable/design.json` se o comando `document` pedir

**Interfaces:**
- Produces: `<Bento>` (grade 12/6/1 colunas) e `<Bloco area className titulo acoes destaque?>` (um painel por bloco, quatro estados via `components/states.tsx`); classes de tamanho por bloco definidas em cada dashboard.

- [ ] Rodar `impeccable` (shape) para os quatro dashboards antes de desenhar; escrever as regras no DESIGN.md (seção 11 da spec).

### Task 11: Dashboard do aluno

**Files:** Create `painel/aluno/{PainelAluno,RepassarEgos,MinhasTransferencias,Catalogo}.tsx`, `painel/aluno/regras.ts` (validação do repasse no cliente espelhando o backend); Modify `content.ts` (templates `TRANSFERENCIA` por perfil e evento)
- [ ] Blocos da spec seção 9 (aluno). Formulário mostra as regras do professor escolhido. Aviso ao vivo atualiza saldo, carteiras e listas sem duplicar (por id).
- [ ] build, lint; autoverificação de `regras.ts` com `node --experimental-strip-types` no scratchpad.

### Task 12: Dashboard do professor

**Files:** Create `painel/professor/{PainelProfessor,Pendencias,RegrasMoeda,CotaDoSemestre}.tsx`; mover `painel/EnviarEgos.tsx` (usa `GET /professores/{id}/alunos`)
- [ ] Blocos da spec (professor); aprovar/recusar com retorno imediato e aviso ao vivo de novas pendências.

### Task 13: Dashboard da empresa

**Files:** Create `painel/empresa/{PainelEmpresa,VantagemForm,MinhasVantagens}.tsx`
- [ ] CRUD com multipart (pré-visualização local da foto, limite de 2 MB e tipo conferidos antes de enviar), excluir com confirmação, "Meus dados".

### Task 14: Dashboard e gestão do admin

**Files:** Create `painel/admin/{PainelAdmin,EnviosPorMes}.tsx`, `pages/admin/{Instituicoes,Cursos,Professores,Alunos,Empresas,Admins}.tsx` e formulários em `forms/`
- [ ] Blocos da spec (admin) com a igualdade dos Egos visível; gestão com listar, criar (onde se aplica), editar, excluir e os 409 mostrados.

### Task 15: Verificação, documentação e entrega

- [ ] `./mvnw test` (total e falhas), `npm run build`, `npm run lint`.
- [ ] Roteiro no Chrome headless (puppeteer-core no scratchpad, Chrome do sistema) a 1440 e 375 px: login dos quatro perfis; envio professor→aluno ao vivo; transferência com aprovação (pedido, aviso ao professor, aprovação, aviso aos dois); sem aprovação; regra violada; vantagem com foto; admin cria instituição e professor (cota 1000) e vê a invariante fechar.
- [ ] `impeccable audit` e `critique` nos dashboards; corrigir o que mudar o resultado.
- [ ] Docs (spec seção 12) e REVISAO.md (seção da rodada 2 com roteiro manual, logins de teste, decisões, falhas conhecidas).
- [ ] Reiniciar a visualização (8080/5180) com o código novo e banco novo; criar dados de exemplo nela.
