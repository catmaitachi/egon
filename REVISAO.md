# Revisão manual: moedas, extratos, avisos ao vivo e novo frontend

Arquivo descartável, para quem vai testar à mão. Pode apagar depois da revisão. Nada foi commitado.

> **Rodada 2** (login, admin, ids, CRUDs, vantagens, moeda por professor com repasse, dashboards em bento e modo dev) logo abaixo. A rodada 1 continua depois dela; onde as duas discordam, vale a rodada 2 (a tela Entrar sem senha e o CPF na URL não existem mais).

# Rodada 3: resgate com cupom e telas largas

**O que entrou.** O aluno resgata uma vantagem do catálogo (HU09). O custo sai das carteiras, a de maior saldo primeiro, com o quanto saiu de cada uma registrado. Cada resgate gera um cupom `EGON-XXXX-XXXX`, sorteado de forma segura, com QR code (HU10). O QR leva a `/painel?cupom=…`: a empresa logada que o lê cai na conferência com o código preenchido. A empresa confere quem resgatou o quê e confirma a troca; o aluno recebe o aviso ao vivo. As telas de quem está logado usam a tela até 100rem, com margens maiores no desktop.

**Como testar à mão.**
1. Como aluno com saldo, em "O que já dá para trocar", clique em "Resgatar" e confirme. Esperado: o saldo cai pelo custo, abre o cupom com QR e código, e o cupom aparece em "Meus cupons" como "Para usar". O extrato mostra o resgate como saída, com o código. A empresa (outra janela) recebe "Novo resgate" ao vivo.
2. Leia o QR com a câmera do celular, ou abra o link `/painel?cupom=<código>` logado como a empresa. Esperado: "Conferir cupom" já vem preenchido, mostrando a vantagem, o aluno e "Pronto para usar".
3. Digite o código em minúsculas, com espaços ou sem os hífens: também acha.
4. Clique em "Confirmar troca". Esperado: "Troca confirmada", o aluno recebe "Cupom usado" ao vivo, e o cupom vira "Usado em…" para os dois. Confirmar de novo dá "Este cupom já foi usado."
5. Como outra empresa, o mesmo código responde "Nenhum cupom com este código nas suas vantagens".
6. Tente excluir a vantagem resgatada ou a empresa (como admin): recusado, porque os cupons dependem delas.
7. No painel do admin, "Onde estão os Egos" ganha "Gastos em resgates", e a conta continua fechando.
8. Saldo menor que o custo: a vantagem não aparece em "O que já dá para trocar" (pela API, `POST /alunos/{id}/resgates` dá 422 `SALDO_INSUFICIENTE`).
9. Em 1920, 1440 e 1024px os painéis ocupam a largura sem faixas vazias e as duas colunas terminam na mesma linha.

**Testes.** `sh mvnw test`: **146 testes, 0 falhas**. Novos: `resgate/ResgateTest` (débito pela maior carteira, extrato, saldo insuficiente, acesso, conferência com código digitado de qualquer jeito, confirmação única, cupom de outra empresa, exclusões), `resgate/CodigoCupomTest` (formato, 10 mil códigos sem repetir, normalização), `resgate/ResgateConcorrenciaTest` (dois resgates que não cabem juntos e duas confirmações ao mesmo tempo; sem as travas, os dois testes falham: conferido trocando o código), aviso de cupom no `NotificacaoWebSocketTest` e a conta dos Egos com resgate no `ResumoTest`. No Chrome, o roteiro completo deu 23 de 23. O QR da tela foi decodificado com `jsQR` e leva ao link de confirmação; as colunas do bento ficam alinhadas a 1920, 1440 e 1024px nos quatro painéis.

**Decisões.** Maior carteira primeiro; código de 40 bits do alfabeto de Crockford; QR gerado no front com `qrcode-generator` (dependência nova, MIT, sem dependências próprias) e desenhado pela matriz, sem injetar HTML; a empresa passa a receber avisos ao vivo; vantagem e empresa com resgate não saem; cupom sem expiração nem cancelamento (não pedido); "Confirmar troca" é botão de contorno, porque o roxo da tela da empresa é o "Publicar".

**Limites.** A leitura do QR é pela câmera do celular (link), não por câmera dentro do app. A tabela `NOTIFICACAO` do modelo continua sem uso.

---

# Rodada 2

## R2.1 O que foi feito

| Parte | O que muda | Onde |
| :--- | :--- | :--- |
| Login (HU01) | Spring Security com sessão (cookie HttpOnly) para aluno, professor, empresa e admin; regras por perfil e por pessoa em cada endpoint; CSRF por cookie; sessão de conta excluída cai na próxima requisição | `backend/.../security/*`, `frontend/src/lib/api.ts`, `components/Sessao.tsx`, `pages/Entrar.tsx` |
| Ids | Chave UUID em todas as tabelas de pessoa, instituição, vantagem, carteira e transferência; CPF e CNPJ só como colunas únicas, fora das URLs | entidades e controllers; banco de dev agora em `backend/data/egon2` |
| Admin (HU04, HU14) | Admin pré-cadastrado; CRUD de instituições, cursos, professores (com cota na hora), alunos, empresas e admins; resumo com a conta dos Egos | `AdminController`, `InstituicaoController`, `CursoController`, `ProfessorController`, `ResumoController`; front `pages/admin/Gestoes.tsx`, `painel/admin/` |
| Vantagens (HU08) | CRUD da empresa com foto (JPEG/PNG/WebP, até 2 MB, conferida pelos bytes); catálogo para o aluno; quantos alunos já podem pagar | `VantagemController`, `FotoArmazem`; front `painel/empresa/` |
| Moeda por professor e repasse (HU13) | Carteira por professor; repasse entre alunos sob as regras do professor (permitir, aprovação, máximo, limite mensal, mesmo curso); reserva enquanto pendente; aprovar, recusar, cancelar; avisos ao vivo | `TransferenciaService`, `Carteira`, `RegrasMoeda`; front `painel/aluno/`, `painel/professor/` |
| Dashboards em bento | `/painel` por perfil, blocos por pergunta, colunas no desktop e uma coluna no celular | `components/Bento.tsx`, `painel/*`; regras no `DESIGN.md` |
| Modo dev | Código Konami no login mostra todas as contas e entra sem senha; só existe com o perfil `dev` (ou `EGON_DEV=true`) | `dev/ModoDevController`, `components/ModoDev.tsx`, `lib/useKonami.ts` |

