# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

Vite + React + TypeScript em `frontend/`, Tailwind v4, Motion para animação, Phosphor para ícones. Fundo do hero: Pattern Waves do React Bits (WebGL via `ogl`).

## Users

Primário: aluno de graduação de uma instituição conveniada, que chega pela indicação de um professor ou da faculdade e decide se vale criar conta. Secundários: professores pré-cadastrados pela instituição (não se cadastram sozinhos) e empresas parceiras que oferecem vantagens.

## Product Purpose

Sistema de moeda estudantil. Professores reconhecem mérito (participação, dedicação, comportamento) transferindo moedas com uma mensagem obrigatória. Alunos acumulam saldo e trocam por vantagens cadastradas por empresas parceiras, recebendo um cupom por e-mail. Sucesso na landing: o aluno entende o ciclo e cria conta.

## Positioning

A moeda só nasce do reconhecimento de um professor, sempre com justificativa escrita. Não é um programa de pontos por compra.

## Operating Context

- Professor recebe 1.000 moedas por semestre; saldo não usado acumula.
- Toda transferência gera e-mail ao aluno com professor, valor, data e mensagem.
- Resgate debita o saldo, gera cupom com código e envia e-mail ao aluno e à empresa; a troca é presencial, com conferência do código.
- Instituições fornecem a lista de professores e departamentos.

## Capabilities and Constraints

- Cadastro público só para alunos (com CPF, RG, curso e instituição de uma lista) e empresas (CNPJ).
- Login único para aluno, professor e empresa.
- Nome do produto: **Egon**. Nome da moeda: **Ego** (plural "Egos"). Ambos ficam em `frontend/src/content.ts`. No logo, a moeda ocupa o lugar do "O".

## Evidence on Hand

Histórias de usuário e diagramas em `docs/`. Não existem parceiros, números de uso, depoimentos ou vantagens reais: tudo que aparecer como vantagem na landing é exemplo e deve estar rotulado assim.

## Product Principles

1. Reconhecimento primeiro, recompensa depois: a mensagem do professor importa tanto quanto o valor.
2. Pouco texto. O ciclo professor → aluno → vantagem tem que caber num olhar.
3. Nada de números inventados; só fatos das histórias de usuário.

## Accessibility & Inclusion

WCAG AA para texto e controles. Animações respeitam `prefers-reduced-motion`. Interações de hover também funcionam por toque e teclado.
