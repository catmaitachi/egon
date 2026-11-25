# Padrões de projeto (GoF) — funcionalidades escolhidas

Funcionalidades escolhidas (ver `ESCOLHAS.md`):

| Funcionalidade | Padrão GoF | Categoria |
|---|---|---|
| 2.1 Gerenciamento centralizado das configurações | **Singleton** | Criacional |
| 2.3 Controle do ciclo de vida dos resgates | **State** | Comportamental |
| 2.5 Composição de vantagens e benefícios adicionais | **Decorator** | Estrutural |

As três se conectam. O Singleton de configurações fornece o prazo de validade usado pelo State para expirar
cupons e os limites de benefícios usados pelos Decorators. A ação da transição "Confirmado" do State
dispara os comportamentos extras dos Decorators, como o cashback.

---

## 2.1 Gerenciamento centralizado das configurações — Singleton

### Problema identificado

Hoje os parâmetros globais estão espalhados pelo código e fixos em tempo de compilação:

- a cota semestral é uma constante em `CotaSemestralService` (`static final int VALOR = 1000`);
- não existe prazo de validade de cupom (`Cupom` só tem `EMITIDO`/`USADO`);
- os limites de benefícios e as regras gerais de uso das moedas não existem como parâmetro;
- `application.properties` não serve, porque só é lido na subida e não pode ser alterado pelo admin com o
  sistema rodando.

Se cada módulo (cota, resgate, vantagens, transferências) guardasse sua própria cópia desses valores,
uma alteração feita pelo admin chegaria a um módulo e não a outro. Por exemplo, o resgate poderia
calcular a validade com 30 dias enquanto a tarefa de expiração usasse 15.

### Responsabilidades envolvidas

- **`ConfiguracaoSistema`** (o Singleton): guarda os valores vigentes e oferece consulta
  (`cotaSemestral()`, `validadeCupom()`, `percentualMaximoBeneficios()`, …) e atualização
  (`atualizar(novosValores)`), validando cada valor. É a única fonte desses valores no sistema.
- **`ParametrosSistema`** (entidade JPA, tabela com **uma única linha**, id fixo): mantém os valores
  salvos entre reinícios. Só o Singleton lê e grava essa tabela.
- **`ConfiguracaoController`** (`/configuracoes`): `GET` para os perfis autenticados e `PUT` apenas para
  `Perfil.ADMIN`, seguindo a mesma regra de permissão do `AdminController` em `SegurancaConfig`.
- **Módulos clientes** (`CotaSemestralService`, `ResgateService`, `VantagemService`,
  `TransferenciaService`): apenas consultam os valores e nunca guardam cópia deles.

### Justificativa da escolha

O desafio é garantir **uma única instância** responsável pelos valores globais e **um ponto de acesso
global** a ela. Essa é exatamente a intenção do Singleton.

- Com **várias instâncias**, cada uma poderia carregar o banco em um momento diferente, e os módulos
  ficariam inconsistentes depois de uma atualização.
- Uma **classe utilitária estática** (só com `static`) não pode ser injetada nem substituída em testes,
  e não tem como aplicar a permissão de quem pode alterar os valores.
- **Parâmetros em `application.properties`** não podem ser alterados em tempo de execução.

**Forma de implementação no Spring:** não usamos a variante clássica com construtor privado e
`getInstance()` estático. Usamos o **escopo singleton do contêiner** (`@Component`, cujo escopo padrão é
singleton). O contêiner garante uma instância por aplicação e a entrega por injeção de dependência. A
intenção do padrão se mantém (instância única com acesso global), e ainda é possível testar e trocar a
implementação. Isso segue o que o projeto já faz com o `Clock` em `TempoConfig`.

Cuidados no Singleton:

- os valores ficam num objeto imutável (`record Configuracoes`) guardado numa referência `volatile`.
  Uma atualização troca o objeto inteiro, então nenhum leitor vê metade dos valores antigos e metade
  dos novos;
- a atualização grava no banco e, **somente depois do commit**, troca a referência em memória;
- os valores passam a valer para **novas operações**. Um cupom já emitido mantém a validade que tinha
  na emissão, gravada nele (a mesma regra de `RegrasMoeda`: mudar a regra não mexe no que está pendente).

### Integração com a arquitetura existente

