---
name: Egon
description: Moeda estudantil em preto e branco, com um único roxo onde o aluno deve olhar ou clicar.
colors:
  night: "#0b0a0b"
  coal: "#161415"
  snow: "#fbf9fa"
  ink-soft: "color-mix(in oklab, #fbf9fa 72%, #0b0a0b)"
  line: "color-mix(in oklab, #fbf9fa 14%, transparent)"
  accent: "#6a4cff"
  accent-deep: "#3f25c9"
  danger: "#ff6b6b"
typography:
  display:
    fontFamily: "Tomorrow, ui-sans-serif, system-ui, sans-serif"
    fontSize: "5.6rem"
    fontWeight: 800
    lineHeight: 1.02
    letterSpacing: "-0.02em"
  headline:
    fontFamily: "Tomorrow, ui-sans-serif, system-ui, sans-serif"
    fontSize: "3.75rem"
    fontWeight: 800
    lineHeight: 1.05
    letterSpacing: "-0.02em"
  title:
    fontFamily: "Tomorrow, ui-sans-serif, system-ui, sans-serif"
    fontSize: "2.25rem"
    fontWeight: 800
    lineHeight: 1.05
    letterSpacing: "-0.02em"
  subtitle:
    fontFamily: "Tomorrow, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1.25rem"
    fontWeight: 700
    lineHeight: 1.15
    letterSpacing: "-0.01em"
  body:
    fontFamily: "Chakra Petch, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 300
    lineHeight: 1.625
  data:
    fontFamily: "Chakra Petch, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
    fontFeature: "tnum"
  label:
    fontFamily: "Tomorrow, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 600
    lineHeight: 1.2
    letterSpacing: "0.08em"
  button:
    fontFamily: "Tomorrow, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 600
    lineHeight: 1
    letterSpacing: "0.025em"
rounded:
  pill: "9999px"
  surface: "20px"
spacing:
  gutter: "16px"
  gutter-wide: "24px"
  stack-sm: "12px"
  stack-md: "20px"
  stack-lg: "32px"
  panel: "28px"
components:
  button-primary:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.snow}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: "0 28px"
    height: "52px"
  button-primary-hover:
    backgroundColor: "{colors.accent-deep}"
    textColor: "{colors.snow}"
  button-outline:
    backgroundColor: "{colors.night}"
    textColor: "{colors.snow}"
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: "0 28px"
    height: "52px"
  button-outline-hover:
    backgroundColor: "{colors.snow}"
    textColor: "{colors.night}"
  button-app:
    typography: "{typography.button}"
    rounded: "{rounded.pill}"
    padding: "0 24px"
    height: "44px"
  button-danger:
    backgroundColor: "{colors.night}"
    textColor: "{colors.danger}"
    rounded: "{rounded.pill}"
    height: "44px"
  input:
    backgroundColor: "{colors.night}"
    textColor: "{colors.snow}"
    typography: "{typography.data}"
    rounded: "{rounded.pill}"
    padding: "0 20px"
    height: "48px"
  panel:
    backgroundColor: "{colors.coal}"
    textColor: "{colors.snow}"
    rounded: "{rounded.surface}"
    padding: "{spacing.panel}"
  tab-selected:
    backgroundColor: "{colors.snow}"
    textColor: "{colors.night}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    height: "44px"
  price-chip:
    backgroundColor: "{colors.snow}"
    textColor: "{colors.night}"
    rounded: "{rounded.pill}"
    padding: "4px 14px 4px 4px"
  coin:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.snow}"
    rounded: "{rounded.pill}"
---

# Design System: Egon

## Overview

**Creative North Star: "O Letreiro Aceso"**

O Egon é uma sala escura onde só uma coisa acende: o Ego. Tudo é preto, carvão e branco; o roxo azulado aparece apenas onde o aluno deve olhar ou clicar (a ação principal, a palavra Ego, a moeda). Os títulos são letreiros: Tomorrow pesada, em caixa alta, que na landing desce contra a rolagem e acende letra a letra. O texto corrido é Chakra Petch fina, técnica sem ser fria.

