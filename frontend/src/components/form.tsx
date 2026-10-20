import { useId, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes } from 'react'
import { CaretDownIcon, WarningCircleIcon } from '@phosphor-icons/react'

// Campos em pílula, como os controles da landing. O erro aparece embaixo do campo e
// fica ligado a ele por aria-describedby, para o leitor de tela ler junto.

const labelClass = 'mb-2 block font-display text-xs font-semibold tracking-wide text-ink-soft uppercase'

const controlClass =
  'h-12 w-full rounded-full bg-night px-5 text-base text-snow ring-1 transition-shadow duration-200 placeholder:text-snow/40 focus:ring-2 focus:ring-snow focus-visible:outline-none disabled:opacity-60'

type FieldBase = { label: string; error?: string; hint?: string; className?: string }

function useFieldIds(id: string | undefined, error?: string, hint?: string) {
  const auto = useId()
  const fieldId = id ?? auto
  const noteId = `${fieldId}-nota`
  return { fieldId, noteId, describedBy: error || hint ? noteId : undefined }
}

function FieldNote({ id, error, hint }: { id: string; error?: string; hint?: string }) {
  if (error) {
    return (
      <p id={id} className="mt-2 flex items-start gap-1.5 px-5 text-sm text-danger">
        <WarningCircleIcon size={16} weight="bold" className="mt-0.5 shrink-0" />
        {error}
      </p>
    )
  }
  if (hint) {
    return (
      <p id={id} className="mt-2 px-5 text-sm text-ink-soft">
        {hint}
      </p>
    )
  }
  return null
}

type TextFieldProps = FieldBase & Omit<InputHTMLAttributes<HTMLInputElement>, 'className'>

export function TextField({ label, error, hint, className = '', id, ...input }: TextFieldProps) {
  const { fieldId, noteId, describedBy } = useFieldIds(id, error, hint)
  return (
    <div className={className}>
      <label htmlFor={fieldId} className={labelClass}>
        {label}
      </label>
      <input
        id={fieldId}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        className={`${controlClass} ${error ? 'ring-danger' : 'ring-snow/20 hover:ring-snow/40'}`}
        {...input}
      />
      <FieldNote id={noteId} error={error} hint={hint} />
    </div>
  )
}

type SelectFieldProps = FieldBase &
  Omit<SelectHTMLAttributes<HTMLSelectElement>, 'className'> & { placeholder: string; children: ReactNode }

export function SelectField({ label, error, hint, className = '', id, placeholder, children, ...select }: SelectFieldProps) {
  const { fieldId, noteId, describedBy } = useFieldIds(id, error, hint)
  return (
    <div className={className}>
      <label htmlFor={fieldId} className={labelClass}>
        {label}
      </label>
      <div className="relative">
        <select
          id={fieldId}
          aria-invalid={error ? true : undefined}
          aria-describedby={describedBy}
          className={`${controlClass} cursor-pointer appearance-none pr-12 ${error ? 'ring-danger' : 'ring-snow/20 hover:ring-snow/40'}`}
          {...select}
        >
          <option value="" disabled>
            {placeholder}
          </option>
          {children}
        </select>
        <CaretDownIcon size={18} aria-hidden className="pointer-events-none absolute top-1/2 right-5 -translate-y-1/2 text-ink-soft" />
      </div>
      <FieldNote id={noteId} error={error} hint={hint} />
    </div>
  )
}

// Um bloco do formulário: legenda curta e os campos em duas colunas a partir do tablet.
export function FieldGroup({ legend, children }: { legend: string; children: ReactNode }) {
  return (
    <fieldset className="grid gap-5 md:grid-cols-2">
      <legend className="mb-5 font-display text-sm font-bold tracking-wide uppercase">{legend}</legend>
      {children}
    </fieldset>
  )
}

export function FormAlert({ children }: { children: ReactNode }) {
  return (
    <div role="alert" className="flex items-start gap-3 rounded-[20px] px-5 py-4 text-danger ring-1 ring-danger/50">
      <WarningCircleIcon size={20} weight="bold" className="mt-0.5 shrink-0" />
      <p>{children}</p>
    </div>
  )
}
