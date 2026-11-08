// Cliente da API do backend. Em dev, /api passa pelo proxy do Vite (vite.config.ts), inclusive o WebSocket.
// A sessão é um cookie HttpOnly; toda escrita leva o token de CSRF lido do cookie XSRF-TOKEN.
const base = import.meta.env.VITE_API_URL ?? '/api'

export type PerfilSessao = 'ALUNO' | 'PROFESSOR' | 'EMPRESA' | 'ADMIN'
export type Sessao = { perfil: PerfilSessao; id: string; nome: string }
// Modo dev (só com a API em dev): uma conta para entrar sem senha.
export type ContaDev = { perfil: PerfilSessao; id: string; nome: string; login: string; detalhe: string }

export type Instituicao = { id: string; cnpj: string; nome: string; tipo: string }
export type InstituicaoCadastro = { cnpj: string; nome: string; tipo: string }
export type InstituicaoEdicao = Omit<InstituicaoCadastro, 'cnpj'>
export type Curso = { id: number; nome: string }

// Referência curta a uma pessoa (ou empresa) dentro de movimentos e avisos: nunca o documento.
export type Pessoa = { id: string; nome: string }

export type Aluno = {
  id: string
  cpf: string
  nome: string
  email: string
  rg: string
  endereco: string
  instituicao: Instituicao
  curso: Curso
  login: string
  // Soma das carteiras: muda por envios e repasses, não pelo cadastro.
  saldo: number
}

export type AlunoCadastro = {
  nome: string
  email: string
  cpf: string
  rg: string
  endereco: string
  idInstituicao: string
  idCurso: number | null
  login: string
  senha: string
}

// O CPF identifica a pessoa e não muda; senha em branco mantém a atual.
export type AlunoEdicao = Omit<AlunoCadastro, 'cpf'>

export type Empresa = { id: string; cnpj: string; nome: string; email: string; login: string }
export type EmpresaCadastro = Omit<Empresa, 'id'> & { senha: string }
export type EmpresaEdicao = Omit<EmpresaCadastro, 'cnpj'>

export type Professor = {
  id: string
  cpf: string
  nome: string
  departamento: string
  instituicao: Instituicao
  login: string
  saldo: number
}
export type ProfessorCadastro = {
  cpf: string
  nome: string
  departamento: string
  idInstituicao: string
  login: string
  senha: string
}
export type ProfessorEdicao = Omit<ProfessorCadastro, 'cpf'>

export type Admin = { id: string; nome: string; login: string }
export type AdminCadastro = { nome: string; login: string; senha: string }

export type Transacao = {
  id: string
  // Instant ISO-8601 em UTC, com fração de segundo, ex. "2026-11-04T15:30:00.123456Z".
  data: string
  valor: number
  motivo: string
  professor: Pessoa
  aluno: Pessoa
}

// Envio de Egos (HU06). null no valor vira "obrigatório" no backend.
export type TransacaoNova = { idAluno: string; valor: number | null; motivo: string }

// O que o professor permite fazer com os Egos dele no repasse entre alunos. null é "sem limite".
export type Regras = {
  permiteRepasse: boolean
  exigeAprovacao: boolean
  valorMaximo: number | null
  limiteMensal: number | null
  mesmoCurso: boolean
}

export type Carteira = { professor: Pessoa; saldo: number; regras: Regras }

export type StatusTransferencia = 'PENDENTE' | 'APROVADA' | 'RECUSADA' | 'CANCELADA'

// O valor é sempre positivo: o sinal vem do tipo. Repasses trazem status e a origem (o professor dos Egos).
export type Movimento = {
  tipo: 'RECEBIMENTO' | 'ENVIO' | 'COTA' | 'TRANSFERENCIA_ENVIADA' | 'TRANSFERENCIA_RECEBIDA' | 'RESGATE'
  id: string
  data: string
  valor: number
  motivo: string | null
  // A outra pessoa: professor no RECEBIMENTO, aluno no ENVIO, colega no repasse, null na COTA.
  pessoa: Pessoa | null
  // Só na COTA, ex. "2026.2".
  semestre: string | null
  status: StatusTransferencia | null
  origem: Pessoa | null
  // Só no RESGATE: o código do cupom (o motivo é o nome da vantagem e a pessoa, a empresa).
  cupom: string | null
}

