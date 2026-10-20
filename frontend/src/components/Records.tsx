import { useState, type ReactNode } from 'react'
import { ArrowClockwiseIcon, CheckCircleIcon } from '@phosphor-icons/react'
import { ActionButton, ActionLink } from './controls'
import { Dialog } from './Dialog'
import { FormAlert } from './form'
import { Panel } from './Shell'

// Peças comuns às telas de gestão de alunos e de empresas.

type ListStateProps = {
  items: unknown[] | null
  error: string | null
  onRetry: () => void
  loadingText: string
  emptyText: string
  emptyAction: { to: string; label: string }
  children: ReactNode
}

// Carregando, erro, vazio ou a lista. A lista em si vem como children.
export function ListState({ items, error, onRetry, loadingText, emptyText, emptyAction, children }: ListStateProps) {
  if (error) {
    return (
      <div className="grid gap-4">
        <FormAlert>{error}</FormAlert>
        <div>
          <ActionButton variant="outline" onClick={onRetry}>
            <ArrowClockwiseIcon size={18} />
            Tentar de novo
          </ActionButton>
        </div>
      </div>
    )
  }
  if (!items) {
    return (
      <Panel className="px-7 py-10 text-center text-ink-soft">
        <p role="status">{loadingText}</p>
      </Panel>
    )
  }
  if (items.length === 0) {
    return (
      <Panel className="flex flex-col items-center gap-6 px-7 py-14 text-center">
        <p className="text-lg">{emptyText}</p>
        <ActionLink to={emptyAction.to} variant="outline">
          {emptyAction.label}
        </ActionLink>
      </Panel>
    )
  }
  return (
    <Panel>
      <ul className="divide-y divide-line">{children}</ul>
    </Panel>
  )
}

export function Notice({ children }: { children: ReactNode }) {
  return (
    <p role="status" className="flex items-center gap-2.5 text-snow">
      <CheckCircleIcon size={20} weight="fill" className="shrink-0" />
      {children}
    </p>
  )
}

// Um par rótulo/valor na linha da lista.
export function Detail({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex gap-2">
      <dt className="text-ink-soft">{label}</dt>
      <dd>{children}</dd>
    </div>
  )
}

type ConfirmDeleteProps = { name: string | null; onCancel: () => void; onConfirm: () => Promise<void> }

export function ConfirmDelete({ name, onCancel, onConfirm }: ConfirmDeleteProps) {
  return (
    <Dialog
      open={name !== null}
      onClose={onCancel}
      title="Excluir cadastro"
      description={
        <>
          O cadastro e o login de <strong className="font-medium text-snow">{name}</strong> serão apagados. Não dá para desfazer.
        </>
      }
    >
      <ConfirmActions onCancel={onCancel} onConfirm={onConfirm} />
    </Dialog>
  )
}

// Separado para o estado recomeçar a cada abertura (o Dialog só monta o conteúdo aberto).
function ConfirmActions({ onCancel, onConfirm }: Omit<ConfirmDeleteProps, 'name'>) {
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const confirm = async () => {
    setSending(true)
    setError(null)
    try {
      await onConfirm()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Algo deu errado. Tente de novo.')
      setSending(false)
    }
  }

  return (
    <div className="grid gap-6">
      {error && <FormAlert>{error}</FormAlert>}
      <div className="flex flex-wrap justify-end gap-3">
        <ActionButton variant="outline" onClick={onCancel}>
          Cancelar
        </ActionButton>
        <ActionButton variant="danger" onClick={confirm} disabled={sending}>
          {sending ? 'Excluindo…' : 'Excluir'}
        </ActionButton>
      </div>
    </div>
  )
}