A landing (modo Persuade) é a referência visual e não muda: seções de tela cheia, uma única cena de fundo (ondas de pontos no hero), superfícies de carvão com contorno de 1px e cantos de 20px, controles em pílula. As telas de app (modo Operate) falam a mesma língua com o volume baixo: mesmos tokens, mesmas pílulas, mesmos contornos, sem cena de fundo, sem entrada coreografada, com densidade e estados completos. A marca vive nos detalhes: a moeda no saldo, o roxo só no botão que a pessoa veio apertar.

Recusa confirmada pelo dono do produto: barra de navegação na landing, fundo colorido contínuo, cards de três colunas iguais, tema claro.

**Key Characteristics:**
- Preto e branco por padrão; uma cor, com função.
- Títulos em caixa alta pesada; texto fino e legível.
- Pílula para tudo que se aperta ou preenche; 20px para tudo que contém.
- Profundidade por camada tonal (preto, carvão) e contorno de 1px, nunca por sombra.
- Movimento ligado a um estado ou à rolagem; `prefers-reduced-motion` sempre respeitado.
- Só modo escuro.

## Colors

Neutros quase puros com um único acento roxo azulado de alta saturação, usado com avareza.

### Primary
- **Ego Violeta** (accent): o botão da ação principal, a palavra Ego/Egos nos títulos, o disco da moeda, a trilha do fluxo. Texto claro por cima tem 5,1:1.
- **Violeta Fundo** (accent-deep): hover e pressionado do botão primário. Nunca aparece em repouso.

### Neutral
- **Noite** (night): fundo de todas as telas e dos campos de formulário.
- **Carvão** (coal): superfícies (painéis, placas de vantagem, diálogo, abas). É a segunda camada; não existe terceira.
- **Neve** (snow): texto principal, contorno em foco, aba selecionada, chip de preço.
- **Tinta Suave** (ink-soft): texto secundário, rótulos, ícones de apoio. 9:1 sobre a noite.
- **Linha** (line): contornos de 1px das superfícies e divisórias de lista. Contornos de controles vão de Neve 40% (repouso) a 60% (hover) e 100% (foco): o campo (noite) quase não se separa do painel (carvão), então o contorno é o limite dele e precisa de 3:1 sobre os dois (3,4:1 sobre o carvão).

### Tertiary
- **Alerta** (danger): só erro de formulário e ação destrutiva (excluir). 6,6:1 sobre o carvão. Não é cor de valor negativo.

### Named Rules
**A Regra do Único Acento.** Numa tela há no máximo um botão roxo. O roxo nunca enfeita: se não é ação principal, Ego ou moeda, é preto, branco ou cinza.

**A Regra do Sinal, Não da Cor.** Entrada e saída de Egos se distinguem por sinal (+ / −) e ícone de direção, nunca por verde e vermelho. O vermelho é só erro.

## Typography

**Display Font:** Tomorrow (com ui-sans-serif, system-ui), pesos 600, 700 e 800.
**Body Font:** Chakra Petch (com ui-sans-serif, system-ui), pesos 300, 400 e 500.

**Character:** Tomorrow é o letreiro: geométrica, larga, sempre em caixa alta nos títulos. Chakra Petch tem o mesmo DNA técnico em corpo fino, para leitura sem cansar.