// Mais recentes primeiro. `carteiras` só vem no extrato do aluno.
export type Extrato = { saldo: number; carteiras: Carteira[]; movimentos: Movimento[] }

export type Transferencia = {
  id: string
  data: string
  valor: number
  motivo: string
  status: StatusTransferencia
  remetente: Pessoa
  destinatario: Pessoa
  professor: Pessoa
  dataDecisao: string | null
}
export type TransferenciaNova = { destinatarioId: string; professorId: string; valor: number | null; motivo: string }

// Para escolher a quem enviar ou repassar: sem documento nem contato.
export type Colega = { id: string; nome: string; curso: string }

export type TipoVantagem = 'DESCONTO' | 'PRODUTO' | 'SERVICO'
export type Vantagem = {
  id: string
  nome: string
  descricao: string
  custo: number
  tipo: TipoVantagem
  fotoUrl: string
  // Quantos alunos já têm saldo para ela (só a contagem).
  alunosComSaldo: number
}
export type VantagemCatalogo = Omit<Vantagem, 'alunosComSaldo'> & { empresa: Pessoa }
export type VantagemDados = { nome: string; descricao: string; custo: string; tipo: TipoVantagem | ''; foto: File | null }

// HU09/HU10: o cupom de um resgate. EMITIDO espera a empresa conferir; USADO já foi trocado.
export type Cupom = {
  codigo: string
  status: 'EMITIDO' | 'USADO'
  data: string
  valor: number
  vantagem: Pessoa
  empresa: Pessoa
  aluno: Pessoa
  dataUso: string | null
}

export type Resumo = {
  // Sempre vale: emitidos = comProfessores + comAlunos + reservados + resgatados.
  egos: { emitidos: number; comProfessores: number; comAlunos: number; reservados: number; resgatados: number }
  // Os últimos 6 meses, o atual por último; mes no formato "2026-11".
  enviosPorMes: { mes: string; quantidade: number; valor: number }[]
  porInstituicao: { id: string; nome: string; alunos: number; professores: number; enviado: number }[]
  contagens: { instituicoes: number; cursos: number; alunos: number; professores: number; empresas: number; vantagens: number }
}

export type EventoTransferencia = 'SOLICITADA' | 'APROVADA' | 'RECUSADA' | 'CANCELADA'

// Mensagens do servidor no WebSocket. `saldo` é sempre o de quem recebe o aviso.
export type Notificacao =
  | { tipo: 'MOEDAS_RECEBIDAS'; transacao: Transacao; saldo: number }
  | { tipo: 'ENVIO_CONFIRMADO'; transacao: Transacao; saldo: number }
  | { tipo: 'TRANSFERENCIA'; evento: EventoTransferencia; transferencia: Transferencia; saldo: number }
  | { tipo: 'CUPOM'; evento: 'EMITIDO' | 'USADO'; cupom: Cupom; saldo: number }

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

// Avisado quando a sessão acaba no meio do uso (expirou, saiu em outra aba, conta excluída).
export const SESSAO_EXPIRADA = 'egon:sessao-expirada'

const semConexao = () =>
  new ApiError('SEM_CONEXAO', 'Não foi possível falar com o servidor. Confira se a API está rodando.')

// O token é lido a cada escrita: o login troca o cookie, e um valor guardado antes ficaria velho.
const tokenCsrf = () => document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/)?.[1]

const escreve = (method = 'GET') => !['GET', 'HEAD', 'OPTIONS'].includes(method.toUpperCase())

async function enviar(path: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers)
  if (typeof init.body === 'string') headers.set('Content-Type', 'application/json')
  if (escreve(init.method)) {
    // Sem cookie ainda (primeira visita): /auth/eu o entrega.
    if (!tokenCsrf()) await fetch(`${base}/auth/eu`, { credentials: 'same-origin' }).catch(() => null)
    headers.set('X-XSRF-TOKEN', decodeURIComponent(tokenCsrf() ?? ''))
  }
  try {
    return await fetch(base + path, { ...init, headers, credentials: 'same-origin' })
  } catch {
    throw semConexao()
  }
}

