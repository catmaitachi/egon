import {
  useEffect,
  useId,
  useMemo,
  useRef,
  type InputHTMLAttributes,
  type ReactNode,
  type SelectHTMLAttributes,
  type TextareaHTMLAttributes,
} from 'react'
import { CaretDownIcon, ImageIcon, WarningCircleIcon } from '@phosphor-icons/react'

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

type TextAreaFieldProps = FieldBase & Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, 'className'>

// Texto longo (descrição, mensagem): mesma linguagem dos campos, cantos de superfície em vez de pílula.
export function TextAreaField({ label, error, hint, className = '', id, rows = 4, ...area }: TextAreaFieldProps) {
  const { fieldId, noteId, describedBy } = useFieldIds(id, error, hint)
  return (
    <div className={className}>
      <label htmlFor={fieldId} className={labelClass}>
        {label}
      </label>
      <textarea
        id={fieldId}
        rows={rows}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        className={`block w-full resize-y rounded-[20px] bg-night px-5 py-3 text-base text-snow ring-1 transition-shadow duration-200 placeholder:text-snow/40 focus:ring-2 focus:ring-snow focus-visible:outline-none ${
          error ? 'ring-danger' : 'ring-snow/20 hover:ring-snow/40'
        }`}
        {...area}
      />
      <FieldNote id={noteId} error={error} hint={hint} />
    </div>
  )
}

type ImageFieldProps = FieldBase & { name: string; file: File | null; onFile: (file: File | null) => void }

// Foto com pré-visualização. O input fica escondido; a caixa inteira abre o seletor e acende no foco.
export function ImageField({ label, error, hint, className = '', name, file, onFile }: ImageFieldProps) {
  const { fieldId, noteId, describedBy } = useFieldIds(undefined, error, hint)
  const input = useRef<HTMLInputElement>(null)
  const preview = useMemo(() => (file ? URL.createObjectURL(file) : null), [file])
  useEffect(() => () => {
    if (preview) URL.revokeObjectURL(preview)
  }, [preview])

  return (
    <div className={className}>
      <label htmlFor={fieldId} className={labelClass}>
        {label}
      </label>
      <div
        onClick={() => input.current?.click()}
        className={`flex cursor-pointer items-center gap-5 rounded-[20px] bg-night p-3 ring-1 transition-shadow duration-200 focus-within:ring-2 focus-within:ring-snow ${
          error ? 'ring-danger' : 'ring-snow/20 hover:ring-snow/40'
        }`}
      >
        <span className="grid size-20 shrink-0 place-items-center overflow-hidden rounded-2xl bg-coal ring-1 ring-line">
          {preview ? (
            <img src={preview} alt="" className="size-full object-cover" />
          ) : (
            <ImageIcon size={28} className="text-ink-soft" aria-hidden />
          )}
        </span>
        <span className="min-w-0">
          <span className="block font-display text-sm font-semibold tracking-wide uppercase">
            {file ? 'Trocar foto' : 'Escolher foto'}
          </span>
          <span className="mt-1 block truncate text-sm text-ink-soft">{file ? file.name : 'JPG, PNG, WebP ou GIF, até 5 MB.'}</span>
        </span>
        <input
          ref={input}
          id={fieldId}
          name={name}
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          aria-invalid={error ? true : undefined}
          aria-describedby={describedBy}
          className="sr-only"
          onClick={(e) => e.stopPropagation()}
          onChange={(e) => onFile(e.target.files?.[0] ?? null)}
        />
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
