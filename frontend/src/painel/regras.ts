import type { Extrato, Movimento, Regras, Transacao } from '../lib/api'

// Regras dos painéis, sem React nem conteúdo: dá para conferir fora do navegador.

// A transação vista de cada lado: entrada para o aluno, saída para o professor.
export function movimentoDe(lado: 'aluno' | 'professor', t: Transacao): Movimento {
  return {
    tipo: lado === 'aluno' ? 'RECEBIMENTO' : 'ENVIO',
    id: t.id,
    data: t.data,
    valor: t.valor,
    motivo: t.motivo,
    pessoa: lado === 'aluno' ? t.professor : t.aluno,
    semestre: null,
    status: null,
    origem: null,
    cupom: null,
  }
}

// O Instant do Java tira os zeros do fim da fração ("32Z", "32.1Z"); completar até 9 casas faz
// a comparação de texto seguir o tempo, sem perder os microssegundos que o Date jogaria fora.
const instante = (iso: string) => {
  const [s, f = ''] = iso.slice(0, -1).split('.')
  return `${s}.${f.padEnd(9, '0')}`
}

// Põe o movimento no lugar dele (mais recente primeiro). Os avisos podem chegar fora da ordem em que
// foram gravados: só o mais recente traz o saldo atual, um atrasado entra no meio sem mexer nele.
// Se o extrato já o tem (a recarga ao conectar trouxe), fica como está: nada conta duas vezes.
export function comMovimento(e: Extrato, m: Movimento, saldo: number): Extrato {
  if (e.movimentos.some((x) => x.id === m.id)) return e
  const i = e.movimentos.findIndex((x) => instante(x.data) <= instante(m.data))
  const pos = i === -1 ? e.movimentos.length : i
  return { ...e, saldo: pos === 0 ? saldo : e.saldo, movimentos: e.movimentos.toSpliced(pos, 0, m) }
}

// A ordem do banco não segue o português (acentos, maiúsculas: "Érico" viria depois de "Zélia").
export const porNome = (a: { nome: string }, b: { nome: string }) => a.nome.localeCompare(b.nome, 'pt-BR')

const semAcento = (s: string) => s.normalize('NFD').replace(/\p{M}/gu, '').toLocaleLowerCase('pt-BR')

// Busca por nome sem acento e sem caixa: "joao" acha "João". Busca vazia acha todos.
export const bate = (nome: string, busca: string) => semAcento(nome).includes(semAcento(busca.trim()))

export type Envio = { idAluno: string; valor: string; motivo: string }

// As regras do backend, antes de ir para a rede. O valor chega como o texto do campo e precisa
// ser inteiro, como o backend exige; checar aqui poupa a ida à rede. O limite de 500 fica no maxLength.
export function validarEnvio({ idAluno, valor, motivo }: Envio, saldo: number) {
  const erros: Record<string, string> = {}
  const n = Number(valor)
  if (!idAluno) erros.idAluno = 'Escolha o aluno.'
  if (!valor.trim()) erros.valor = 'Informe o valor.'
  else if (!Number.isInteger(n) || n <= 0) erros.valor = 'Use um número inteiro maior que zero.'
  else if (n > saldo) erros.valor = 'O valor passa do seu saldo.'
  if (!motivo.trim()) erros.motivo = 'Escreva o motivo do envio.'
  return erros
}

export type Repasse = { destinatarioId: string; professorId: string; valor: string; motivo: string }

// As regras do repasse que dá para conferir sem o servidor: a carteira escolhida, o máximo do
// professor e o saldo dela. Limite mensal e curso o servidor confere (precisam de dados de outros).
export function validarRepasse(r: Repasse, carteira: { saldo: number; regras: Regras } | undefined) {
  const erros: Record<string, string> = {}
  const n = Number(r.valor)
  if (!r.professorId || !carteira) erros.professorId = 'Escolha de qual professor é o repasse.'
  else if (!carteira.regras.permiteRepasse) erros.professorId = 'Este professor não permite repasse.'
  if (!r.destinatarioId) erros.destinatarioId = 'Escolha o colega.'
  if (!r.valor.trim()) erros.valor = 'Informe o valor.'
  else if (!Number.isInteger(n) || n <= 0) erros.valor = 'Use um número inteiro maior que zero.'
  else if (carteira && n > carteira.saldo) erros.valor = 'O valor passa do que você tem deste professor.'
  else if (carteira?.regras.valorMaximo != null && n > carteira.regras.valorMaximo)
    erros.valor = `Este professor permite até ${carteira.regras.valorMaximo} por repasse.`
  if (!r.motivo.trim()) erros.motivo = 'Escreva o motivo do repasse.'
  return erros
}