Plano e design desta rodada: `docs/superpowers/specs/2026-11-05-acesso-e-dashboards-design.md` e `docs/superpowers/plans/2026-11-05-acesso-e-dashboards.md` (também descartáveis).

## R2.2 Como rodar

```bash
cd backend && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home sh mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd frontend && npm run dev
```

O banco novo (`backend/data/egon2`) nasce na primeira subida com 3 instituições, 8 cursos, 4 professores (1.000 Egos cada) e o admin. O arquivo antigo `backend/data/egon.mv.db` não é usado nem foi tocado.

A visualização desta sessão já está no ar em **http://localhost:5180** (API na 8080, com o modo dev ligado e banco próprio no scratchpad).

Contas: `admin` / `admin12345`; professores `helena.duarte`, `ricardo.moraes` (Horizonte), `beatriz.campos` (Vale Verde), `marcelo.tavares` (Aurora), todos com `professor123`. Ou, na tela de login, ↑ ↑ ↓ ↓ ← → ← → B A (fora dos campos) e um clique.

## R2.3 Roteiro de teste manual

1. **Modo dev.** Em `/entrar`, sem clicar em campo nenhum, digite ↑ ↑ ↓ ↓ ← → ← → B A. Esperado: aparece "Modo dev" com as contas agrupadas por perfil; um clique entra no `/painel` daquela conta. Suba a API sem `-Dspring-boot.run.profiles=dev` e repita: aparece o aviso de que a API está sem o modo dev, e `curl localhost:8080/dev/contas` dá 404.
2. **Login.** `helena.duarte` com senha errada e `ninguem` com qualquer senha: a mesma mensagem "Login ou senha inválidos.". Com a certa, vai para `/painel`. Abra `/admin/alunos` como professora: volta para `/painel`. Saia e abra `/painel`: vai para `/entrar` e, depois do login, volta para onde ia.
3. **Cadastros.** Crie dois alunos na Universidade Horizonte (mesmo curso) e um no Vale Verde em `/cadastro`, e uma empresa em `/empresa/cadastro`. Esperado: no fim, o link "Entrar".
4. **Envio ao vivo.** Professora Helena numa janela, aluno A noutra (janela anônima). Helena envia 100 ao aluno A com motivo. Esperado: confirmação para Helena (saldo 900), aviso para o aluno com professor, valor, data e mensagem, saldo 100 e a carteira "De Helena Duarte: 100" sem recarregar.
5. **Repasse com aprovação** (padrão). Aluno A repassa 30 ao aluno B. Esperado: "Pedido enviado", saldo de A 70 (reservado), o pedido aparece em "Esperando o professor" com "Cancelar"; Helena recebe "Pedido de repasse" ao vivo. Helena aprova: A recebe "Repasse concluído", B recebe "Você recebeu 30 Egos" (Egos de Helena Duarte). Repita e recuse: os 30 voltam para A. Repita e cancele como A: Helena recebe "Pedido de repasse cancelado".
6. **Regras.** Helena desliga "Exigir minha aprovação": o repasse conclui na hora. Máximo 20: repassar 21 dá erro no valor. Limite mensal 50 com 40 já repassados no mês: repassar 20 dá erro no valor dizendo quanto já foi. "Só mesmo curso": colega de outro curso dá erro no colega. "Permitir repasse" desligado: erro na carteira. Aluno do Vale Verde não aparece como colega do aluno A (ou dá erro de colega, se a professora mudou de instituição).
7. **Cota.** No painel da Helena, "Cota do semestre" mostra quanto dos 1.000 já foi distribuído.
8. **Empresa.** Entre como a empresa: cadastre uma vantagem com foto (troque o arquivo antes de publicar para ver a prévia). Tente um `.txt` renomeado para `.png`: erro na foto. Edite sem foto nova (fica a atual) e com foto nova. "Meus dados" edita nome, e-mail e login. Como aluno A, a vantagem aparece em "O que já dá para trocar" se o custo couber no saldo.
9. **Admin.** O painel mostra "Onde estão os Egos" com a conta fechando (emitidos = professores + carteiras + reservados; com um repasse pendente, os reservados aparecem). Cadastre uma instituição, um curso e um professor (ele já aparece com 1.000 Egos). Tente excluir a Horizonte (tem alunos), um curso com alunos, a Helena (já enviou), o aluno A (tem histórico) e a si mesmo: cada um com a mensagem de por que não pode. Exclua a empresa: as vantagens somem do catálogo.
10. **Sessão.** Com o aluno B logado numa janela, exclua um aluno sem histórico como admin noutra, entrando com ele antes: a próxima ação dele leva ao login com "Sua sessão terminou".
11. **Bento sem vãos.** A 1440 e a 1024 px, nos quatro painéis, as duas colunas terminam na mesma linha; com muitos movimentos, o extrato rola por dentro em vez de esticar a página (medido no Chrome: diferença de 0 px nos 8 casos).
12. **375 px.** Repita os painéis no celular (ou DevTools a 375 px): uma coluna, na ordem saldo, extrato, ações; sem rolagem para o lado.

Atalhos de API (com o cookie de sessão de um navegador logado, ou pelo `/dev/entrar` com a API em dev):

```bash
C=/tmp/egon.jar; curl -s -c $C -b $C localhost:8080/auth/eu >/dev/null; T=$(awk '/XSRF/{print $7}' $C)
curl -s -c $C -b $C -H "X-XSRF-TOKEN: $T" -H 'Content-Type: application/json' localhost:8080/auth/login -d '{"login":"admin","senha":"admin12345"}'
curl -s -b $C localhost:8080/admin/resumo
```

## R2.4 Testes automatizados

`sh mvnw test` (JDK 21): **134 testes, 0 falhas**. Os novos desta rodada:

