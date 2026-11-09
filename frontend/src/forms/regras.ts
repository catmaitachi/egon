// As mesmas regras e mensagens dos DTOs do backend (dto/*Request, validation/*), para o erro
// aparecer sem ida ao servidor. O backend continua decidindo: o que só ele sabe (CPF, CNPJ,
// e-mail ou login em uso) chega pela API, no mesmo formato.

export type Erros = Record<string, string>

// Hibernate @Email somado ao EMAIL_FORMATO de dto/Campos: o domínio precisa de um ponto.
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@.]+$/
// Usuario.LOGIN_FORMATO.
const LOGIN = /^[a-z0-9._-]{3,50}$/

const PESOS_CPF_1 = [10, 9, 8, 7, 6, 5, 4, 3, 2]
const PESOS_CPF_2 = [11, 10, 9, 8, 7, 6, 5, 4, 3, 2]
const PESOS_CNPJ_1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
const PESOS_CNPJ_2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]

// Cópia de Documentos.digito: cada caractere vale seu código menos 48 (dígitos 0-9, letras 17-42).
function digito(doc: string, pesos: number[]) {
  const resto = pesos.reduce((soma, peso, i) => soma + (doc.charCodeAt(i) - 48) * peso, 0) % 11
  return resto < 2 ? 0 : 11 - resto
}

const repetido = (doc: string) => new Set(doc).size === 1

export const normalizarCpf = (v: string) => v.replace(/\D/g, '')
export const normalizarCnpj = (v: string) => v.replace(/[^0-9a-z]/gi, '').toUpperCase()

export function cpfValido(cpf: string) {
  if (!/^\d{11}$/.test(cpf) || repetido(cpf)) return false
  return digito(cpf, PESOS_CPF_1) === +cpf[9] && digito(cpf, PESOS_CPF_2) === +cpf[10]
}

// CNPJ alfanumérico: letras só nas 12 primeiras posições; os verificadores são números.
export function cnpjValido(cnpj: string) {
  if (!/^[0-9A-Z]{12}\d{2}$/.test(cnpj) || repetido(cnpj)) return false
  return digito(cnpj, PESOS_CNPJ_1) === +cnpj[12] && digito(cnpj, PESOS_CNPJ_2) === +cnpj[13]
}

// Cada função devolve o primeiro erro do campo, obrigatório antes de formato (como o ApiExceptionHandler).

const texto = (v: string, vazio: string, max: number) =>
  !v.trim() ? vazio : v.trim().length > max ? `Use até ${max} caracteres.` : undefined

function email(v: string, vazio: string) {
  const e = v.trim().toLowerCase()
  if (!e) return vazio
  if (!EMAIL.test(e)) return 'E-mail inválido.'
  if (e.length > 100) return 'Use até 100 caracteres.'
}

function login(v: string) {
  const l = v.trim().toLowerCase()
  if (!l) return 'Escolha um login.'
  if (!LOGIN.test(l)) return 'Use de 3 a 50 letras minúsculas, números, ponto, hífen ou sublinhado.'
}

// @Senha: 8 caracteres a 72 bytes (limite do BCrypt). Na edição, em branco mantém a atual.
function senha(v: string, opcional: boolean) {
  if (!v.trim()) return opcional ? undefined : 'Crie uma senha.'
  if (v.length < 8 || new TextEncoder().encode(v).length > 72) return 'Use de 8 a 72 caracteres (letras acentuadas contam como 2).'
}

function cpf(v: string) {
  const c = normalizarCpf(v)
  if (!c) return 'Informe o CPF.'
  if (!cpfValido(c)) return 'CPF inválido. Confira os 11 dígitos.'
}

function cnpj(v: string) {
  const c = normalizarCnpj(v)
  if (!c) return 'Informe o CNPJ.'
  if (!cnpjValido(c)) return 'CNPJ inválido. Confira os 14 caracteres.'
}

const soErros = (campos: Record<string, string | undefined>) =>
  Object.fromEntries(Object.entries(campos).filter((c): c is [string, string] => c[1] !== undefined))

export type ValoresAluno = Record<
  'nome' | 'email' | 'cpf' | 'rg' | 'endereco' | 'idInstituicao' | 'idCurso' | 'login' | 'senha',
  string
>

// Na edição o CPF não vai (não muda) e a senha é opcional.
export function validarAluno(v: ValoresAluno, edicao: boolean): Erros {
  return soErros({
    nome: texto(v.nome, 'Informe o nome completo.', 100),
    email: email(v.email, 'Informe o e-mail.'),
    cpf: edicao ? undefined : cpf(v.cpf),
    rg: texto(v.rg, 'Informe o RG.', 20),
    endereco: texto(v.endereco, 'Informe o endereço.', 255),
    idInstituicao: v.idInstituicao ? undefined : 'Escolha a instituição.',
    idCurso: v.idCurso ? undefined : 'Escolha o curso.',
    login: login(v.login),
    senha: senha(v.senha, edicao),
  })
}

export type ValoresEmpresa = Record<'nome' | 'cnpj' | 'email' | 'login' | 'senha', string>

export function validarEmpresa(v: ValoresEmpresa, edicao: boolean): Erros {
  return soErros({
    nome: texto(v.nome, 'Informe o nome da empresa.', 100),
    cnpj: edicao ? undefined : cnpj(v.cnpj),
    email: email(v.email, 'Informe o e-mail de contato.'),
    login: login(v.login),
    senha: senha(v.senha, edicao),
  })
}

export type ValoresProfessor = Record<'cpf' | 'nome' | 'email' | 'departamento' | 'idInstituicao' | 'login' | 'senha', string>

export function validarProfessor(v: ValoresProfessor, edicao: boolean): Erros {
  return soErros({
    cpf: edicao ? undefined : cpf(v.cpf),
    nome: texto(v.nome, 'Informe o nome completo.', 100),
    email: email(v.email, 'Informe o e-mail.'),
    departamento: texto(v.departamento, 'Informe o departamento.', 100),
    idInstituicao: v.idInstituicao ? undefined : 'Escolha a instituição.',
    login: login(v.login),
    senha: senha(v.senha, edicao),
  })
}

export function validarInstituicao(v: Record<'cnpj' | 'nome' | 'tipo', string>, edicao: boolean): Erros {
  return soErros({
    cnpj: edicao ? undefined : cnpj(v.cnpj),
    nome: texto(v.nome, 'Informe o nome da instituição.', 100),
    tipo: texto(v.tipo, 'Informe o tipo.', 30),
  })
}

export function validarAdmin(v: Record<'nome' | 'login' | 'senha', string>, edicao: boolean): Erros {
  return soErros({ nome: texto(v.nome, 'Informe o nome.', 100), login: login(v.login), senha: senha(v.senha, edicao) })
}
