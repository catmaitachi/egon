import { useState, type ChangeEvent, type FormEvent } from 'react'
import { brand, tipoVantagem } from '../content'
import type { VantagemCadastro } from '../lib/api'
import { useFormSubmit } from '../lib/useFormSubmit'
import { ActionButton } from '../components/controls'
import { FormAlert, ImageField, SelectField, TextAreaField, TextField } from '../components/form'

type Values = Omit<VantagemCadastro, 'foto'>

const vazio: Values = { nome: '', descricao: '', custo: '', tipo: '' }

// HU08: nome, descrição, foto e custo em moedas. Depois de publicar, o formulário volta em branco.
export function VantagemForm({ onSubmit }: { onSubmit: (dados: VantagemCadastro) => Promise<void> }) {
  const [values, setValues] = useState<Values>(vazio)
  const [foto, setFoto] = useState<File | null>(null)
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()

  const bind = (name: keyof Values) => ({
    name,
    value: values[name],
    error: errors[name],
    onChange: (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
      setValues((v) => ({ ...v, [name]: e.target.value }))
      clear(name)
    },
  })

  const enviar = (e: FormEvent) => {
    e.preventDefault()
    submit(async () => {
      await onSubmit({ ...values, foto })
      setValues(vazio)
      setFoto(null)
    })
  }

  return (
    <form ref={formRef} noValidate onSubmit={enviar} className="grid gap-6">
      {alert && <FormAlert>{alert}</FormAlert>}
      <div className="grid gap-5 md:grid-cols-2">
        <TextField label="Nome da vantagem" placeholder="Ex.: 20% de desconto no almoço" className="md:col-span-2" {...bind('nome')} />
        <SelectField label="Tipo" placeholder="Escolha da lista" {...bind('tipo')}>
          {Object.entries(tipoVantagem).map(([valor, rotulo]) => (
            <option key={valor} value={valor}>
              {rotulo}
            </option>
          ))}
        </SelectField>
        <TextField
          label={`Custo em ${brand.coin}s`}
          type="number"
          inputMode="numeric"
          min={1}
          step={1}
          hint="Número inteiro maior que zero."
          {...bind('custo')}
        />
        <TextAreaField
          label="Descrição"
          placeholder="O que o aluno recebe e como usar."
          maxLength={1000}
          className="md:col-span-2"
          {...bind('descricao')}
        />
        <ImageField
          label="Foto"
          name="foto"
          file={foto}
          error={errors.foto}
          className="md:col-span-2"
          onFile={(f) => {
            setFoto(f)
            clear('foto')
          }}
        />
      </div>
      <div className="flex justify-end">
        <ActionButton type="submit" disabled={sending}>
          {sending ? 'Publicando…' : 'Publicar vantagem'}
        </ActionButton>
      </div>
    </form>
  )
}