async function request<T>(path: string, init: RequestInit = {}, repetida = false): Promise<T> {
  const res = await enviar(path, init)
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    if (body?.codigo) {
      // Token de CSRF velho (outra aba entrou ou saiu): busca um novo e tenta uma vez de novo.
      if (body.codigo === 'CSRF_INVALIDO' && !repetida) {
        await fetch(`${base}/auth/eu`, { credentials: 'same-origin' }).catch(() => null)
        return request(path, init, true)
      }
      if (res.status === 401 && !path.startsWith('/auth/')) window.dispatchEvent(new Event(SESSAO_EXPIRADA))
      throw new ApiError(body.codigo, body.mensagem, body.campos ?? {})
    }
    // Sem corpo da API: 502-504 é o proxy sem conseguir chegar ao backend. Outro status
    // veio de outro lugar e não deve ser confundido com servidor fora do ar.
    if (res.status >= 502 && res.status <= 504) throw semConexao()
    throw new ApiError('ERRO_HTTP', `O servidor respondeu com um erro inesperado (HTTP ${res.status}).`)
  }
  return res.status === 204 ? (undefined as T) : res.json()
}

const send = (method: string, body?: unknown): RequestInit => ({ method, body: body === undefined ? undefined : JSON.stringify(body) })

// Vantagem vai em multipart: os campos e a foto no mesmo envio. Sem foto nova, a atual fica (edição).
const formVantagem = (method: string, d: VantagemDados): RequestInit => {
  const form = new FormData()
  form.set('nome', d.nome)
  form.set('descricao', d.descricao)
  form.set('custo', d.custo)
  form.set('tipo', d.tipo)
  if (d.foto) form.set('foto', d.foto)
  return { method, body: form }
}

