# Diagramas de Sequência

Realização dos casos de uso do [Diagrama de Casos de Uso](../Casos%20de%20Uso.png): um diagrama por caso de uso, com os objetos e as mensagens que o código da `main` usa para atendê-lo. As classes de domínio que aparecem aqui estão no [Diagrama de Classes](../Diagrama%20de%20Classes.png).

Cada PNG leva o seu diagrama do draw.io embutido; `Diagramas de Sequencia.drawio` tem os 14, um por página.

## Notação

Segue os slides da disciplina (A.08, Modelagem de Interação):

- **Ator** como boneco; **objetos** `:Classe` (ou `nome:Classe`) sublinhados, em caixa amarela; uma **classe** sem sublinhado quando a operação é estática (`CodigoCupom.novo()`).
- **Linha de vida** tracejada e **foco de controle** (a barra) enquanto o objeto executa; o foco aninhado é uma chamada ao próprio objeto.
- **Mensagens numeradas** em hierarquia: `1` é a ação do ator, `1.1` o que a tela chama, `1.1.1` o que o controller chama, e assim por diante.
- **Síncrona**: linha cheia com ponta cheia. **Assíncrona**: linha cheia com ponta aberta (os e-mails, que saem numa thread à parte). **Retorno**: tracejada com ponta aberta, em itálico. **Criação**: tracejada com `«create»`, chegando à caixa do objeto novo.
- **Guarda** `[condição]` e **iteração** `*[condição]` antes do nome da mensagem.

## Diagramas

| # | Caso de uso | Histórias | Caminho no código |
| :-: | :--- | :--- | :--- |
| 1 | [Realizar login](01%20-%20Realizar%20login.png) | HU01 | `Entrar.tsx` → `AuthController.entrar` → `AuthenticationManager` → `ContasService.loadUserByUsername` (aluno, professor, empresa e admin, nessa ordem) → `PasswordEncoder` → `Sessoes.abrir` |
| 2 | [Realizar cadastro](02%20-%20Realizar%20cadastro.png) | HU02 | `CadastroAluno.tsx` → `AlunoController.cadastrar` → `AlunoService.cadastrar` |
| 3 | [Realizar cadastro de empresa](03%20-%20Realizar%20cadastro%20de%20empresa.png) | HU03 | `CadastroEmpresa.tsx` → `EmpresaController.cadastrar` → `EmpresaService.cadastrar` |
| 4 | [Distribuir moedas](04%20-%20Distribuir%20moedas.png) | HU06, HU07 | `EnviarEgos.tsx` → `ProfessorController.enviar` → `TransacaoService.enviar` → evento `TransacaoConfirmada` → depois do commit, `NotificacaoHandler` (aviso no app) e `EmailsDaTransacao` → `EnvioDeEmail` (e-mail ao aluno e ao professor) |
| 5 | [Cadastrar vantagens](05%20-%20Cadastrar%20vantagens.png) | HU08 | `PainelEmpresa.tsx` → `VantagemController.cadastrar` → `VantagemService.cadastrar` → `FotoArmazem.gravar` |
| 6 | [Trocar moedas](06%20-%20Trocar%20moedas.png) | HU09, HU10 | catálogo: `VantagemController.catalogo` → `VantagemService.catalogo`; resgate: `ResgateController.resgatar` → `ResgateService.resgatar` (carteiras da maior para a menor, `TrocaDebito`, `CodigoCupom`, `Cupom`) |
| 7 | [Visualizar saldo](07%20-%20Visualizar%20saldo.png) | HU11, HU12 | `AlunoService.extrato` (soma das carteiras) e `ProfessorService.extrato` (`Professor.getSaldo`) |
| 8 | [Visualizar transações](08%20-%20Visualizar%20transacoes.png) | HU11, HU12 | `AlunoService.extrato` (recebimentos, repasses e resgates) e `ProfessorService.extrato` (envios e cotas) |
| 9 | [Repassar moedas](09%20-%20Repassar%20moedas.png) | HU13 | `RepassarEgos.tsx` → `TransferenciaController.pedir` → `TransferenciaService.pedir` (regras do professor, débito da carteira de origem, repasse direto ou pendente) |
| 10 | [Aprovar repasse](10%20-%20Aprovar%20repasse.png) | HU13 | `Pedidos.tsx` → `TransferenciaController.aprovar` → `TransferenciaService.decidir(APROVADA)`; recusar e cancelar seguem o mesmo caminho, devolvendo os Egos a quem pediu |
| 11 | [Definir regras de repasse](11%20-%20Definir%20regras%20de%20repasse.png) | HU13 | `PainelProfessor.tsx` → `ProfessorController.definirRegras` → `TransferenciaService.definirRegras` → `RegrasMoeda` |
| 12 | [Conferir cupom](12%20-%20Conferir%20cupom.png) | HU10 | `ConferirCupom.tsx` → `ResgateController.buscar` e `confirmar` → `ResgateService` → `Cupom.usar` |
| 13 | [Manter cadastros](13%20-%20Manter%20cadastros.png) | HU04, HU14 | `Gestoes.tsx` → `ProfessorController.cadastrar` → `ProfessorService.cadastrar` → `CotaSemestralService.creditar`. O cadastro de professor representa os CRUDs do admin: instituições, cursos, alunos, empresas e admins seguem o mesmo caminho (controller → service → repositório) |
| 14 | [Acompanhar moedas](14%20-%20Acompanhar%20moedas.png) | HU14 | `PainelAdmin.tsx` → `ResumoController.resumo` → `ResumoService.resumo` (somas dos Egos, envios por mês e por instituição) |

## O que fica de fora

- Os fluxos alternativos (saldo insuficiente, CPF repetido, login errado…) não estão desenhados: viram uma resposta de erro do controller (`ApiException`, tratada pelo `ApiExceptionHandler`) e estão descritos nas histórias.
- Saldo e transações aparecem em diagramas separados porque são casos de uso separados, mas o painel pede os dois numa chamada só (`extrato`), que devolve o saldo e os movimentos juntos.
- Em "Manter cadastros", só o cadastro de professor está desenhado (ver a tabela acima). Em "Aprovar repasse", a recusa e o cancelamento diferem só na decisão e em quem recebe os Egos de volta.
- Decisões e esquema do banco no [Modelo de Dados](../Modelo%20de%20Dados.md).
