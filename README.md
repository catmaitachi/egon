# Egon

Sistema de moeda estudantil: professores reconhecem o mérito dos alunos com Egos, a moeda do sistema, e os alunos trocam o saldo por vantagens de empresas parceiras.

- `backend/`: API REST em Java 21 com Spring Boot, JPA e H2 (banco e fotos em `backend/data/`).
- `frontend/`: landing page, cadastros, login e os painéis de aluno, professor e empresa, em React, TypeScript e Vite.
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

## Contas para testar

Alunos e empresas se cadastram pelas telas. Professores não: são pré-cadastrados com os dados que a instituição fornece (HU04), e na primeira subida a API cria estes, todos com a senha inicial `professor123`:

| Login | Professor | Instituição |
| :--- | :--- | :--- |
| `helena.duarte` | Helena Duarte, Computação | Universidade Horizonte |
| `ricardo.moura` | Ricardo Moura, Engenharia | Universidade Horizonte |
| `marcos.tavares` | Marcos Tavares, Ciências Sociais Aplicadas | Centro Universitário Vale Verde |
| `lucia.fernandes` | Lúcia Fernandes, Tecnologia | Faculdade de Tecnologia Aurora |

Cada professor recebe 1.000 Egos por semestre (HU05). Um professor só envia Egos para alunos da instituição dele, então, para testar, cadastre o aluno numa delas.

## Telas

| Caminho | Tela | Histórias |
| :--- | :--- | :--- |
| `/` | Landing page | |
| `/cadastro` | Cadastro de aluno | HU02 |
| `/empresa/cadastro` | Cadastro de empresa parceira | HU03 |
| `/entrar` | Login de aluno, professor e empresa | HU01 |
| `/painel/aluno` | Saldo, catálogo de vantagens e troca com cupom, extrato e e-mails recebidos | HU09, HU10, HU11, HU07 |
| `/painel/professor` | Saldo, envio de Egos com mensagem e extrato | HU05, HU06, HU12 |
| `/painel/empresa` | Cadastro de vantagens com foto, resgates com conferência do cupom e e-mails recebidos | HU08, HU10 |
| `/alunos`, `/empresas` | Listagem, edição e exclusão de alunos e empresas | |

## E-mails

Os avisos de moedas recebidas (HU07) e de troca, com o cupom, para o aluno e para a empresa (HU10), ficam registrados e aparecem na aba "E-mails" dos painéis. Para eles também saírem por e-mail de verdade, configure um servidor SMTP em `backend/src/main/resources/application.properties` (propriedades `spring.mail.*`, comentadas no arquivo). Sem SMTP, a API só registra no log que não enviou.

## Testes

```bash
cd backend && ./mvnw test
```

```bash
cd frontend && npm run build && npm run lint
```
