import type { ReactNode } from 'react'
import { ArrowClockwiseIcon, CheckCircleIcon } from '@phosphor-icons/react'
import { ActionButton } from './controls'
import { FormAlert } from './fields'

// Padding de uma linha de lista dentro de um Panel `flush`. Use no conteúdo do item (ou no link
// que ocupa a linha inteira) para todas as listas terem o mesmo ritmo.
export const rowClass = 'px-5 py-4 sm:px-7'

type ListStateProps<T> = {
  // null enquanto carrega.
  items: T[] | null
  getKey: (item: T) => string
  // O item é desenhado aqui; ListState já põe o <li>.
  children: (item: T) => ReactNode
  // Texto para leitor de tela enquanto carrega, ex. "Carregando alunos…".
  loading: string
  // A frase deve ensinar o próximo passo; a ação (um ActionLink de contorno) é opcional.
  empty: { text: string; action?: ReactNode }
  error?: string | null
  onRetry?: () => void
  label?: string
}

// Os quatro estados de uma lista: erro, carregando, vazia e cheia. Vai dentro de um Panel `flush`.
export function ListState<T>({ items, getKey, children, loading, empty, error, onRetry, label }: ListStateProps<T>) {
  if (error) {
    return (
      <div className="grid gap-4 p-5 sm:p-7">
        <FormAlert>{error}</FormAlert>
        {onRetry && (
          <div>
            <ActionButton variant="outline" onClick={onRetry}>
              <ArrowClockwiseIcon size={18} aria-hidden />
              Tentar de novo
            </ActionButton>
          </div>
        )}
      </div>
    )
  }

  if (!items) {
    return (
      <>
        <p role="status" className="sr-only">
          {loading}
        </p>
        <ul aria-hidden className="divide-y divide-line">
          {[0.42, 0.3, 0.36].map((w) => (
            <li key={w} className={`${rowClass} grid gap-2.5`}>
              <span className="h-4 rounded-full bg-snow/8 motion-safe:animate-pulse" style={{ width: `${w * 100}%` }} />
              <span className="h-3 w-1/4 rounded-full bg-snow/5 motion-safe:animate-pulse" />
            </li>
          ))}
        </ul>
      </>
    )
  }

  if (items.length === 0) {
    return (
      <div className="flex flex-col items-center gap-5 px-5 py-12 text-center sm:px-7">
        <p className="max-w-[36ch] text-lg">{empty.text}</p>
        {empty.action}
      </div>
    )
  }

  return (
    <ul aria-label={label} className="divide-y divide-line">
      {items.map((item) => (
        <li key={getKey(item)}>{children(item)}</li>
      ))}
    </ul>
  )
}

// Confirmação de sucesso ("Cadastro de Ana atualizado."). Fica sempre montado, com children null
// quando não há aviso: assim o leitor de tela anuncia quando a frase aparece.
export function Notice({ children }: { children: ReactNode }) {
  return (
    <div role="status">
      {children && (
        <p className="flex items-center gap-2.5 text-snow">
          <CheckCircleIcon size={20} weight="fill" aria-hidden className="shrink-0" />
          {children}
        </p>
      )}
    </div>
  )
}
