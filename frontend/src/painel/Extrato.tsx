import type { ReactNode } from 'react'
import {
  ArrowDownLeftIcon,
  ArrowsLeftRightIcon,
  ArrowUpRightIcon,
  CalendarCheckIcon,
  TicketIcon,
  type Icon,
} from '@phosphor-icons/react'
import { coins } from '../content'
import type { ApiError, Movimento } from '../lib/api'
import { dataHora } from '../lib/format'
import { Coin } from '../components/Coin'
import { ListState, rowClass } from '../components/states'
import { nomeDoStatus } from './textos'

type Carga<T> = { data: T | null; error: ApiError | null; reload: () => void }

const fmt = new Intl.NumberFormat('pt-BR')

// Entrada e saída por sinal e direção da seta, nunca por cor (o vermelho é só erro).
const tipos: Record<Movimento['tipo'], { icon: Icon; entrada: boolean }> = {
  RECEBIMENTO: { icon: ArrowDownLeftIcon, entrada: true },
  ENVIO: { icon: ArrowUpRightIcon, entrada: false },
  COTA: { icon: CalendarCheckIcon, entrada: true },
  TRANSFERENCIA_ENVIADA: { icon: ArrowsLeftRightIcon, entrada: false },
  TRANSFERENCIA_RECEBIDA: { icon: ArrowsLeftRightIcon, entrada: true },
  RESGATE: { icon: TicketIcon, entrada: false },
}

function quem(m: Movimento) {
  switch (m.tipo) {
    case 'COTA':
      return `Cota do semestre ${m.semestre}`
    case 'ENVIO':
      return `Para ${m.pessoa?.nome ?? ''}`
    case 'RECEBIMENTO':
      return `De ${m.pessoa?.nome ?? ''}`
    case 'TRANSFERENCIA_ENVIADA':
      return `Repasse para ${m.pessoa?.nome ?? ''}`
    case 'TRANSFERENCIA_RECEBIDA':
      return `Repasse de ${m.pessoa?.nome ?? ''}`
    case 'RESGATE':
      return `${m.motivo ?? ''}, na ${m.pessoa?.nome ?? ''}`
  }
}

// Uma linha do extrato. `acao` é um botão da linha (cancelar um repasse pendente).
export function Linha({ m, acao }: { m: Movimento; acao?: ReactNode }) {
  const { icon: Glyph, entrada } = tipos[m.tipo]
  // Repasse recusado ou cancelado não tirou nada de ninguém: sem sinal, valor riscado.
  const desfeito = m.status === 'RECUSADA' || m.status === 'CANCELADA'
  return (
    <div className={`${rowClass} flex gap-4`}>
      <span aria-hidden className="mt-0.5 grid size-10 shrink-0 place-items-center rounded-full text-ink-soft ring-1 ring-line">
        <Glyph size={18} />
      </span>
      <div className="min-w-0 flex-1">
        <div className="flex items-baseline justify-between gap-x-4">
          <p className="min-w-0 font-medium break-words">{quem(m)}</p>
          <p className="shrink-0">
            <span className="sr-only">
              {desfeito ? 'Sem efeito:' : entrada ? 'Entrada de' : 'Saída de'} {coins(m.valor)}
            </span>
            <span aria-hidden className={`inline-flex items-center gap-1.5 font-medium tabular-nums ${desfeito ? 'text-ink-soft line-through' : ''}`}>
              {desfeito ? '' : entrada ? '+' : '−'}
              {fmt.format(m.valor)}
              <Coin size={16} />
            </span>
          </p>
        </div>
        <p className="text-sm text-ink-soft">
          <time dateTime={m.data}>{dataHora(m.data)}</time>
          {m.origem && <> · de {m.origem.nome}</>}
          {m.status && m.tipo === 'TRANSFERENCIA_ENVIADA' && <> · {nomeDoStatus[m.status]}</>}
        </p>
        {m.cupom ? (
          <p className="mt-2 text-sm">
            Cupom <span className="font-medium tabular-nums tracking-wide">{m.cupom}</span>
          </p>
        ) : (
          m.motivo && <p className="mt-2 leading-relaxed break-words">“{m.motivo}”</p>
        )}
        {acao && <div className="mt-3">{acao}</div>}
      </div>
    </div>
  )
}

type ExtratoListaProps = {
  movimentos: Carga<Movimento[]>
  vazio: string
  loading?: string
  acao?: (m: Movimento) => ReactNode
}

// A lista de movimentos com os quatro estados. Vai dentro de um bloco `flush`.
export function ExtratoLista({ movimentos, vazio, loading = 'Carregando extrato…', acao }: ExtratoListaProps) {
  return (
    <ListState
      items={movimentos.data}
      getKey={(m) => m.id}
      loading={loading}
      empty={{ text: vazio }}
      error={movimentos.error?.message}
      onRetry={movimentos.reload}
      label="Movimentos, do mais recente ao mais antigo"
    >
      {(m) => <Linha m={m} acao={acao?.(m)} />}
    </ListState>
  )
}