```
AdminController/ConfiguracaoController ──PUT──► ConfiguracaoSistema (singleton) ◄──► ParametrosSistema (1 linha)
                                                     ▲   ▲   ▲
                CotaSemestralService ────────────────┘   │   └──────── VantagemService / Decorators (2.5)
                ResgateService / estados (2.3) ──────────┘
```

- `CotaSemestralService` deixa de usar `VALOR` e passa a usar `configuracao.cotaSemestral()`. O valor
  efetivamente creditado continua gravado em `CotaSemestral.valor`, então o histórico do extrato (HU12)
  não muda.
- `DadosIniciais` cria a linha de `ParametrosSistema` com os valores atuais (1000 moedas etc.) na
  primeira subida.
- No front, entra uma tela de configurações no painel do admin.

---

## 2.3 Controle do ciclo de vida dos resgates — State

### Problema identificado

Um resgate passa por **Solicitado → Aguardando utilização → Confirmado**, e pode terminar em
**Cancelado** ou **Expirado**. Cada operação (confirmar, cancelar, expirar) depende do estado atual:

| Estado atual \ operação | gerar cupom | confirmar uso | cancelar | expirar |
|---|---|---|---|---|
| Solicitado | → Aguardando | erro | → Cancelado | erro |
| Aguardando utilização | erro | → Confirmado | → Cancelado | → Expirado |
| Confirmado | erro | erro (*já usado*) | erro | erro |
| Cancelado | erro | erro | erro | erro |
| Expirado | erro | erro (*expirado*) | erro | erro |

Hoje existem só dois estados, e a regra fica num `if` dentro do serviço:

```java
if (cupom.getStatus() == StatusCupom.USADO) { throw ApiException.cupomUsado(); }
cupom.usar(agora());
```

Com cinco estados e quatro operações, essa abordagem vira um `switch`/`if` em cada método de
`ResgateService`, com a mesma tabela acima repetida em vários lugares. Seria fácil, por exemplo, deixar
um resgate confirmado voltar para solicitado.

### Responsabilidades envolvidas

- **Contexto — `Resgate`** (evolução do `Cupom` atual, que já guarda o status, ligado 1-1 à `Troca`).
  Guarda o estado atual e a data de validade, e **delega** cada operação ao objeto de estado:
  `confirmar()`, `cancelar()`, `expirar()`.
- **Estado — interface `EstadoResgate`**: declara as operações e, por padrão, recusa todas
  (`ApiException.transicaoInvalida`).
- **Estados concretos** — `Solicitado`, `AguardandoUtilizacao`, `Confirmado`, `Cancelado`, `Expirado`.
  Cada um sobrescreve **apenas** as transições que permite, define o próximo estado e executa a ação da
  transição:
  - `Solicitado.gerarCupom` → gera o código e calcula a validade (`ConfiguracaoSistema.validadeCupom()`,
    funcionalidade 2.1);
  - `AguardandoUtilizacao.confirmar` → registra `dataUso` e publica `CupomNotificado("USADO")`;
  - `AguardandoUtilizacao.cancelar` / `Solicitado.cancelar` → estorna os Egos para as carteiras de
    origem usando os `TrocaDebito` já registrados;
  - `AguardandoUtilizacao.expirar` → marca como expirado, com estorno se a configuração mandar.
- **`ResgateService`**: localiza e trava o resgate (como já faz com `findBloqueadoByCodigo`), chama a
  operação no contexto e salva o resultado. Não contém mais nenhuma regra de transição.
- **Tarefa agendada de expiração** (`@Scheduled`, como em `CotaSemestralService`): busca os resgates
  aguardando com validade vencida e chama `expirar()`.

### Justificativa da escolha

O comportamento do objeto **muda conforme o estado interno**, e as regras de transição estão espalhadas
em condicionais. Essa é a aplicabilidade descrita para o State:

- cada estado vira uma classe pequena e coesa, e a tabela de transições fica distribuída entre essas
  classes em vez de repetida em `if`s;
- uma transição proibida (Confirmado → Solicitado) simplesmente não existe no código de `Confirmado`;
- para criar um novo estado (por exemplo "Em análise"), basta escrever uma nova classe, sem mexer nos
  outros estados (princípio aberto/fechado).

**Por que não Strategy?** As duas estruturas são parecidas. No Strategy, quem escolhe o algoritmo é o
cliente, e ele não muda sozinho. No State, **o próprio estado decide o próximo estado**, que é
justamente o que a funcionalidade pede.

