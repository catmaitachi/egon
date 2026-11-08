import { useId, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react'
import { CaretDownIcon, WarningCircleIcon } from '@phosphor-icons/react'

// Campos em pílula, como os controles da landing. O erro aparece embaixo do campo e fica ligado
// a ele por aria-describedby (junto com a dica), para o leitor de tela ler os dois.

const labelClass = 'mb-2 block font-display text-xs font-semibold tracking-[0.08em] text-ink-soft uppercase'

const controlClass =
  'w-full bg-night text-base font-normal text-snow ring-1 transition-shadow duration-200 placeholder:text-snow/50 focus:ring-2 focus:ring-snow focus-visible:outline-none disabled:opacity-60'

// O fundo do campo (noite) quase não se separa do painel (carvão): o anel é o contorno do campo e
// precisa de 3:1 sobre os dois (WCAG 1.4.11). Neve 40% dá 3,4:1 sobre o carvão.
const ringFor = (error?: string) => (error ? 'ring-danger' : 'ring-snow/40 hover:ring-snow/60')

type FieldBase = { label: string; error?: string; hint?: ReactNode; className?: string }

function useField(id: string | undefined, error?: string, hint?: ReactNode) {
  const auto = useId()
  const fieldId = id ?? auto
  const errorId = `${fieldId}-erro`
  const hintId = `${fieldId}-dica`
  const describedBy = [error && errorId, hint && hintId].filter(Boolean).join(' ') || undefined
  return { fieldId, errorId, hintId, describedBy, invalid: error ? true : undefined }
}

function Notes({ errorId, hintId, error, hint }: { errorId: string; hintId: string; error?: string; hint?: ReactNode }) {
  return (
    <>
      {error && (
        <p id={errorId} className="mt-2 flex items-start gap-1.5 px-5 text-sm text-danger">
          <WarningCircleIcon size={16} weight="bold" aria-hidden className="mt-0.5 shrink-0" />
          {error}
        </p>
      )}
      {hint && (
        <p id={hintId} className="mt-2 px-5 text-sm text-ink-soft">
          {hint}
        </p>
      )}
    </>
  )
}

type TextFieldProps = FieldBase & Omit<InputHTMLAttributes<HTMLInputElement>, 'className'>

export function TextField({ label, error, hint, className = '', id, ...input }: TextFieldProps) {
  const f = useField(id, error, hint)
  return (
    <div className={className}>
      <label htmlFor={f.fieldId} className={labelClass}>
        {label}
      </label>
      <input
        id={f.fieldId}
        aria-invalid={f.invalid}
        aria-describedby={f.describedBy}
        className={`${controlClass} h-12 rounded-full px-5 ${ringFor(error)}`}
        {...input}
      />
      <Notes errorId={f.errorId} hintId={f.hintId} error={error} hint={hint} />
    </div>
  )
}

type TextAreaFieldProps = FieldBase & Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, 'className'>

// Várias linhas: cantos de 20px, porque pílula em bloco alto quebra a forma.
export function TextAreaField({ label, error, hint, className = '', id, rows = 4, ...textarea }: TextAreaFieldProps) {
  const f = useField(id, error, hint)
  return (
    <div className={className}>
      <label htmlFor={f.fieldId} className={labelClass}>
        {label}
      </label>
      <textarea
        id={f.fieldId}
        rows={rows}
        aria-invalid={f.invalid}
        aria-describedby={f.describedBy}
        className={`${controlClass} block resize-y rounded-[20px] px-5 py-3 leading-relaxed ${ringFor(error)}`}
        {...textarea}
      />
      <Notes errorId={f.errorId} hintId={f.hintId} error={error} hint={hint} />
    </div>
  )
}

type SelectFieldProps = FieldBase &
  Omit<SelectHTMLAttributes<HTMLSelectElement>, 'className'> & { placeholder: string; children: ReactNode }

export function SelectField({ label, error, hint, className = '', id, placeholder, children, ...select }: SelectFieldProps) {
  const f = useField(id, error, hint)
  return (
    <div className={className}>
      <label htmlFor={f.fieldId} className={labelClass}>
        {label}
      </label>
      <div className="relative">
        <select
          id={f.fieldId}
          aria-invalid={f.invalid}
          aria-describedby={f.describedBy}
          className={`${controlClass} h-12 cursor-pointer appearance-none rounded-full pr-12 pl-5 ${ringFor(error)}`}
          {...select}
        >
          <option value="" disabled>
            {placeholder}
          </option>
          {children}
        </select>
        <CaretDownIcon size={18} aria-hidden className="pointer-events-none absolute top-1/2 right-5 -translate-y-1/2 text-ink-soft" />
      </div>
      <Notes errorId={f.errorId} hintId={f.hintId} error={error} hint={hint} />
    </div>
  )
}

// Um bloco do formulário: legenda curta e os campos em duas colunas a partir do tablet.
export function FieldGroup({ legend, columns = 2, children }: { legend: string; columns?: 1 | 2; children: ReactNode }) {
  return (
    <fieldset className={`grid gap-5 ${columns === 2 ? 'md:grid-cols-2' : ''}`}>
      <legend className="mb-5 font-display text-sm font-bold tracking-wide uppercase">{legend}</legend>
      {children}
    </fieldset>
  )
}

// Erro que não é de um campo (sem conexão, regra de negócio). Lido na hora pelo leitor de tela.
export function FormAlert({ children }: { children: ReactNode }) {
  return (
    <div role="alert" className="flex items-start gap-3 rounded-[20px] px-5 py-4 text-danger ring-1 ring-danger/50">
      <WarningCircleIcon size={20} weight="bold" aria-hidden className="mt-0.5 shrink-0" />
      <p>{children}</p>
    </div>
  )
}
