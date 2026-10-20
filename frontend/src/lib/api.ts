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

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response
  try {
    res = await fetch(base + path, {
      ...init,
      headers: init?.body ? { 'Content-Type': 'application/json' } : undefined,
    })
  } catch {
    throw new ApiError('SEM_CONEXAO', 'Não foi possível falar com o servidor. Confira se a API está rodando.')
  }

  if (!res.ok) {
    const body = await res.json().catch(() => null)
    if (body?.codigo) throw new ApiError(body.codigo, body.mensagem, body.campos)
    throw new ApiError('SEM_CONEXAO', 'Não foi possível falar com o servidor. Confira se a API está rodando.')
  }
  return res.status === 204 ? (undefined as T) : res.json()
}

const send = (method: string, body?: unknown): RequestInit => ({ method, body: JSON.stringify(body) })

export const api = {
  alunos: {
    listar: () => request<Aluno[]>('/alunos'),
    cadastrar: (dados: AlunoCadastro) => request<Aluno>('/alunos', send('POST', dados)),
    atualizar: (cpf: string, dados: AlunoEdicao) => request<Aluno>(`/alunos/${cpf}`, send('PUT', dados)),
    excluir: (cpf: string) => request<void>(`/alunos/${cpf}`, { method: 'DELETE' }),
  },
  empresas: {
    listar: () => request<Empresa[]>('/empresas'),
    cadastrar: (dados: EmpresaCadastro) => request<Empresa>('/empresas', send('POST', dados)),
    atualizar: (cnpj: string, dados: EmpresaEdicao) => request<Empresa>(`/empresas/${cnpj}`, send('PUT', dados)),
    excluir: (cnpj: string) => request<void>(`/empresas/${cnpj}`, { method: 'DELETE' }),
  },
  instituicoes: () => request<Instituicao[]>('/instituicoes'),
  cursos: () => request<Curso[]>('/cursos'),
}
