import { useEffect, useRef, type ReactNode } from 'react'
import { ActionButton } from '../components/controls'
import { Coin } from '../components/Coin'
import { FormAlert } from '../components/fields'

type FormActionsProps = { alert: string | null; sending: boolean; label: string; busyLabel: string; onCancel?: () => void }

// Fim do formulário. O erro geral fica junto do botão, onde a pessoa olha quando envia.
export function FormActions({ alert, sending, label, busyLabel, onCancel }: FormActionsProps) {
  return (
    <div className="grid gap-5">
      {alert && <FormAlert>{alert}</FormAlert>}
      <div className="flex flex-wrap justify-end gap-3">
        {onCancel && (
          <ActionButton variant="outline" onClick={onCancel}>
            Cancelar
          </ActionButton>
        )}
        <ActionButton type="submit" busy={sending} className={onCancel ? '' : 'w-full sm:w-auto'}>
          {sending ? busyLabel : label}
        </ActionButton>
      </div>
    </div>
  )
}

type DoneProps = { title: string; text: ReactNode; children: ReactNode }

// Fecho de um cadastro, no lugar do formulário. O foco vai para o título: o botão de enviar sumiu,
// e assim o leitor de tela anuncia o resultado e o Tab segue para as saídas logo abaixo.
// O h1 é escrito aqui, e não com PageTitle, porque precisa receber foco.
export function Done({ title, text, children }: DoneProps) {
  const heading = useRef<HTMLHeadingElement>(null)

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' })
    heading.current?.focus()
  }, [])

  return (
    <div className="flex flex-col items-center pt-8 text-center sm:pt-12">
      <Coin size={56} />
      <h1
        ref={heading}
        tabIndex={-1}
        className="mt-8 font-display text-3xl leading-[1.05] font-extrabold tracking-[-0.02em] uppercase focus:outline-none md:text-4xl"
      >
        {title}
      </h1>
      <p className="mt-3 max-w-[34rem] text-base leading-relaxed text-ink-soft md:text-lg">{text}</p>
      <div className="mt-8 flex flex-wrap justify-center gap-3">{children}</div>
    </div>
  )
}