| Classe | O que prova |
| :--- | :--- |
| `security/AuthTest` | Login dos quatro perfis, mesma resposta para login inexistente e senha errada, caixa e espaços no login, logout, CSRF obrigatório e trocado no login, sessão de conta excluída |
| `security/AcessoTest` | A matriz de acesso: anônimo 401, outra pessoa ou perfil 403, quem pode 2xx |
| `admin/CadastrosDoAdminTest` | CRUD de instituição, curso, professor (cota na hora) e admin, com os 409 |
| `vantagem/VantagemTest` | Cadastro com foto, foto falsa, grande ou ausente, edição com e sem foto, arquivo apagado depois do commit, acesso, catálogo, alunos com saldo |
| `transferencia/TransferenciaTest` | Cada regra fixa e do professor, fluxos com e sem aprovação, quem decide, decisão repetida, regra mudada com pendente, professor que muda de instituição, extratos, listas sem documentos |
| `transferencia/TransferenciaConcorrenciaTest` | 20 repasses simultâneos não estouram a carteira; aprovar e cancelar ao mesmo tempo decide uma vez (sem as travas os dois falham: conferido trocando o código) |
| `admin/ResumoTest` | Números do resumo e a invariante dos Egos com transferências em todos os estados |
| `dev/ModoDevTest` | Modo dev ligado lista e entra sem senha; desligado, as rotas não existem |
| `notificacao/NotificacaoWebSocketTest` | Handshake pela sessão (sem sessão, admin e empresa recusados) e os avisos de repasse para as pessoas certas |

Front: `npm run build` ok, `npm run lint` sem erros e só os 2 avisos antigos da landing; detector do impeccable sem achados nas telas novas. Roteiro no Chrome headless (modo dev, envio e repasse ao vivo, vantagem com foto, conta do admin, acesso negado, 4 painéis a 375 px sem rolagem lateral): 18 de 18 verificações ok.

## R2.5 Decisões tomadas por mim (para conferir)

- Professor cadastrado pelo admin recebe a cota do semestre na hora.
- Excluir empresa apaga as vantagens e as fotos dela.
- Banco de dev em arquivo novo (`data/egon2`), porque a chave primária mudou; o antigo fica intocado.
- Anônimo em rota inexistente recebe 401, não 404 (não revela quais rotas existem).
- CSRF inválido tem código próprio (`CSRF_INVALIDO`); o front busca um token novo e repete uma vez.
- Quem não é admin e manda corpo inválido recebe 400 (mensagens de validação) antes do 403.
- Pedido de repasse com professor inexistente responde `SALDO_INSUFICIENTE` (o aluno não tem carteira dele), sem revelar se o id existe.
- O destinatário só vê no extrato repasses aprovados; o remetente vê todos.
- Os avisos ao vivo recarregam o extrato em vez de mesclar na tela (repasse mexe em carteiras).
- O front da rodada 1 foi adaptado, não refeito: a base estava certa.

## R2.6 Limites conhecidos

- Resgate e cupom (HU09, HU10) não existem; falta decidir de quais carteiras o resgate debita.
- Dois admins excluindo um ao outro ao mesmo tempo podem deixar o sistema sem admin (a checagem do "último" não trava a tabela).
- Se o navegador não conseguir ler a foto escolhida (arquivo apagado depois da escolha), a tela diz "não foi possível falar com o servidor".
- O nome no cabeçalho é o do login; se o admin renomear a pessoa, muda no próximo login. Trocar o login de quem está logado não derruba a sessão (só excluir derruba).
- Com a pessoa deslogada, o navegador registra no console o 401 esperado de `/auth/eu` (duas vezes em dev, pelo StrictMode).
- Diagramas PNG não regerados (lista em "Mudanças pendentes nos diagramas" do Modelo de Dados).
- O front continua sem testes automatizados (sem runner, e dependência nova não entrou).
- O modo dev nunca deve ser ligado fora da máquina de quem desenvolve: com ele, qualquer pessoa entra em qualquer conta.

---

# Rodada 1

## 1. O que foi implementado

### Backend, por HU

| HU | O que faz | Arquivos principais (`backend/src/main/java/com/egon/`) |
| :--- | :--- | :--- |
| HU04 Professores pré-cadastrados | Entidade `Professor` (sem e-mail). 4 professores fictícios entram na primeira subida, só com a tabela vazia. `GET /professores` (por nome) e `GET /professores/{cpf}`, sem POST/PUT/DELETE. O login único passa a incluir professores. Se um aluno ou empresa antigo já tiver o login de um professor, a subida para com mensagem clara. | `model/Professor.java`, `repository/ProfessorRepository.java`, `service/ProfessorService.java`, `controller/ProfessorController.java`, `dto/ProfessorResponse.java`, `config/DadosIniciais.java`, `repository/UsuarioRepository.java` |
| HU05 Cota semestral | `CotaSemestral`, uma por professor e semestre (o par é único). Credita 1.000 e soma ao saldo, sem zerar. Roda na subida, antes do Tomcat aceitar conexões, e por `@Scheduled(cron = "0 5 0 1 1,7 *", zone = "America/Sao_Paulo")`. O semestre vem de um `Clock` (bean `relogio`). | `model/CotaSemestral.java`, `repository/CotaSemestralRepository.java`, `service/CotaSemestralService.java`, `config/TempoConfig.java` |
| HU06 Envio de Egos | `POST /professores/{cpf}/transacoes` `{cpfAluno, valor, motivo}` → 201. Professor e aluno são lidos com `PESSIMISTIC_WRITE`, sempre o professor primeiro. Débito, crédito e insert acontecem numa só transação. O CPF aceita máscara. `valor` com fração ou de outro tipo dá 400 no campo `valor` (não trunca mais). Editar ou excluir um aluno durante um envio também trava o aluno. | `model/Transacao.java`, `repository/TransacaoRepository.java`, `service/TransacaoService.java`, `dto/TransacaoRequest.java`, `dto/TransacaoResponse.java`, `dto/PessoaResponse.java`, `exception/ApiException.java`, `exception/ApiExceptionHandler.java`, `repository/AlunoRepository.java` (`findBloqueadoByCpf`), `service/AlunoService.java`, `resources/application.properties` (`accept-float-as-int=false`) |
| HU07 Aviso no app ao vivo (no lugar do e-mail) | WebSocket puro em `/ws/notificacoes?perfil=aluno\|professor&cpf=…`. O handshake recusa com 403 perfil inválido, CPF de outro perfil e origem fora de `egon.ws.origens`. O envio sai em `@TransactionalEventListener(AFTER_COMMIT)`: rollback não avisa. Mensagens `MOEDAS_RECEBIDAS` (aluno) e `ENVIO_CONFIRMADO` (professor), com a transação e o saldo novo. Várias abas recebem. | `service/TransacaoConfirmada.java`, `notificacao/NotificacaoHandler.java`, `notificacao/PerfilInterceptor.java`, `config/WebSocketConfig.java`, `dto/Notificacao.java`, `pom.xml` (`spring-boot-starter-websocket`) |
| HU11 Extrato do aluno | `GET /alunos/{cpf}/extrato` → `{saldo, movimentos}`, do mais recente para o mais antigo. Só `RECEBIMENTO`. | `controller/AlunoController.java`, `service/AlunoService.java`, `dto/ExtratoResponse.java`, `dto/Movimento.java` |
| HU12 Extrato do professor | `GET /professores/{cpf}/extrato`: `ENVIO` e `COTA`, do mais recente para o mais antigo. | `service/ProfessorService.java`, `controller/ProfessorController.java` |
| Exclusão de aluno | `DELETE /alunos/{cpf}` de quem já recebeu Egos → 409 `ALUNO_COM_TRANSACOES`. Sem transações, exclui como antes. | `service/AlunoService.java`, `exception/ApiException.java` |

