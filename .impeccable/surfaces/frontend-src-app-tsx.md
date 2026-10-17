---
version: 1
slug: "frontend-src-app-tsx"
primary_target: "frontend/src/App.tsx"
related_targets: []
---

## Scope

Landing page pública (`frontend/`), modo Persuade. Visitante principal: aluno de instituição conveniada. Ação: Criar conta (`/cadastro`). Professores (Entrar) e empresas (Cadastrar empresa) são caminhos secundários. Sem cabeçalho: cada seção se basta (tela cheia, âncora própria, saída para a próxima) e o hero funciona como índice.

## Direction contract

THESIS: Preto e branco, com uma única cor onde o aluno deve olhar ou clicar. O hero é a única cena (ondas de pontos discretas); o resto da página é preto e limpo, e cada seção entra como um letreiro acendendo. Recusa barra de navegação, fundo colorido contínuo e cards de três colunas.

OWN-WORLD: Preto #0B0A0B, carvão #161415, branco #FBF9FA. Acento único roxo azulado #6A4CFF (profundo #3F25C9), com texto claro por cima (5,1:1), restrito a: botão Criar conta, a palavra Ego/Egos, o símbolo da moeda e o fluxo da trilha. Títulos Tomorrow 600-800 em caixa alta, texto Chakra Petch 300-500. Contornos de 1px branco a 14-40%, pílula para controles, 20px para superfícies. Moeda Ego = Phosphor CoinVertical num disco roxo; no logo EGON ela ocupa o lugar do O. Ícones Phosphor regular/fill/light. Só modo escuro.

STORY: O aluno entende que esforço em aula vira Ego, vê o Ego fluindo do professor até a troca, vê onde gastar e clica em Criar conta.

FIRST VIEWPORT: Fundo Pattern Waves (React Bits) em pontos brancos com pouca opacidade, tingido de roxo no hover de Criar conta. Logo EGON centralizado no alto. Título de 2 linhas com "Ego." em roxo, uma linha de apoio, Criar conta (roxo) e Entrar (contorno branco). No pé, só a faixa de três atalhos (Como funciona, Vantagens, Quem participa); no mobile eles viram pílulas abaixo das ações e o pé leva a "Como funciona ↓".

FORM: Brief-pinned pelo usuário ao longo da conversa, sem rolagem de conceito. Pedidos literais que fixam a forma: "design centralizado"; "sem cabeçalho as sessões devem ser acessíveis por si"; "use Chakra Petch fino para leitura e Tomorrow forte para títulos"; "paleta mais preto e branco com uso de cores em pontos estratégicos"; "trocar a cor de destaque de rosa para um roxo vibrante meio azul"; "background pattern-waves, só que mais sutil"; "mudar o nome de moeda por Ego"; "o ícone da moeda deve ser usado como o O no título Egon"; "animações de surgimento das sessões como entrada de letreiro contra a posição da rolagem"; "remova o enquadramento automático"; "no hero, remova a parte de baixo com reflexo, deixe só a nav". Code-led (sem geração de imagem). Seed key: n/a (pinned by brief). Assinatura: fluxo contínuo de Egos pela trilha das etapas, com as paradas pulsando quando um Ego passa; títulos de seção que descem contra a rolagem acendendo letra a letra; troca de perfil com transição direcional.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