**Persistência:** o JPA grava o estado como `enum StatusResgate` (coluna `status`, como hoje), e cada
constante devolve o seu objeto de estado (`StatusResgate.CONFIRMADO.estado()`). Assim o banco continua
guardando um texto simples, e o comportamento fica nas classes de estado.

### Integração com a arquitetura existente

- `StatusCupom {EMITIDO, USADO}` passa a ser `StatusResgate {SOLICITADO, AGUARDANDO_UTILIZACAO,
  CONFIRMADO, CANCELADO, EXPIRADO}`, e `Cupom.usar()` sai do modelo, porque passa a ser uma ação do estado.
- `ResgateService.resgatar` cria o resgate em `SOLICITADO` e chama `gerarCupom()` na mesma transação.
  `confirmar` delega ao contexto. Entram `cancelar` e a tarefa de expiração.
- Os eventos `CupomNotificado` e os e-mails (HU10) continuam iguais. Só passam a ser publicados pela
  ação da transição, e entram os eventos `CANCELADO` e `EXPIRADO`.
- `CupomResponse` expõe o novo status e a validade, e o front mostra os cinco estados.

---

## 2.5 Composição de vantagens e benefícios adicionais — Decorator

### Problema identificado

A `Vantagem` atual tem só nome, descrição, custo, tipo e foto. É preciso permitir benefícios extras
**combináveis**: desconto em categoria, entrega grátis, moedas de volta (cashback) e bônus em datas
comemorativas.

- Com **uma subclasse por combinação**, quatro benefícios já geram 2⁴ − 1 = 15 classes, e cada benefício
  novo dobra esse número.
- Com **campos booleanos na `Vantagem`** (`temCashback`, `temFreteGratis`, …), a classe precisa ser
  modificada a cada benefício novo e se enche de `if`s em cada uso.

### Responsabilidades envolvidas

- **Componente — interface `OfertaVantagem`**: o que o resto do sistema sabe de uma vantagem:
  `custo(LocalDate)`, `descricao()`, `beneficios()` (lista legível para o catálogo) e
  `aoConfirmarUso(ContextoUso)` (o gancho executado quando o cupom é confirmado).
- **Componente concreto — `VantagemBase`**: envolve a entidade `Vantagem` atual e devolve os dados dela
  sem alteração.
- **Decorator abstrato — `BeneficioAdicional`**: implementa `OfertaVantagem`, guarda a `OfertaVantagem`
  envolvida e repassa a ela todas as chamadas.
- **Decorators concretos**, cada um acrescentando uma única característica:
  - `DescontoCategoria` → acrescenta à descrição o desconto na categoria;
  - `EntregaGratuita` → acrescenta o benefício à lista;
  - `CashbackMoedas` → em `aoConfirmarUso`, chama o componente e depois credita X Egos ao aluno;
  - `BonusDataComemorativa` → em `custo(data)`, reduz o custo quando a data é comemorativa.
- **`MontadorDeOferta`**: lê os benefícios cadastrados para a vantagem (tabela
  `beneficio_adicional`: vantagem, tipo, parâmetros, ordem) e monta a cadeia de decorators. Respeita os
  limites de `ConfiguracaoSistema` (2.1), por exemplo o percentual máximo de desconto somado.

### Justificativa da escolha

O desafio pede para **acrescentar responsabilidades a um objeto dinamicamente, sem alterar sua classe e
sem uma explosão de subclasses**. Essa é a intenção do Decorator:

- cada benefício é uma classe, e as combinações são feitas por composição em tempo de execução
  (`new CashbackMoedas(new EntregaGratuita(new VantagemBase(v)), 50)`). Com isso bastam 4 classes, não 15;
- como todo decorator implementa a mesma interface, os clientes (catálogo, resgate) tratam uma
  vantagem decorada exatamente como uma simples;
- a `Vantagem` original não muda, e as vantagens já cadastradas continuam funcionando sem benefícios
  (o princípio aberto/fechado é preservado);
- a empresa liga e desliga benefícios por cadastro, sem precisar de deploy.

**Por que não deixar o Decorator na entidade JPA?** Entidades são carregadas e controladas pelo
Hibernate, e envolvê-las diretamente atrapalharia o mapeamento e os proxies. Por isso o Decorator
fica sobre a interface de domínio `OfertaVantagem`, e a entidade continua sendo só dados.

### Integração com a arquitetura existente

