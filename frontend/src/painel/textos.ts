import { coins } from '../content'
import type { Regras, StatusTransferencia, TipoVantagem } from '../lib/api'

// Textos dos painéis que mais de uma tela usa.

export const nomeDoTipo: Record<TipoVantagem, string> = { DESCONTO: 'Desconto', PRODUTO: 'Produto', SERVICO: 'Serviço' }

export const nomeDoStatus: Record<StatusTransferencia, string> = {
  PENDENTE: 'Esperando aprovação',
  APROVADA: 'Concluído',
  RECUSADA: 'Recusado pelo professor',
  CANCELADA: 'Cancelado',
}

// As regras do professor em uma frase, para o aluno saber antes de pedir.
export function resumoDasRegras(r: Regras) {
  if (!r.permiteRepasse) return 'Não permite repasse.'
  const partes = [r.exigeAprovacao ? 'Precisa da aprovação do professor' : 'Sem aprovação: vai na hora']
  if (r.valorMaximo != null) partes.push(`até ${coins(r.valorMaximo)} por repasse`)
  if (r.limiteMensal != null) partes.push(`até ${coins(r.limiteMensal)} por mês`)
  if (r.mesmoCurso) partes.push('só para colegas do seu curso')
  return `${partes.join(', ')}.`
}
