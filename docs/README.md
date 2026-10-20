# Documentação

Artefatos de engenharia de software do **Egon**, o Sistema de Moeda Estudantil. Para rodar a aplicação, veja o [README da raiz](../README.md).

Os diagramas estão alinhados entre si e com as histórias de usuário; as divergências que existiam e a decisão tomada em cada uma estão em [Modelo de Dados](Sprint%202/Modelo%20de%20Dados.md). Todas as imagens levam o arquivo do draw.io embutido: para editar, abra o PNG no [diagrams.net](https://app.diagrams.net).

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
   - Front-end (telas de login, cadastro, gestão e painéis), back-end em camadas (controllers, services, repositories), banco relacional e os serviços externos de e-mail e de imagens.

2. **[Modelo Entidade-Relacionamento](Sprint%202/Mer.drawio.png)**
   - Modelo conceitual.

3. **[Diagrama Relacional](Sprint%202/DiagramaRelacional.png)**
   - Tabelas, chaves e tipos.

4. **[Modelo de Dados](Sprint%202/Modelo%20de%20Dados.md)**
   - Reconciliação dos diagramas com as histórias de usuário: as divergências encontradas e a decisão de cada uma, o esquema relacional, os endpoints e regras da API e o que mudou em cada imagem.

5. **Implementação**
   - API REST em `backend/` (Spring Boot, JPA, H2) e telas em `frontend/` (React, Vite) para as 12 histórias: cadastros, login, professores pré-cadastrados, cota semestral, envio de moedas, vantagens com foto, troca com cupom, e-mails e extratos. Endpoints e regras no [Modelo de Dados](Sprint%202/Modelo%20de%20Dados.md#api); como rodar e as contas de teste no [README da raiz](../README.md).