### Hierarchy
- **Display** (800, 2.4rem no celular até 5.6rem no desktop, 1.02): só o título do hero.
- **Headline** (800, 2rem até 3.75rem, 1.05): títulos de seção da landing, o letreiro que acende.
- **Title** (800, 1.875rem no celular e 2.25rem a partir de 768px, 1.05): título de página nas telas de app. Escala fixa por breakpoint, sem clamp.
- **Subtitle** (700, 1.125rem a 1.25rem, 1.15, caixa alta): título de painel, nome de etapa, nome de vantagem.
- **Body** (300, 1rem a 1.125rem, 1.625): texto corrido e apoio. Medida máxima de 34rem nos parágrafos de destaque, 65ch no geral.
- **Data** (400, 1rem, 1.5, algarismos tabulares): valores de formulário, linhas de lista, extrato. Nomes em linha de lista sobem para 500.
- **Label** (600, 0.75rem, 0.08em, caixa alta): rótulos de campo, abas, cabeçalhos de coluna. Na faixa do hero o espaçamento chega a 0.14em.
- **Button** (600 no primário, 500 no contorno, 0.9375rem na landing e 0.875rem no app, 0.025em, caixa alta).

### Named Rules
**A Regra do Letreiro.** Título é Tomorrow em caixa alta, com tracking negativo (-0.02em) nos grandes. Texto corrido nunca vai em caixa alta.

**A Regra do Algarismo Alinhado.** Todo número que se compara com outro (saldo, valor de movimento, preço) usa `tabular-nums`.

## Layout

Coluna central. A landing é uma pilha de seções de tela cheia (`min-height: 100dvh`), cada uma com âncora própria e uma saída no pé para a próxima; não há cabeçalho. Conteúdo limitado a 64rem (max-w-5xl), texto de destaque a 34rem. Margem lateral de 16px no celular e 24px a partir de 640px. Breakpoints do Tailwind: 640, 768, 1024. A partir de 1024px a trilha "Como funciona" fica horizontal e as vantagens viram grade de 6 colunas; abaixo disso, trilha vertical e lista de preços.

**Telas de app (modo Operate).**
- Moldura: logo EGON no alto. Fluxos públicos (cadastro, entrar, não encontrada) têm o logo centralizado, como no hero. Telas de gestão e painéis têm o logo à esquerda e, à direita, a navegação ou o perfil atual.
- Largura: telas de quem está logado (painéis, gestão) usam a tela até 100rem, com margem lateral de 16px no celular, 24px a partir de 640px e 40px a partir de 1024px (56px a partir de 1536px): nada de faixas vazias dos lados. Fluxos públicos (cadastro, entrar, não encontrada) continuam em 42rem.
- Ritmo: 32px entre o título da página e o conteúdo, 24px entre painéis, 20px entre campos, 12px dentro de grupos.
- Densidade: linhas de lista com 16px a 20px de padding vertical; uma informação principal por linha (nome ou valor) e o resto em Tinta Suave.
- Responsivo estrutural: colunas viram pilha, ações de linha ficam abaixo do conteúdo no celular. Tudo funciona a 375px sem rolagem horizontal.

**Dashboards em bento (`/painel`, um por perfil).**
- Grade de 12 colunas a partir de 1024px, 6 no tablet (768px) e 1 no celular, com 24px entre blocos. No celular os blocos seguem a ordem do código, que é a ordem de importância.
- Cada bloco responde a uma pergunta de quem olha ("quanto ainda posso enviar?"); o título do bloco é essa resposta em poucas palavras. Bloco sem dado real não entra: nada de número inventado ou enfeite.
- O tamanho segue a importância: o bloco do número principal (saldo, Egos em circulação) é o maior da primeira linha. Nunca todos os blocos iguais; nunca três colunas idênticas.
- Um painel por bloco (Carvão, contorno Linha, 20px), nunca um dentro do outro. Só o bloco do número principal ganha o contorno no tom da moeda (Ego Violeta a 50%); nenhum outro bloco usa o acento além da moeda e da ação principal.
- Cada bloco tem os quatro estados (carregando, vazio, erro, cheio) por conta própria: um bloco que falha não derruba os outros.
- Conteúdo do bloco se ajusta à largura dele (container queries, `@container`), não à da tela; trilhas com `minmax(0,1fr)`.
- Ações de bloco (enviar, cadastrar, aprovar) ficam dentro do bloco; no máximo um botão roxo por tela.
- Sem vãos: a partir de 1024px a grade vira colunas (`Coluna`) da mesma altura, e cada coluna tem um bloco elástico que ocupa a sobra. Listas que crescem (extrato, pedidos, vantagens) são `rolar`: ficam com a altura que a outra coluna deixa e rolam por dentro; blocos curtos são `esticar`. As duas colunas terminam sempre na mesma linha.

