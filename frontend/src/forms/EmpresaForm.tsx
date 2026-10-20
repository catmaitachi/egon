import { useState, type ChangeEvent, type FormEvent } from 'react'
import type { Empresa, EmpresaCadastro, EmpresaEdicao } from '../lib/api'
import { maskCnpj } from '../lib/documentos'
import { useFormSubmit } from '../lib/useFormSubmit'
import { ActionButton } from '../components/controls'
import { FieldGroup, FormAlert, TextField } from '../components/form'

// Sem `initial`, é o cadastro (HU03). Com `initial`, é a edição: o CNPJ não aparece
// (é a chave da empresa) e a senha vira opcional.
type Props = { submitLabel: string; onCancel?: () => void } & (
  | { initial?: undefined; onSubmit: (dados: EmpresaCadastro) => Promise<void> }
  | { initial: Empresa; onSubmit: (dados: EmpresaEdicao) => Promise<void> }
)

type Values = Record<'nome' | 'cnpj' | 'email' | 'login' | 'senha', string>

export function EmpresaForm(props: Props) {
  const editing = props.initial !== undefined
  const [values, setValues] = useState<Values>(() => ({
    nome: props.initial?.nome ?? '',
    cnpj: '',
    email: props.initial?.email ?? '',
    login: props.initial?.login ?? '',
    senha: '',
  }))
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()

  const bind = (name: keyof Values, mask?: (v: string) => string) => ({
    name,
    value: values[name],
    error: errors[name],
    onChange: (e: ChangeEvent<HTMLInputElement>) => {
      const value = mask ? mask(e.target.value) : e.target.value
      setValues((v) => ({ ...v, [name]: value }))
      clear(name)
    },
  })

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    const dados: EmpresaEdicao = { nome: values.nome, email: values.email, login: values.login, senha: values.senha }
    submit(() => (props.initial ? props.onSubmit(dados) : props.onSubmit({ ...dados, cnpj: values.cnpj })))
  }

  return (
    <form ref={formRef} noValidate onSubmit={onSubmit} className="grid gap-10">
      {alert && <FormAlert>{alert}</FormAlert>}

      <FieldGroup legend="Dados da empresa">
        <TextField label="Nome da empresa" autoComplete="organization" className="md:col-span-2" {...bind('nome')} />
        {!editing && (
          <TextField
            label="CNPJ"
            autoComplete="off"
            autoCapitalize="characters"
            placeholder="00.000.000/0000-00"
            {...bind('cnpj', maskCnpj)}
          />
        )}
        <TextField
          label="E-mail de contato"
          type="email"
          autoComplete="email"
          className={editing ? 'md:col-span-2' : ''}
          {...bind('email')}
        />
      </FieldGroup>

      <FieldGroup legend="Acesso">
        <TextField
          label="Login"
          autoComplete="username"
          autoCapitalize="none"
          hint="Letras minúsculas, números, ponto, hífen ou sublinhado."
          {...bind('login')}
        />
        <TextField
          label={editing ? 'Nova senha' : 'Senha'}
          type="password"
          autoComplete="new-password"
          hint={editing ? 'Deixe em branco para manter a atual.' : 'Pelo menos 8 caracteres.'}
          {...bind('senha')}
        />
      </FieldGroup>

      <div className="flex flex-wrap justify-end gap-3">
        {props.onCancel && (
          <ActionButton variant="outline" onClick={props.onCancel}>
            Cancelar
          </ActionButton>
        )}
        <ActionButton type="submit" disabled={sending}>
          {sending ? 'Enviando…' : props.submitLabel}
        </ActionButton>
      </div>
    </form>
  )
}