### Frontend (refeito, exceto a landing)

| Rota | Tela | Arquivos (`frontend/src/`) |
| :--- | :--- | :--- |
| `/` | Landing (só mudou o texto de e-mail e o link Entrar) | `App.tsx`, `sections/*`, `content.ts` |
| `/entrar` | Abas Aluno \| Professor (setas, Home, End), busca sem acento, lista por nome em pt-BR, sem senha. A aba fica em `?perfil=`. | `pages/Entrar.tsx` |
| `/aluno/:cpf` | Nome, saldo, extrato e aviso ao vivo. Status "Ao vivo / Reconectando…". | `pages/PainelAluno.tsx`, `painel/Painel.tsx`, `painel/usePainel.ts`, `painel/regras.ts` |
| `/professor/:cpf` | Saldo, Enviar Egos (alunos da mesma instituição, valor, motivo de até 500 caracteres), extrato com envios e cotas e aviso ao vivo. | `pages/PainelProfessor.tsx`, `painel/EnviarEgos.tsx`, `painel/*` |
| `/cadastro`, `/empresa/cadastro` | Cadastro de aluno (HU02) e de empresa (HU03), com validação no cliente e as mesmas mensagens do backend | `pages/CadastroAluno.tsx`, `pages/CadastroEmpresa.tsx`, `forms/*` |
| `/alunos`, `/empresas` | Listagem, edição em diálogo e exclusão com confirmação | `pages/Alunos.tsx`, `pages/Empresas.tsx`, `components/Dialog.tsx` |
| `*` | Página não encontrada | `pages/NaoEncontrada.tsx` |

Peças compartilhadas: `lib/api.ts` (cliente e tipos), `lib/useNotificacoes.ts` (WebSocket nativo, espera de 1 s a 30 s, recarrega o extrato a cada conexão), `lib/useLiveNotices.ts` + `components/LiveNotices.tsx` (avisos: 10 s, até 3 no desktop e 1 abaixo de 640 px), `lib/useLoad.ts`, `lib/useFormSubmit.ts`, `components/{Shell,controls,fields,states,Balance}.tsx`, `content.ts` (`notifications`, os dois templates de aviso), `main.tsx` (rotas), `vite.config.ts` (`ws: true` no proxy). Design em `DESIGN.md` e `.impeccable/design.json`.

## 2. Decisões e impasses resolvidos

Todas estão em `docs/Sprint 2/Modelo de Dados.md`, na tabela "Divergências e decisões", salvo onde está indicado outro lugar.

| Decisão | Onde está |
| :--- | :--- |
| Identidade sem login: o CPF fica na URL, e a tela Entrar escolhe o perfil sem senha (HU01 fora do escopo). | Divergência 12; `README.md`; `PRODUCT.md:35`; texto em `pages/Entrar.tsx` |
| O texto dos avisos é montado no front, porque o nome "Ego" só existe em `content.ts`. | Divergência 13; seção "Aviso ao vivo" do Modelo de Dados; `frontend/src/content.ts` (`notifications`) |
| Não há tabela de notificação para transação: quem está offline vê a transação no extrato. `NOTIFICACAO` fica só para o cupom (HU10). | Divergência 14 |
| O extrato do aluno não tem saídas: as trocas dependem das HU09 e HU10. | Divergência 15 |
| A cota entra no extrato do professor. | Divergência 18 |
| 409 ao excluir aluno com transações. | Divergência 17 |
| `PROFESSOR_CURSO` não foi implementada. | Divergência 16 |
| A cota semestral foi confirmada como tabela própria. | Divergência 10 |
| JDK 21: o Lombok não compila no Java 27. | `README.md`, "Como rodar" |
| Os diagramas PNG não foram regerados (draw.io indisponível): vale o texto. | Modelo de Dados, "Mudanças pendentes nos diagramas" |
| HU11: a ordem passou a ser "da mais recente para a mais antiga". | `docs/Sprint 1/Historias de Usuario.md:141` |
| Valor com fração é recusado (antes 1.5 virava 1). | `backend/src/main/resources/application.properties` |

## 3. Como rodar

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

**Antes de subir, confira o que está rodando** (`lsof -nP -iTCP -sTCP:LISTEN | grep -E ':(8080|8081|5173|5180) '`). Estado em 05/11:

- **PID 90418, porta 8081**: segura o arquivo `backend/data/egon.mv.db`. Foi iniciado em 21/10 por outra sessão (filho do mvn PID 90293) e roda de `backend/target/classes`, que os builds desta tarefa sobrescreveram. Pare-o (`kill 90418 90293`) antes de subir a API com o banco real. Enquanto ele estiver de pé, o H2 recusa abrir o arquivo.
- **PID 26539, porta 8080**: é a pré-visualização desta tarefa (front em http://localhost:5180), com uma cópia do banco no scratchpad. Ela foi reiniciada com as classes já corrigidas (recusa `valor` 1.5 com `campos.valor`). Quando não for mais usar, pare esse processo e o vite da 5180.
- **Porta 5173**: está ocupada por outro projeto (PID 86737). O `npm run dev` vai cair na 5174 ou na seguinte. Use a URL que ele imprimir.

API (porta 8080). A primeira subida sobre o banco real cria as tabelas `professor`, `cota_semestral` e `transacao` (ddl-auto=update), carrega os 4 professores e credita a cota 2026.2. No log, a cota sai antes de "Tomcat started".

```bash
cd backend && ./mvnw spring-boot:run
```

Recomendado para o teste manual: usar um banco descartável. O roteiro cria alunos que, depois de receber Egos, **não podem mais ser excluídos** (409). Com um caminho novo, o banco nasce vazio, só com instituições, cursos e professores:

```bash
cd backend && SPRING_DATASOURCE_URL=jdbc:h2:file:/tmp/egon-revisao/egon ./mvnw spring-boot:run
```

Front, com `/api` (e o WebSocket) apontando para a 8080:

```bash
cd frontend && npm install && npm run dev
```

## 4. Dados de teste

Professores, pré-cadastrados. A senha `professor123` fica gravada, mas não é usada, porque não há login.

| Nome | CPF | Instituição | Login |
| :--- | :--- | :--- | :--- |
| Helena Duarte | 482.915.736-46 | Universidade Horizonte | helena.duarte |
| Ricardo Moraes | 615.283.947-55 | Universidade Horizonte | ricardo.moraes |
| Beatriz Campos | 739.461.258-91 | Centro Universitário Vale Verde | beatriz.campos |
| Marcelo Tavares | 857.134.629-19 | Faculdade de Tecnologia Aurora | marcelo.tavares |

Instituições: Universidade Horizonte `11.222.333/0001-81`, Centro Universitário Vale Verde `45.218.073/0001-92`, Faculdade de Tecnologia Aurora `28.039.156/0001-03`. Cursos: ids 1 a 8 (Administração … Sistemas de Informação, `GET /cursos`).

Na cópia do banco real que usei na verificação, não havia alunos nem empresas. Para o roteiro, use estes alunos (CPFs válidos, aceitos pela API):

| Aluno | CPF | Instituição |
| :--- | :--- | :--- |
| Ana Lima | 123.456.781-43 | Horizonte |
| Bruno Souza | 234.567.892-54 | Horizonte |
| Carla Dias | 345.678.903-37 | Vale Verde |
| Érico Valente (opcional, para a ordem) | 456.789.012-49 | Horizonte |

CPF válido que não existe: `111.444.777-35`. Empresa: CNPJ alfanumérico `12.ABC.345/01DE-35`, ou numérico `11.444.777/0001-61`.

## 5. Roteiro de teste manual

Use duas janelas lado a lado (professor e aluno) e o DevTools aberto (Console e Network).

**Landing e cadastros**

1. Abra `/`. Esperado: nenhuma menção a e-mail ("Aviso no app"). Os links "Entrar" levam a `/entrar`.
2. Em `/cadastro`, clique em criar com tudo vazio. Esperado: 9 campos marcados e o foco em "Nome completo". Digite no Nome e depois no E-mail: o foco fica onde você digita e os erros diminuem (o cursor não pula mais de campo).
3. CPF `123.456.789-00`. Esperado: "CPF inválido. Confira os 11 dígitos." embaixo do CPF.
4. Crie Ana Lima (Horizonte, Ciência da Computação, login `ana.lima`, senha de 8+ caracteres). Esperado: "Conta criada", com o foco no título. Crie também Bruno Souza (Horizonte), Carla Dias (Vale Verde) e, se quiser, Érico Valente (Horizonte).
5. Repita o CPF da Ana, depois o e-mail e depois o login. Esperado: o erro aparece embaixo do campo certo. Com o login `helena.duarte`: "Este login já está em uso. Escolha outro."
6. Em `/empresa/cadastro`, CNPJ `12.ABC.345/01DE-99`. Esperado: "CNPJ inválido. Confira os 14 caracteres." Digite `12.abc.345/01de-35`: a máscara põe em maiúsculas, e o cadastro termina em "Empresa cadastrada".

**Entrar e painéis**

7. Em `/entrar`, aba Professor (as setas trocam de aba), clique em Helena Duarte. Esperado: `/professor/48291573646`, saldo 1.000, extrato com "Cota do semestre 2026.2 +1.000" e lista de alunos só com Ana, Bruno (e Érico), sem Carla. Ordem pt-BR: Ana, Bruno, Érico.
8. Na outra janela, abra `/entrar`, aba Aluno, e clique em Ana Lima. Esperado: `/aluno/12345678143`, saldo 0, extrato vazio com uma frase explicando como chegam os Egos e status "Ao vivo".

**Envio e aviso ao vivo**

9. Na Helena: busque "ana", escolha Ana Lima, Valor 50, Motivo "Explicou recursão para a turma", e clique em Enviar Egos. Esperado na Helena: aviso "Envio confirmado — 50 Egos para Ana Lima, em <data>. Saldo restante: 950 Egos." com o motivo, saldo 950, a linha "Para Ana Lima −50" no topo, formulário limpo e foco no botão. Esperado na Ana, sem recarregar: aviso "Você recebeu 50 Egos — De Helena Duarte, em <data>." com o motivo, saldo 50 e a linha "De Helena Duarte +50".
10. Abra uma segunda aba da Ana e envie mais 5. Esperado: as duas abas recebem o aviso.
11. O aviso some em cerca de 10 s, pausa com o mouse em cima ou com foco e fecha no X.

**Validações na tela (professor)**

12. Enviar vazio. Esperado: Aluno "Escolha o aluno.", Valor "Informe o valor.", Motivo "Escreva o motivo do envio." e o foco em Aluno. Clique em Valor e digite `50`: o texto inteiro fica em Valor e o foco não volta para Aluno.
13. Valor `0`, `-5`, `1.5` e `1,5`. Esperado: "Use um número inteiro maior que zero."
14. Valor `5000` (acima do saldo). Esperado: "O valor passa do seu saldo.", com o foco em Valor. Nada é enviado, e a aba da Ana não recebe aviso.
15. Motivo só com espaços. Esperado: "Escreva o motivo do envio." O campo para em 500 caracteres (contador 500/500).
16. Aluno de outra instituição, aluno inexistente, motivo de 501 caracteres e saldo insuficiente no servidor só dá para testar pela API (a tela filtra antes). Veja a seção 6. Em nenhum desses casos o saldo muda ou chega aviso.

**Extratos**

17. Helena: envios ("Para <aluno>", −valor, data, motivo) e a cota, do mais recente para o mais antigo. Ana: só entradas ("De Helena Duarte", +valor, motivo). Confira com os `curl` de extrato: devem bater com a tela.
18. Entre como Ricardo Moraes e envie 10 para a Ana. Esperado: a Ana recebe "De Ricardo Moraes", e o extrato da Helena não muda.

**Reconexão**

19. Com as abas da Helena e da Ana abertas, pare a API (Ctrl+C). Esperado: em poucos segundos, as duas mostram "Reconectando…".
20. Suba a API e, antes de as abas reconectarem, envie 7 por `curl`. Esperado: ao reconectar (a espera cresce de 1 s até 30 s), as abas voltam a "Ao vivo", a linha de 7 aparece pela recarga do extrato, sem aviso, e os saldos batem com a API. O envio seguinte chega ao vivo, com aviso.

**Gestão**

21. `/alunos`: ordem pt-BR, com o saldo em cada linha. Editar Bruno (nome e endereço): o CPF não pode ser editado, senha em branco mantém a atual, salvar mostra "Cadastro de … atualizado." e o saldo não muda. Com o diálogo aberto e uma edição pela metade, clique fora da caixa: **não fecha**. Esc e Fechar fecham.
22. Excluir Ana (já recebeu). Esperado, dentro do diálogo: "Ana Lima já recebeu Egos e não pode ser excluído: o histórico dos professores depende dele." Só aparecem Fechar e Cancelar, e a Ana continua na lista.
23. Excluir Carla (sem transações). Esperado: some da lista, com o aviso "Cadastro de Carla Dias excluído."
24. `/empresas`: editar e excluir a empresa criada.

**CPF inexistente, troca de perfil e 404**

25. `/aluno/11144477735` e `/professor/11144477735`. Esperado: "Aluno não encontrado" ou "Professor não encontrado", com o botão "Escolher perfil". Em Network, nenhum WebSocket e nenhum GET de extrato: só o GET da pessoa, com 404.
26. `/aluno/123.456.781-43` (com máscara). Esperado: abre o painel da Ana.
27. "Trocar perfil". Esperado: `/entrar?perfil=professor` (ou `aluno`), com a aba já selecionada e sem erro no console. Em dev, o StrictMode pode registrar o aviso "WebSocket is closed before the connection is established", que é esperado.
28. `/qualquer-coisa`. Esperado: página 404.

**375 px (DevTools, modo celular)**

29. `/entrar`, os dois painéis, `/cadastro`, `/empresa/cadastro`, `/alunos` e `/empresas`: sem rolagem lateral, com botões de 44 px.
30. No painel do aluno a 375 px, faça 3 envios seguidos pela API. Esperado: 1 aviso por vez, o mais novo. No desktop, até 3.

## 6. Comandos equivalentes (API e WebSocket)

```bash
A=localhost:8080; P=48291573646; ANA=12345678143; CARLA=34567890337
H='Content-Type: application/json'

# Envio ok (201, TransacaoResponse)
curl -s -H "$H" -X POST $A/professores/$P/transacoes -d "{\"cpfAluno\":\"$ANA\",\"valor\":50,\"motivo\":\"Explicou recursão\"}"
# CPF com máscara no caminho e no corpo (201)
curl -s -H "$H" -X POST $A/professores/482.915.736-46/transacoes -d '{"cpfAluno":"123.456.781-43","valor":5,"motivo":"com máscara"}'

# 400 DADOS_INVALIDOS: corpo vazio aponta cpfAluno, valor e motivo
curl -s -H "$H" -X POST $A/professores/$P/transacoes -d '{}'
# 400 valor: 0 ("maior que zero"), 1.5 e "abc" ("Use um número inteiro.")
curl -s -H "$H" -X POST $A/professores/$P/transacoes -d "{\"cpfAluno\":\"$ANA\",\"valor\":1.5,\"motivo\":\"x\"}"
# 400 motivo: só espaços, ou 501 caracteres ("Use até 500 caracteres.")
curl -s -H "$H" -X POST $A/professores/$P/transacoes -d "{\"cpfAluno\":\"$ANA\",\"valor\":5,\"motivo\":\"$(printf 'a%.0s' {1..501})\"}"
# 404 PROFESSOR_NAO_ENCONTRADO
curl -s -H "$H" -X POST $A/professores/11144477735/transacoes -d "{\"cpfAluno\":\"$ANA\",\"valor\":5,\"motivo\":\"x\"}"
# 400 ALUNO_NAO_ENCONTRADO (campo cpfAluno)
curl -s -H "$H" -X POST $A/professores/$P/transacoes -d '{"cpfAluno":"11144477735","valor":5,"motivo":"x"}'
# 422 ALUNO_DE_OUTRA_INSTITUICAO (campo cpfAluno)
curl -s -H "$H" -X POST $A/professores/$P/transacoes -d "{\"cpfAluno\":\"$CARLA\",\"valor\":5,\"motivo\":\"x\"}"
# 422 SALDO_INSUFICIENTE (campo valor, "Você tem N disponíveis.")
curl -s -H "$H" -X POST $A/professores/$P/transacoes -d "{\"cpfAluno\":\"$ANA\",\"valor\":5000,\"motivo\":\"x\"}"

# Extratos (e 404 com o código do perfil)
curl -s $A/professores/$P/extrato
curl -s $A/alunos/$ANA/extrato
curl -s $A/alunos/11144477735/extrato

# Professores
curl -s $A/professores
curl -s $A/professores/482.915.736-46

# 409 ALUNO_COM_TRANSACOES / 204 sem transações
curl -s -X DELETE $A/alunos/$ANA
curl -s -o /dev/null -w '%{http_code}\n' -X DELETE $A/alunos/$CARLA

# Handshake: 101 aceito, 403 recusado (perfil errado, perfil inválido, origem de fora)
WS='-s -i --max-time 2 -H Connection:Upgrade -H Upgrade:websocket -H Sec-WebSocket-Version:13 -H Sec-WebSocket-Key:dGhlIHNhbXBsZSBub25jZQ=='
curl ${=WS} "$A/ws/notificacoes?perfil=aluno&cpf=$ANA" | head -1                                  # 101
curl ${=WS} "$A/ws/notificacoes?perfil=professor&cpf=$ANA" | head -1                              # 403
curl ${=WS} "$A/ws/notificacoes?perfil=admin&cpf=$ANA" | head -1                                  # 403
curl ${=WS} -H 'Origin: http://evil.example' "$A/ws/notificacoes?perfil=aluno&cpf=$ANA" | head -1  # 403
```

(`${=WS}` é para zsh; no bash, use `$WS` sem aspas.)

Ouvir os avisos. Use dois terminais e depois faça um envio com o `curl` acima:

```bash
websocat "ws://localhost:8080/ws/notificacoes?perfil=aluno&cpf=12345678143"
websocat "ws://localhost:8080/ws/notificacoes?perfil=professor&cpf=48291573646"
```

Sem websocat (não está instalado aqui), com o WebSocket nativo do Node 22+:

```bash
node -e "const w=new WebSocket('ws://localhost:8080/ws/notificacoes?perfil=aluno&cpf=12345678143'); w.onmessage=(e)=>console.log(e.data)"
```

Esperado: `{"tipo":"MOEDAS_RECEBIDAS","transacao":{…},"saldo":<saldo novo do aluno>}` e `{"tipo":"ENVIO_CONFIRMADO","transacao":{…},"saldo":<saldo novo do professor>}`. Um envio recusado não gera mensagem.

## 7. Testes automatizados

Última execução (05/11, verificação final): `cd backend && ./mvnw test` com JDK 21 deu **78 testes, 0 falhas, 0 erros, BUILD SUCCESS**. Antes do trabalho eram 33. `npm run build` passou (só o aviso de chunk acima de 500 kB, que já existia). `npm run lint` deu 0 erros e 2 avisos que já existiam em arquivos da landing (`WavesStage.tsx`, `ui.tsx`).

| Classe | Testes | O que prova |
| :--- | :-: | :--- |
| `controller/TransacaoControllerTest` | 17 | Envio debita e credita, e a soma dos saldos não muda. Gasta o saldo inteiro. Recusa valor 0, negativo, ausente, com fração ou de outro tipo, sem debitar. Recusa motivo em branco, de outro tipo ou com 501 caracteres. Exige cpfAluno. Aluno inexistente dá 400, aluno de outra instituição 422, saldo insuficiente 422 com o saldo na mensagem e professor inexistente 404. Extratos em ordem e com os campos certos, 404 por perfil, 409 ao excluir aluno com transações. |
| `service/TransacaoConcorrenciaTest` | 4 | Envios simultâneos do mesmo professor nunca deixam o saldo negativo. Vários professores creditando o mesmo aluno não perdem crédito. Editar o aluno durante um envio não apaga o crédito. Excluir durante um envio não esbarra na FK. |
| `notificacao/NotificacaoWebSocketTest` | 10 | Aluno e professor recebem cada um a sua mensagem, com a transação e o saldo. Duas abas recebem. Envio recusado e rollback não enviam nada. O handshake recusa perfil inválido ou CPF de outro perfil, aceita a origem do front e recusa as não configuradas. |
| `service/CotaSemestralServiceTest` | 7 | A subida credita uma vez. O semestre novo soma 1.000 sem zerar. Rodar duas vezes credita uma vez. Viradas 30/06→01/07 e 31/12→01/01 no fuso de São Paulo. Datas em 2099/2100, para não quebrar com o tempo. |
| `controller/ProfessorControllerTest` | 4 | Lista os 4 por nome, com cota e sem senha. Busca com e sem máscara. 404. CPFs válidos, login `nome.sobrenome` e senha em BCrypt. |
| `config/DadosIniciaisTest` | 1 | A carga não cria professor com login que já é de um aluno: a subida para. |
| `controller/AlunoControllerTest` | 17 | O CRUD antigo, mais "recusa login que já pertence a um professor". |
| `controller/ErrosDaApiTest` | 4 | 404, 405, JSON quebrado (CORPO_INVALIDO sem campo) e 415. |
| `EmpresaControllerTest`, `ListasDoCadastroTest`, `DocumentosTest` | 8 + 2 + 4 | Os testes antigos, sem mudança de comportamento. |

O frontend não tem test runner, e a spec proíbe dependência nova. As autoverificações de `forms/regras.ts` e `painel/regras.ts` (com `node --experimental-strip-types`) ficaram no scratchpad, fora do repo.

Verificação final no navegador (05/11): backend na 8093 com uma cópia nova de `egon.mv.db` e vite na 5393, no Chrome headless. Entrar → Professor → Helena, com a Ana aberta em outra aba. Envio de 50: aviso "Envio confirmado … Saldo restante: 950 Egos." na Helena e "Você recebeu 50 Egos / De Helena Duarte" na Ana, com os saldos 950 e 50 e os extratos na tela iguais aos da API. 5000 na tela deu "O valor passa do seu saldo."; pela API, 422 SALDO_INSUFICIENTE. Nenhuma mensagem chegou à Ana nas recusas. "Trocar perfil" levou a `/entrar?perfil=professor`, com 0 erros e 0 avisos no console. A 375 px: scrollWidth 375 e 1 aviso por vez. Pelo curl, também conferi: 1.5 e "abc" (400 com `campos.valor`), 0, motivo vazio, 501 caracteres, `{}`, outra instituição (422), aluno inexistente (400), professor inexistente (404), máscara (201), 409 e 204 no DELETE, e o handshake (101/403). As fotos ficaram no scratchpad da sessão, em `final/shots/`.

## 8. Falhas e limites conhecidos

O que não foi corrigido:

- **Estouro de int em `valor`** (`99999999999`): responde 400 `CORPO_INVALIDO` com `campos` vazio, sem apontar o campo (confirmei na verificação final). A causa é um erro de parse do jackson-core. Para apontar o campo, seria preciso apontar campo também em JSON quebrado. A tela não chega lá, porque valida antes.
- **A landing fica em branco sem WebGL** (`components/PatternWaves.tsx`, `new Renderer` lança exceção e não há error boundary). Não foi mexido porque a spec manda manter a landing como está. A correção mínima seria um try/catch em volta do `new Renderer(...)`.
- **Já acertados depois da verificação**: o comentário de `frontend/src/painel/regras.ts:46`, o exemplo de data (agora com fração) em `frontend/src/lib/api.ts:54` e `.impeccable/design.json` (borda 40%/60%, placeholder 50%, como o `DESIGN.md` e o código). Build e lint rodados de novo: ok.
- **Diagramas PNG** não regerados (draw.io indisponível). A lista do que falta está no Modelo de Dados, em "Mudanças pendentes nos diagramas".
- **Sem login**: qualquer um envia Egos como qualquer professor pela API, e quem souber um CPF ouve os avisos dele por WebSocket. Clientes sem cabeçalho Origin passam pela checagem de origem (divergência 12).
- **Subida bloqueada de propósito**: se um banco antigo tiver aluno ou empresa com o login de um dos 4 professores e a tabela de professores estiver vazia, a API não sobe até que esse login seja trocado.
- **409 pela API** ainda diz "moedas" (`ApiException`). A tela de alunos troca o texto por "Egos".
- Escolhas aceitas no front: (1) com qualquer erro no diálogo de exclusão, o botão de confirmar some, então, sem conexão, tentar de novo exige Cancelar e abrir de novo. (2) Quem estiver com 3 avisos e reduzir a janela para menos de 640 px continua vendo os 3 até chegar o próximo. (3) O painel busca a pessoa e só depois o extrato (duas idas em sequência). (4) `shortcut:` em `painel/usePainel.ts:36`: um aviso que chegue entre a leitura e a resposta da recarga do extrato some da lista até a próxima recarga (o aviso aparece). A janela é de milissegundos e nunca foi reproduzida.
- O bundle único tem mais de 500 kB e leva o WebGL da landing para as telas do app (só pesa no primeiro carregamento). Performance não foi medida.
- Timeout de trava do H2 sob contenção muito alta cairia em 500 `ERRO_INTERNO`. Com 200 envios simultâneos, não aconteceu.

O que não tem teste automatizado:

- O frontend inteiro (sem test runner): telas, `useNotificacoes` (reconexão), `useLiveNotices`, foco e diálogo. Foi tudo conferido à mão ou por scripts CDP no scratchpad.
- No backend: a falha de envio numa sessão não afetar as outras (há um try/catch por sessão, sem teste), a sessão fechada sair do mapa e o cron disparar de fato (só a lógica do semestre é testada, com `Clock` fixo).
- A ordem das mensagens no socket: o front já corrige a inversão comparando datas, mas a prova ficou só num script do scratchpad.
- `NotificacaoWebSocketTest` espera 200 ms depois do handshake. Numa máquina muito lenta, pode falhar de vez em quando (com 0 ms passou 3 de 3).

## 9. Arquivos

`git status --porcelain` (só leitura). Este `REVISAO.md` também é novo.

Alterados (M):
`PRODUCT.md`, `README.md`, `docs/README.md`, `docs/Sprint 1/Historias de Usuario.md`, `docs/Sprint 2/Modelo de Dados.md`,
`backend/pom.xml`, `backend/src/main/resources/application.properties`,
`backend/src/main/java/com/egon/config/DadosIniciais.java`, `controller/AlunoController.java`, `dto/Campos.java`, `exception/ApiException.java`, `exception/ApiExceptionHandler.java`, `repository/AlunoRepository.java`, `repository/UsuarioRepository.java`, `service/AlunoService.java`,
`backend/src/test/java/com/egon/controller/AlunoControllerTest.java`, `controller/ErrosDaApiTest.java`,
`frontend/vite.config.ts`, `frontend/src/content.ts`, `main.tsx`, `components/Dialog.tsx`, `components/Shell.tsx`, `components/controls.tsx`, `forms/AlunoForm.tsx`, `forms/EmpresaForm.tsx`, `lib/api.ts`, `lib/useFormSubmit.ts`, `pages/Alunos.tsx`, `pages/CadastroAluno.tsx`, `pages/CadastroEmpresa.tsx`, `pages/Empresas.tsx`, `pages/NaoEncontrada.tsx`.

Criados (??):
`DESIGN.md`, `.impeccable/design.json`,
`backend/src/main/java/com/egon/`: `config/TempoConfig.java`, `config/WebSocketConfig.java`, `controller/ProfessorController.java`, `dto/ExtratoResponse.java`, `dto/Movimento.java`, `dto/Notificacao.java`, `dto/PessoaResponse.java`, `dto/ProfessorResponse.java`, `dto/TransacaoRequest.java`, `dto/TransacaoResponse.java`, `model/CotaSemestral.java`, `model/Professor.java`, `model/Transacao.java`, `notificacao/NotificacaoHandler.java`, `notificacao/PerfilInterceptor.java`, `repository/CotaSemestralRepository.java`, `repository/ProfessorRepository.java`, `repository/TransacaoRepository.java`, `service/CotaSemestralService.java`, `service/ProfessorService.java`, `service/TransacaoConfirmada.java`, `service/TransacaoService.java`,
`backend/src/test/java/com/egon/`: `config/DadosIniciaisTest.java`, `controller/ProfessorControllerTest.java`, `controller/TransacaoControllerTest.java`, `notificacao/NotificacaoWebSocketTest.java`, `service/CotaSemestralServiceTest.java`, `service/TransacaoConcorrenciaTest.java`,
`frontend/src/`: `components/Balance.tsx`, `components/LiveNotices.tsx`, `components/fields.tsx`, `components/states.tsx`, `forms/parts.tsx`, `forms/regras.ts`, `forms/useCampos.ts`, `lib/format.ts`, `lib/useLiveNotices.ts`, `lib/useLoad.ts`, `lib/useNotificacoes.ts`, `pages/Entrar.tsx`, `pages/PainelAluno.tsx`, `pages/PainelProfessor.tsx`, `painel/EnviarEgos.tsx`, `painel/Painel.tsx`, `painel/regras.ts`, `painel/usePainel.ts`.

Removidos (D):
`frontend/src/components/Records.tsx`, `components/SignupDone.tsx`, `components/form.tsx`, `lib/documentos.ts`, `lib/useAcademicOptions.ts`.