## Elevation & Depth

Plano. A profundidade vem de duas camadas tonais (noite, carvão) e de contornos de 1px em Linha. Não há sombra de elevação em nenhum componente. As duas luzes do sistema são da moeda: o halo do Ego no fluxo e o pulso da parada quando um Ego passa. O desfoque de fundo (placa) existe só sobre as ondas do hero, para leitura; o fundo do diálogo é noite a 80% com desfoque leve.

### Shadow Vocabulary
- **Halo do Ego** (`box-shadow: 0 0 0 4px color-mix(in oklab, #6a4cff 22%, transparent), 0 0 28px -2px #6a4cff`): só na moeda em movimento ou recém-chegada.
- **Pulso da parada** (`box-shadow: 0 0 0 1.5px #6a4cff, 0 0 26px -2px #6a4cff`, apagando): só nas paradas da trilha.

### Named Rules
**A Regra do Plano.** Superfície não flutua. Se precisa se separar do fundo, ganha carvão e contorno de 1px; nunca sombra.

## Shapes

Dois raios e nenhum outro. Pílula (9999px) para tudo que se aperta ou preenche: botões, campos de uma linha, abas, chips, ícones de ação, moeda. 20px para tudo que contém: painéis, placas, diálogo, aviso, área de texto de várias linhas (pílula em várias linhas quebra a forma). Contorno sempre de 1px; em foco, 2px. Ícones Phosphor em peso regular (light nos tamanhos grandes, fill para o selecionado), nunca misturados com outra biblioteca.

## Components

### Buttons
Contidos e seguros; o roxo é a recompensa de chegar à ação certa.
- **Shape:** pílula (9999px). Altura 52px na landing, 44px nas telas de app.
- **Primary:** fundo Ego Violeta, texto Neve, Tomorrow 600 em caixa alta, padding lateral de 28px (landing) ou 24px (app). Na landing leva seta à direita que avança 4px no hover; o botão de Criar conta do hero é magnético e tinge as ondas.
- **Hover / Focus:** primário escurece para Violeta Fundo em 300ms. Contorno (borda Neve 40%) vira fundo Neve com texto Noite. Pressionado encolhe para 98%. Foco: contorno Neve de 2px, afastado 4px.
- **Danger:** texto e borda Alerta; no hover, fundo Alerta e texto Noite. Só para excluir.
- **Icon button:** círculo de 44px com borda Linha e ícone de 20px em Tinta Suave; hover acende para Neve (ou Alerta no excluir). Nome no `aria-label` e no `title`.
- **Disabled / ocupado:** 60% de opacidade, sem ponteiro; ocupado mostra um anel girando (parado com movimento reduzido) e `aria-busy`.

### Chips
- **Preço:** fundo Neve, texto Noite, Tomorrow 700 com algarismos tabulares, moeda de 18px colada à esquerda.
- **Atalho do hero (celular):** placa com desfoque, borda Neve 30%, 44px de altura.

### Cards / Containers
- **Corner Style:** 20px.
- **Background:** Carvão sobre Noite.
- **Shadow Strategy:** nenhuma (ver Elevation & Depth).
- **Border:** 1px em Linha; no hover das placas clicáveis, Neve 60%.
- **Internal Padding:** 28px nas placas da landing; 24px (celular) a 32px nos painéis de app. Painel nunca dentro de painel.

### Inputs / Fields
- **Style:** pílula de 48px, fundo Noite, borda Neve 40% (60% no hover), texto Chakra Petch 400, placeholder Neve 50% (5,2:1 sobre a noite). Rótulo acima, em Label.
- **Focus:** borda de 2px em Neve, sem contorno extra.
- **Error / Disabled:** borda Alerta e mensagem em Alerta abaixo do campo, com ícone, ligada por `aria-describedby` e `aria-invalid`. O foco vai para o primeiro campo com erro. Desabilitado a 60%.
- **Área de texto:** mesmo vocabulário com cantos de 20px e contador de caracteres na dica.

