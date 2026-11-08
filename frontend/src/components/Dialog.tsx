import { useEffect, useId, useRef, useState, type ReactNode } from 'react'
import { XIcon } from '@phosphor-icons/react'
import { ActionButton, IconButton } from './controls'
import { FormAlert } from './fields'

type DialogProps = { open: boolean; onClose: () => void; title: string; description?: ReactNode; narrow?: boolean; children: ReactNode }

// <dialog> nativo em modo modal: prende o foco, fecha com Esc e devolve o foco a quem abriu.
// O conteúdo só existe aberto, então um formulário dentro dele recomeça a cada abertura.
export function Dialog({ open, onClose, title, description, narrow = false, children }: DialogProps) {
  const ref = useRef<HTMLDialogElement>(null)
  const titleId = useId()

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog
      ref={ref}
      aria-labelledby={titleId}
      onCancel={(e) => {
        e.preventDefault()
        onClose()
      }}
      // Clique no fundo não fecha: descartaria uma edição pela metade (e arrastar uma seleção de texto
      // para fora da caixa também vira clique no <dialog>). Fecham o Esc e o botão Fechar.
      className={`m-auto max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] overflow-y-auto rounded-[20px] bg-coal text-snow ring-1 ring-snow/25 transition-[opacity,translate] duration-200 ease-out-expo backdrop:bg-night/80 backdrop:backdrop-blur-sm starting:open:translate-y-2 starting:open:opacity-0 motion-reduce:transition-none ${
        narrow ? 'max-w-md' : 'max-w-2xl'
      }`}
    >
      {open && (
        <div className="p-6 md:p-9">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h2 id={titleId} className="font-display text-2xl leading-tight font-extrabold tracking-[-0.01em] uppercase">
                {title}
              </h2>
              {description && <p className="mt-2 text-ink-soft">{description}</p>}
            </div>
            <IconButton label="Fechar" icon={XIcon} onClick={onClose} />
          </div>
          <div className="mt-8">{children}</div>
        </div>
      )}
    </dialog>
  )
}

type ConfirmDialogProps = {
  open: boolean
  title: string
  // O que vai acontecer, em uma frase ("O cadastro de Ana será apagado. Não dá para desfazer.").
  children: ReactNode
  confirmLabel: string
  busyLabel: string
  onCancel: () => void
  // Se rejeitar, a mensagem do erro aparece dentro do diálogo (ex. 409 ALUNO_COM_TRANSACOES).
  onConfirm: () => Promise<void>
}

// Confirmação de ação destrutiva. Quem chama fecha o diálogo (open=false) quando onConfirm der certo.
export function ConfirmDialog({ open, title, children, confirmLabel, busyLabel, onCancel, onConfirm }: ConfirmDialogProps) {
  return (
    <Dialog open={open} onClose={onCancel} title={title} description={children} narrow>
      <ConfirmActions confirmLabel={confirmLabel} busyLabel={busyLabel} onCancel={onCancel} onConfirm={onConfirm} />
    </Dialog>
  )
}

// Separado para o estado recomeçar a cada abertura (o Dialog só monta o conteúdo aberto).
function ConfirmActions({ confirmLabel, busyLabel, onCancel, onConfirm }: Omit<ConfirmDialogProps, 'open' | 'title' | 'children'>) {
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const cancelar = useRef<HTMLButtonElement>(null)

  // Com erro, o botão de confirmar sai (repetiria a mesma recusa) e o foco, que estava nele, vai para o Cancelar.
  useEffect(() => {
    if (error) cancelar.current?.focus()
  }, [error])

  const confirm = async () => {
    setSending(true)
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
        <ActionButton ref={cancelar} variant="outline" onClick={onCancel}>
          Cancelar
        </ActionButton>
        {!error && (
          <ActionButton variant="danger" onClick={confirm} busy={sending}>
            {sending ? busyLabel : confirmLabel}
          </ActionButton>
        )}
      </div>
    </div>
  )
}
