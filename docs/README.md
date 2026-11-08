# Documentação

Artefatos de engenharia de software do **Egon**, o Sistema de Moeda Estudantil. Para rodar a aplicação, veja o [README da raiz](../README.md).

Os diagramas estão alinhados entre si e com as histórias de usuário; as divergências que existiam e a decisão tomada em cada uma estão em [Modelo de Dados](Sprint%202/Modelo%20de%20Dados.md), que também lista o que a implementação das moedas mudou e ainda falta redesenhar nas imagens. Todas as imagens levam o arquivo do draw.io embutido: para editar, abra o PNG no [diagrams.net](https://app.diagrams.net).

## Sprint 1 (Lab03S01)

1. **[Histórias de Usuário](Sprint%201/Historias%20de%20Usuario.md)**
   - Especificação no padrão *Eu como... quero... para...*, com cenários para cada fluxo e perfil.
   - Matriz de rastreabilidade entre histórias, casos de uso e classes.

2. **[Diagrama de Casos de Uso](Sprint%201/Casos%20de%20Uso.png)**
   - Interações dos atores (Aluno, Professor, Empresa) com o sistema.

3. **[Diagrama de Classes](Sprint%201/Diagrama%20de%20Classes.png)**
   - Entidades de domínio: `Usuario` (abstrata), `Aluno`, `Professor`, `Empresa`, `Instituicao`, `Curso`, `CotaSemestral`, `Transacao`, `Vantagem` (com o enum `TipoVantagem`), `Troca`, `Cupom` e `Notificacao`.

## Sprint 2 (Lab03S02)

1. **[Diagrama de Componentes](Sprint%202/Diagrama%20de%20Componentes.png)**
   - Front-end (telas de login, cadastro, gestão e painéis), back-end em camadas (controllers, services, repositories), banco relacional e a pasta de fotos em disco local. O aviso no app por WebSocket e as telas novas ainda não foram desenhados: as mudanças pendentes estão no Modelo de Dados.
   - Há também uma [versão reduzida](Sprint%202/diagrama-de-componentes-reduzido.png), mais abstrata (View, Controller e Model em blocos de gestão), para leitura rápida.

2. **[Modelo Entidade-Relacionamento](Sprint%202/Mer.drawio.png)**
   - Modelo conceitual.

3. **[Diagrama Relacional](Sprint%202/DiagramaRelacional.png)**
   - Tabelas, chaves e tipos.

4. **[Modelo de Dados](Sprint%202/Modelo%20de%20Dados.md)**
   - Reconciliação dos diagramas com as histórias de usuário: as divergências encontradas e a decisão de cada uma, o esquema relacional, o que já está implementado, o que mudou em cada imagem e o que ainda falta redesenhar.

5. **Implementação: CRUD de aluno e de empresa parceira**
   - API REST em `backend/` (Spring Boot, JPA, H2) e telas em `frontend/` (React, Vite): cadastro público de aluno e de empresa (HU02, HU03) e telas de listagem, edição e exclusão.

6. **Implementação: moedas, extratos e aviso ao vivo**
   - Professores pré-cadastrados (HU04), cota semestral (HU05), envio de Egos com motivo obrigatório (HU06), aviso no app ao vivo por WebSocket para o aluno e o professor (HU07) e extratos do aluno e do professor (HU11, HU12).
   - Sem login (HU01): a tela Entrar escolhe o perfil sem senha. Endpoints, formato dos avisos e decisões em [Modelo de Dados](Sprint%202/Modelo%20de%20Dados.md).

## Rodada 2

- Login para aluno, professor, empresa e admin (HU01), admin com os cadastros (HU04, HU14), vantagens com foto (HU08), moeda por professor com repasse entre alunos (HU13), ids UUID e dashboards em bento. As decisões estão nas linhas 19 a 30 do [Modelo de Dados](Sprint%202/Modelo%20de%20Dados.md).