### Navigation
- **Abas de perfil (landing) e navegação de gestão (app):** trilho de Carvão com borda Linha e 6px de respiro; item em Label, Tinta Suave; selecionado ganha pílula Neve com texto Noite, que desliza entre itens (mola 380/32). Setas, Home e End movem entre abas.
- **Índice do hero:** três atalhos no pé, separados por linhas finas; uma barra de 1px acende no topo do item em hover.
- **Saída de seção:** link discreto no pé de cada seção, com ícone e seta que desce no hover.

### Moeda Ego (signature)
Disco Ego Violeta com o ícone CoinVertical (fill) em Neve a 60% do diâmetro. No logo EGON ocupa o lugar do O. Aparece junto de toda quantia de Egos (saldo, preço, movimento) e é um dos poucos pontos de cor da tela. Halo só quando está em movimento ou acabou de chegar.

### Saldo (app)
Número grande em Tomorrow 800 com algarismos tabulares, moeda ao lado e "Egos" em Label. Quando muda por um aviso ao vivo, o número novo desce no lugar do antigo em 250ms.

### Aviso ao vivo (app)
Placa de Carvão com borda Linha, cantos de 20px, presa ao canto inferior direito (centralizada no celular). Moeda à esquerda, título em Subtitle, detalhe em Tinta Suave e a mensagem do professor entre aspas em Neve. Entra subindo 12px em 200ms, sai em 150ms, fecha sozinho em 10s (pausa com ponteiro ou foco em cima) e tem botão de fechar. A região é `aria-live="polite"`.

### Estados de lista (app)
- **Carregando:** linhas-esqueleto em Neve 6% no formato da lista real, pulsando devagar (paradas com movimento reduzido), e o texto de carregamento só para leitor de tela.
- **Vazio:** frase que ensina o próximo passo e, quando existe, a ação em botão de contorno.
- **Erro:** aviso em Alerta com a mensagem da API e "Tentar de novo".

## Do's and Don'ts

### Do:
- **Do** usar Ego Violeta (#6a4cff) só na ação principal, na palavra Ego/Egos e na moeda; no máximo um botão roxo por tela.
- **Do** pôr título em Tomorrow 800 caixa alta com -0.02em; texto em Chakra Petch 300 (prosa) ou 400 (dados).
- **Do** usar pílula em controles e 20px em superfícies, contorno de 1px em Linha.
- **Do** mostrar toda quantia com a moeda e `coins()` de `content.ts` ("1 Ego", "1.350 Egos"); o nome da moeda só existe lá.
- **Do** dar a toda tela de app os estados carregando, vazio, erro e cheio, foco visível e funcionamento a 375px.
- **Do** declarar o raio de toda superfície focável (classe `rounded-*`): o foco global aplica raio de pílula a quem não declarar.
- **Do** respeitar `prefers-reduced-motion`: o fluxo vira uma moeda parada, o letreiro aparece aceso, o esqueleto para de pulsar.
- **Do** manter transições de app entre 150ms e 300ms com a curva expo (cubic-bezier(0.16, 1, 0.3, 1)).

### Don't:
- **Don't** criar barra de navegação na landing, fundo colorido contínuo ou cards de três colunas iguais.
- **Don't** pôr rótulo pequeno acima do título (eyebrow); o título se basta.
- **Don't** usar verde e vermelho para entrada e saída de Egos; use sinal e ícone.
- **Don't** usar sombra para separar superfícies, nem painel dentro de painel.
- **Don't** repetir a coreografia de entrada da landing nas telas de app; app carrega direto na tarefa.
- **Don't** trazer outra biblioteca de ícones, emoji ou glifo no lugar de ícone Phosphor.
- **Don't** criar tema claro.