- `VantagemService.catalogo()` monta as ofertas pelo `MontadorDeOferta`. Além disso,
  `VantagemCatalogoResponse` ganha `custoEfetivo` e `beneficios`.
- `ResgateService.resgatar` cobra `oferta.custo(hoje)` em vez de `vantagem.getCusto()`. O valor cobrado
  continua gravado em `Troca.valor`, então o histórico não muda se os benefícios mudarem depois.
- A ação da transição **Confirmado** do State (2.3) chama `oferta.aoConfirmarUso(...)`, que é onde o
  cashback credita os Egos.
- O cadastro de vantagens da empresa (HU08) ganha a seção "Benefícios adicionais" (`VantagemRequest`
  passa a ter a lista de benefícios).

---

## Diagramas

Os arquivos ficam em `docs/Sprint 4/`. Cada pasta tem um `.drawio` com os três diagramas (uma página por
funcionalidade) e um PNG por diagrama. Cada PNG leva o seu diagrama do draw.io embutido e pode ser aberto
direto no draw.io para edição.

### Diagramas de Estados

O ciclo de vida do objeto central de cada funcionalidade. A notação é a da UML 2: estado com
compartimento de ações (`entry`, `do` e transições internas), pseudoestado inicial, escolha (losango),
estado final e estado composto. As transições seguem o formato `evento [guarda] / ação`, e `after(...)`
e `when(...)` representam eventos de tempo e de mudança.

| Funcionalidade | Objeto | Diagrama | O que mostra |
|---|---|---|---|
| 2.1 | `ConfiguracaoSistema` | [01 - Configuracoes do sistema](docs/Sprint%204/Diagramas%20de%20Estados/01%20-%20Configuracoes%20do%20sistema.png) | Carregando → Vigente na subida, com falha no banco encerrando a aplicação. Uma atualização passa por Validando (recusa com 400) e Gravando (commit troca a referência; rollback mantém os valores). Consultas e a recusa por perfil (403) são transições internas de Vigente. |
| 2.3 | `Resgate` | [02 - Resgate](docs/Sprint%204/Diagramas%20de%20Estados/02%20-%20Resgate.png) | Os cinco estados do enunciado, com as transições permitidas e as ações de cada uma: débito, geração do cupom, estorno e notificações. Confirmado, Cancelado e Expirado são finais. |
| 2.5 | `OfertaVantagem` | [03 - Oferta de vantagem](docs/Sprint%204/Diagramas%20de%20Estados/03%20-%20Oferta%20de%20vantagem.png) | Simples (só `VantagemBase`) ⇄ Composta (cadeia de decorators). Composta é um estado composto com os subestados Preço normal ⇄ Bônus ativo, conforme a data comemorativa. Adicionar além do limite é recusado. |

### Diagramas de Atividades

O fluxo de execução de cada funcionalidade, com uma raia (partição) por participante, nós de decisão e
de junção com guardas, fork/join, final de atividade e final de fluxo, além dos fluxos alternativos.

| Funcionalidade | Diagrama | Raias | Fluxos alternativos |
|---|---|---|---|
| 2.1 | [01 - Atualizar configuracoes](docs/Sprint%204/Diagramas%20de%20Atividades/01%20-%20Atualizar%20configuracoes.png) | Admin, Front-end, `ConfiguracaoController`, `ConfiguracaoSistema`, módulos clientes | Sem permissão (403); valores inválidos (400, volta para a edição); falha ao gravar (rollback). O fork mostra que, após o commit, a tela confirma e os módulos passam a ler os novos valores. |
| 2.3 | [02 - Ciclo do resgate](docs/Sprint%204/Diagramas%20de%20Atividades/02%20-%20Ciclo%20do%20resgate.png) | Aluno, Sistema, Empresa parceira | Saldo insuficiente; depois de emitido, o que ocorrer primeiro: cancelamento pelo aluno, validade vencida (tarefa diária) ou conferência pela empresa (cupom inválido, cupom vencido → expira, ou confirmação). |
| 2.5 | [03 - Composicao de beneficios](docs/Sprint%204/Diagramas%20de%20Atividades/03%20-%20Composicao%20de%20beneficios.png) | Empresa parceira, Sistema, Aluno | Vantagem de outra empresa; benefício acima do limite (volta para o ajuste); adicionar outro benefício (laço). No uso: custo reduzido ou normal, e cashback só quando houver `CashbackMoedas`. |
