# Egon

Sistema de moeda estudantil: professores reconhecem o mérito dos alunos com Egos, a moeda do sistema, e os alunos trocam o saldo por vantagens de empresas parceiras.

- `backend/`: API REST em Java 21 com Spring Boot, JPA e H2 (banco em arquivo, em `backend/data/`).
- `frontend/`: landing page e telas do sistema em React, TypeScript e Vite.
- `docs/`: histórias de usuário, diagramas e o [modelo de dados](docs/Sprint%202/Modelo%20de%20Dados.md), com os endpoints e o formato dos avisos ao vivo. Veja o [índice](docs/README.md).

## Como rodar

Pré-requisitos: JDK 21 e Node 22.22 ou mais recente. O Lombok não compila no Java 27: se o `java` padrão for mais novo, aponte o `JAVA_HOME` para o JDK 21 antes de rodar o `mvnw` (sem permissão de execução, use `sh mvnw`).

API, em `http://localhost:8080`, com o modo dev (login sem senha pelo código Konami, só para testar):

```bash
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Sem `-Dspring-boot.run.profiles=dev` a API sobe sem o modo dev, como deve ser fora da máquina de quem desenvolve. No Windows, sem Git Bash, use `mvnw.cmd` dentro de `backend`. O banco fica em `backend/data/egon3` e nasce na primeira subida com três instituições, oito cursos, quatro professores e o admin.

Front-end, em `http://localhost:5173`, com a API (inclusive o WebSocket dos avisos) atrás do prefixo `/api`:

```bash
cd frontend && npm install && npm run dev
```

## Contas de teste

| Perfil | Login | Senha |
| :--- | :--- | :--- |
| Admin | `admin` | `admin12345` (em produção, `EGON_ADMIN_SENHA`) |
| Professor | `helena.duarte`, `ricardo.moraes` (Universidade Horizonte), `beatriz.campos` (Vale Verde), `marcelo.tavares` (Aurora) | `professor123` |
| Aluno e empresa | os que você cadastrar em `/cadastro` e `/empresa/cadastro` | a do cadastro |

Com a API em dev, na tela de login, digite ↑ ↑ ↓ ↓ ← → ← → B A (fora dos campos): aparece a lista de todas as contas para entrar com um clique.

## Telas

| Caminho | Tela |
| :--- | :--- |
| `/` | Landing page |
| `/entrar` | Login dos quatro perfis (HU01) |
| `/cadastro`, `/empresa/cadastro` | Cadastro público de aluno (HU02) e de empresa (HU03) |
| `/painel` | Dashboard em bento do perfil logado: aluno (saldo por professor, extrato, repasse, catálogo com resgate, cupons), professor (envio, pedidos de repasse, cota, regras, extrato), empresa (conferir cupom, resgates, vantagens com foto, meus dados), admin (onde estão os Egos, envios por mês, por instituição). `/painel?cupom=…` é o link do QR: a empresa logada cai na conferência com o código preenchido |
| `/admin/instituicoes`, `/admin/cursos`, `/admin/professores`, `/admin/alunos`, `/admin/empresas`, `/admin/admins` | Gestão do admin |

Todas as histórias funcionam: login (HU01), cadastros (HU02 a HU04), cota (HU05), envio e aviso ao vivo (HU06, HU07), vantagens (HU08), resgate com cupom em QR code e código, conferido e confirmado pela empresa (HU09, HU10), extratos (HU11, HU12), repasse entre alunos (HU13) e administração (HU14).

## Testes

```bash
cd backend && ./mvnw test
```

```bash
cd frontend && npm run build && npm run lint
```