export const api = {
  auth: {
    // 401 sem sessão (e entrega o cookie de CSRF de qualquer jeito).
    eu: () => request<Sessao>('/auth/eu'),
    entrar: (login: string, senha: string) => request<Sessao>('/auth/login', send('POST', { login, senha })),
    sair: () => request<void>('/auth/logout', { method: 'POST' }),
  },
  alunos: {
    listar: () => request<Aluno[]>('/alunos'),
    buscar: (id: string) => request<Aluno>(`/alunos/${id}`),
    cadastrar: (dados: AlunoCadastro) => request<Aluno>('/alunos', send('POST', dados)),
    atualizar: (id: string, dados: AlunoEdicao) => request<Aluno>(`/alunos/${id}`, send('PUT', dados)),
    // 409 ALUNO_COM_TRANSACOES quando o aluno já recebeu Egos ou repassou.
    excluir: (id: string) => request<void>(`/alunos/${id}`, { method: 'DELETE' }),
    extrato: (id: string) => request<Extrato>(`/alunos/${id}/extrato`),
    colegas: (id: string) => request<Colega[]>(`/alunos/${id}/colegas`),
    repassar: (id: string, dados: TransferenciaNova) =>
      request<Transferencia>(`/alunos/${id}/transferencias`, send('POST', dados)),
    resgatar: (id: string, vantagemId: string) => request<Cupom>(`/alunos/${id}/resgates`, send('POST', { vantagemId })),
    cupons: (id: string) => request<Cupom[]>(`/alunos/${id}/cupons`),
  },
  empresas: {
    listar: () => request<Empresa[]>('/empresas'),
    buscar: (id: string) => request<Empresa>(`/empresas/${id}`),
    cadastrar: (dados: EmpresaCadastro) => request<Empresa>('/empresas', send('POST', dados)),
    atualizar: (id: string, dados: EmpresaEdicao) => request<Empresa>(`/empresas/${id}`, send('PUT', dados)),
    excluir: (id: string) => request<void>(`/empresas/${id}`, { method: 'DELETE' }),
    vantagens: (id: string) => request<Vantagem[]>(`/empresas/${id}/vantagens`),
    cadastrarVantagem: (id: string, d: VantagemDados) =>
      request<Vantagem>(`/empresas/${id}/vantagens`, formVantagem('POST', d)),
    atualizarVantagem: (id: string, vantagem: string, d: VantagemDados) =>
      request<Vantagem>(`/empresas/${id}/vantagens/${vantagem}`, formVantagem('PUT', d)),
    excluirVantagem: (id: string, vantagem: string) =>
      request<void>(`/empresas/${id}/vantagens/${vantagem}`, { method: 'DELETE' }),
    // Os cupons das vantagens desta empresa; o código aceita minúsculas, espaços e sem hífen.
    cupons: (id: string) => request<Cupom[]>(`/empresas/${id}/cupons`),
    buscarCupom: (id: string, codigo: string) => request<Cupom>(`/empresas/${id}/cupons/${encodeURIComponent(codigo.trim())}`),
    confirmarCupom: (id: string, codigo: string) =>
      request<Cupom>(`/empresas/${id}/cupons/${encodeURIComponent(codigo.trim())}/confirmar`, { method: 'POST' }),
  },
  professores: {
    listar: () => request<Professor[]>('/professores'),
    buscar: (id: string) => request<Professor>(`/professores/${id}`),
    cadastrar: (dados: ProfessorCadastro) => request<Professor>('/professores', send('POST', dados)),
    atualizar: (id: string, dados: ProfessorEdicao) => request<Professor>(`/professores/${id}`, send('PUT', dados)),
    excluir: (id: string) => request<void>(`/professores/${id}`, { method: 'DELETE' }),
    extrato: (id: string) => request<Extrato>(`/professores/${id}/extrato`),
    alunos: (id: string) => request<Colega[]>(`/professores/${id}/alunos`),
    enviar: (id: string, dados: TransacaoNova) => request<Transacao>(`/professores/${id}/transacoes`, send('POST', dados)),
    regras: (id: string) => request<Regras>(`/professores/${id}/regras`),
    definirRegras: (id: string, regras: Regras) => request<Regras>(`/professores/${id}/regras`, send('PUT', regras)),
    transferencias: (id: string) => request<Transferencia[]>(`/professores/${id}/transferencias`),
  },
  transferencias: {
    aprovar: (id: string) => request<Transferencia>(`/transferencias/${id}/aprovar`, { method: 'POST' }),
    recusar: (id: string) => request<Transferencia>(`/transferencias/${id}/recusar`, { method: 'POST' }),
    cancelar: (id: string) => request<Transferencia>(`/transferencias/${id}/cancelar`, { method: 'POST' }),
  },
  instituicoes: {
    listar: () => request<Instituicao[]>('/instituicoes'),
    cadastrar: (dados: InstituicaoCadastro) => request<Instituicao>('/instituicoes', send('POST', dados)),
    atualizar: (id: string, dados: InstituicaoEdicao) => request<Instituicao>(`/instituicoes/${id}`, send('PUT', dados)),
    excluir: (id: string) => request<void>(`/instituicoes/${id}`, { method: 'DELETE' }),
  },
  cursos: {
    listar: () => request<Curso[]>('/cursos'),
    cadastrar: (nome: string) => request<Curso>('/cursos', send('POST', { nome })),
    atualizar: (id: number, nome: string) => request<Curso>(`/cursos/${id}`, send('PUT', { nome })),
    excluir: (id: number) => request<void>(`/cursos/${id}`, { method: 'DELETE' }),
  },
  admins: {
    listar: () => request<Admin[]>('/admins'),
    cadastrar: (dados: AdminCadastro) => request<Admin>('/admins', send('POST', dados)),
    atualizar: (id: string, dados: AdminCadastro) => request<Admin>(`/admins/${id}`, send('PUT', dados)),
    excluir: (id: string) => request<void>(`/admins/${id}`, { method: 'DELETE' }),
  },
  vantagens: {
    catalogo: () => request<VantagemCatalogo[]>('/vantagens'),
  },
  resumo: () => request<Resumo>('/admin/resumo'),
  // Só existem com a API em dev (egon.dev=true); fora disso, 404.
  dev: {
    contas: () => request<ContaDev[]>('/dev/contas'),
    entrar: (perfil: PerfilSessao, id: string) => request<Sessao>('/dev/entrar', send('POST', { perfil, id })),
  },
}

// A foto vem por um caminho relativo à API; aqui ganha o prefixo dela.
export const fotoUrl = (v: { fotoUrl: string }) => base + v.fotoUrl

// Endereço do WebSocket de avisos. Quem conecta vem da sessão (o cookie vai junto no handshake).
export function urlNotificacoes() {
  const url = new URL(`${base}/ws/notificacoes`, window.location.href)
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
  return url.toString()
}
