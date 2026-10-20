import { useEffect, useId, useRef, type ReactNode } from 'react'
import { XIcon } from '@phosphor-icons/react'
import { IconButton } from './controls'

type DialogProps = { open: boolean; onClose: () => void; title: string; description?: ReactNode; children: ReactNode }

// <dialog> nativo em modo modal: prende o foco, fecha com Esc e devolve o foco a quem abriu.
// O conteúdo só existe aberto, então um formulário dentro dele recomeça a cada abertura.
export function Dialog({ open, onClose, title, description, children }: DialogProps) {
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
      // Clique no fundo escurecido fecha; dentro da caixa, o alvo é o conteúdo.
      onClick={(e) => e.target === ref.current && onClose()}
      className="m-auto max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-2xl overflow-y-auto rounded-[20px] bg-coal text-snow ring-1 ring-line backdrop:bg-night/80 backdrop:backdrop-blur-sm"
    >
      {open && (
        <div className="p-6 md:p-9">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h2 id={titleId} className="font-display text-2xl font-extrabold tracking-[-0.01em] uppercase">
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
