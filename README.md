# Egon

Sistema de moeda estudantil: professores reconhecem o mérito dos alunos com Egos, a moeda do sistema, e os alunos trocam o saldo por vantagens de empresas parceiras.

- `backend/`: API REST em Java 21 com Spring Boot, JPA e H2 (banco em arquivo, em `backend/data/`).
- `frontend/`: landing page e telas de cadastro em React, TypeScript e Vite.
- `docs/`: histórias de usuário, diagramas e o [modelo de dados](docs/Sprint%202/Modelo%20de%20Dados.md). Veja o [índice](docs/README.md).

## Como rodar

Pré-requisitos: Java 21 e Node 22.22 ou mais recente.

API, em `http://localhost:8080`:

```bash
cd backend && ./mvnw spring-boot:run
```

No Windows, sem Git Bash, use `mvnw.cmd spring-boot:run` dentro de `backend`.

Front-end, em `http://localhost:5173`, com a API atrás do prefixo `/api`:

```bash
cd frontend && npm install && npm run dev
```

## Telas

| Caminho | Tela |
| :--- | :--- |
| `/` | Landing page |
| `/cadastro` | Cadastro de aluno (HU02) |
| `/empresa/cadastro` | Cadastro de empresa parceira (HU03) |
| `/alunos` | Listagem, edição e exclusão de alunos |
| `/empresas` | Listagem, edição e exclusão de empresas |

O login (HU01) e as funcionalidades de moedas e vantagens (HU04 a HU12) ainda não foram implementados; o botão "Entrar" da landing leva, por enquanto, à página de não encontrada.

## Testes

```bash
cd backend && ./mvnw test
```

```bash
cd frontend && npm run build && npm run lint
```
