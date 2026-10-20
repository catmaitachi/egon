import { encerrarSessao, sessaoAtual, type Sessao } from './sessao'

// Cliente da API do backend. Em dev, /api passa pelo proxy do Vite (vite.config.ts).
const base = import.meta.env.VITE_API_URL ?? '/api'

export type Instituicao = { cnpj: string; nome: string; tipo: string }
export type Curso = { id: number; nome: string }

export type Aluno = {
  cpf: string
  nome: string
  email: string
  rg: string
  endereco: string
  instituicao: Instituicao
  curso: Curso
  login: string
  // Só leitura: muda por transação e troca, não pelo cadastro. Começa em 0 (HU02).
  saldo: number
}

export type AlunoCadastro = {
  nome: string
  email: string
  cpf: string
  rg: string
  endereco: string
  cnpjInstituicao: string
  idCurso: number | null
  login: string
  senha: string
}

// CPF é a chave do aluno e não muda; senha em branco mantém a atual.
export type AlunoEdicao = Omit<AlunoCadastro, 'cpf'>

export type Empresa = { cnpj: string; nome: string; email: string; login: string }
export type EmpresaCadastro = Empresa & { senha: string }
export type EmpresaEdicao = Omit<EmpresaCadastro, 'cnpj'>

export type Professor = {
  cpf: string
  nome: string
  departamento: string
  instituicao: Instituicao
  cursos: Curso[]
  login: string
  saldo: number
}

export type AlunoResumo = { cpf: string; nome: string; curso: string }

export type Envio = { cpfAluno: string; valor: number | null; motivo: string }
export type EnvioFeito = { id: string; data: string; valor: number; motivo: string; aluno: string; saldoProfessor: number }

// Linha de extrato. Os campos que não se aplicam à origem vêm nulos.
export type Lancamento = {
  tipo: 'ENTRADA' | 'SAIDA'
  origem: 'TRANSACAO' | 'TROCA' | 'COTA'
  data: string
  valor: number
  pessoa: string | null
  motivo: string | null
  vantagem: string | null
  empresa: string | null
  codigoCupom: string | null
  semestre: string | null
}
export type Extrato = { saldo: number; lancamentos: Lancamento[] }

export type TipoVantagem = 'DESCONTO' | 'PRODUTO' | 'SERVICO'
export type Vantagem = {
  id: number
  nome: string
  descricao: string
  custo: number
  tipo: TipoVantagem
  fotoUrl: string
  empresa: { cnpj: string; nome: string }
}
export type VantagemCadastro = { nome: string; descricao: string; custo: string; tipo: string; foto: File | null }

export type TrocaFeita = {
  id: string
  data: string
  valor: number
  codigoCupom: string
  vantagem: string
  empresa: string
  saldoAluno: number
}

export type Resgate = {
  id: string
  data: string
  valor: number
  codigoCupom: string
  vantagem: string
  aluno: { nome: string; email: string; curso: string; instituicao: string }
}

export type Aviso = { id: number; assunto: string; descricao: string; data: string; codigoCupom: string | null }

// O backend sempre responde erro como { codigo, mensagem, campos }.
export class ApiError extends Error {
  readonly codigo: string
  readonly campos: Record<string, string>

  constructor(codigo: string, mensagem: string, campos: Record<string, string> = {}) {
    super(mensagem)
    this.codigo = codigo
    this.campos = campos
  }
}

const semConexao = () =>
  new ApiError('SEM_CONEXAO', 'Não foi possível falar com o servidor. Confira se a API está rodando.')

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers()
  // FormData leva o próprio Content-Type, com a fronteira entre as partes.
  if (init?.body && !(init.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  const sessao = sessaoAtual()
  if (sessao) headers.set('Authorization', `Bearer ${sessao.token}`)

  let res: Response
  try {
    res = await fetch(base + path, { ...init, headers })
  } catch {
    throw semConexao()
  }

  if (!res.ok) {
    const body = await res.json().catch(() => null)
    // Token vencido ou de cadastro excluído: a sessão guardada não vale mais.
    if (body?.codigo === 'NAO_AUTENTICADO') encerrarSessao()
    if (body?.codigo) throw new ApiError(body.codigo, body.mensagem, body.campos)
    // Sem corpo da API: 502-504 é o proxy sem conseguir chegar ao backend. Qualquer outro
    // status veio de outro lugar e não deve ser confundido com servidor fora do ar.
    if (res.status >= 502 && res.status <= 504) throw semConexao()
    throw new ApiError('ERRO_HTTP', `O servidor respondeu com um erro inesperado (HTTP ${res.status}).`)
  }
  return res.status === 204 ? (undefined as T) : res.json()
}

const send = (method: string, body?: unknown): RequestInit => ({ method, body: JSON.stringify(body) })

// A API devolve o caminho da foto; a tela precisa dele com o prefixo do proxy.
export const fotoUrl = (v: Vantagem) => base + v.fotoUrl

export const api = {
  auth: {
    entrar: (login: string, senha: string) => request<Sessao>('/auth/login', send('POST', { login, senha })),
    eu: () => request<Omit<Sessao, 'token'>>('/auth/eu'),
  },
  alunos: {
    listar: () => request<Aluno[]>('/alunos'),
    cadastrar: (dados: AlunoCadastro) => request<Aluno>('/alunos', send('POST', dados)),
    atualizar: (cpf: string, dados: AlunoEdicao) => request<Aluno>(`/alunos/${cpf}`, send('PUT', dados)),
    excluir: (cpf: string) => request<void>(`/alunos/${cpf}`, { method: 'DELETE' }),
    extrato: () => request<Extrato>('/alunos/eu/extrato'),
  },
  professores: {
    eu: () => request<Professor>('/professores/eu'),
    alunos: () => request<AlunoResumo[]>('/professores/eu/alunos'),
    extrato: () => request<Extrato>('/professores/eu/extrato'),
  },
  empresas: {
    listar: () => request<Empresa[]>('/empresas'),
    cadastrar: (dados: EmpresaCadastro) => request<Empresa>('/empresas', send('POST', dados)),
    atualizar: (cnpj: string, dados: EmpresaEdicao) => request<Empresa>(`/empresas/${cnpj}`, send('PUT', dados)),
    excluir: (cnpj: string) => request<void>(`/empresas/${cnpj}`, { method: 'DELETE' }),
  },
  transacoes: {
    enviar: (dados: Envio) => request<EnvioFeito>('/transacoes', send('POST', dados)),
  },
  vantagens: {
    listar: (cnpjEmpresa?: string) =>
      request<Vantagem[]>(cnpjEmpresa ? `/vantagens?cnpjEmpresa=${encodeURIComponent(cnpjEmpresa)}` : '/vantagens'),
    cadastrar: (dados: VantagemCadastro) => {
      const form = new FormData()
      form.append('nome', dados.nome)
      form.append('descricao', dados.descricao)
      form.append('custo', dados.custo)
      form.append('tipo', dados.tipo)
      if (dados.foto) form.append('foto', dados.foto)
      return request<Vantagem>('/vantagens', { method: 'POST', body: form })
    },
    remover: (id: number) => request<void>(`/vantagens/${id}`, { method: 'DELETE' }),
  },
  trocas: {
    resgatar: (idVantagem: number) => request<TrocaFeita>('/trocas', send('POST', { idVantagem })),
    resgates: () => request<Resgate[]>('/trocas'),
  },
  avisos: () => request<Aviso[]>('/notificacoes'),
  instituicoes: () => request<Instituicao[]>('/instituicoes'),
  cursos: () => request<Curso[]>('/cursos'),
}
