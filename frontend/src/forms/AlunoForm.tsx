import { useState, type ChangeEvent, type FormEvent } from 'react'
import type { Aluno, AlunoCadastro, AlunoEdicao } from '../lib/api'
import { maskCpf } from '../lib/documentos'
import { useAcademicOptions } from '../lib/useAcademicOptions'
import { useFormSubmit } from '../lib/useFormSubmit'
import { ActionButton } from '../components/controls'
import { FieldGroup, FormAlert, SelectField, TextField } from '../components/form'

// Sem `initial`, é o cadastro (HU02). Com `initial`, é a edição: o CPF não aparece
// (é a chave do aluno) e a senha vira opcional.
type Props = { submitLabel: string; onCancel?: () => void } & (
  | { initial?: undefined; onSubmit: (dados: AlunoCadastro) => Promise<void> }
  | { initial: Aluno; onSubmit: (dados: AlunoEdicao) => Promise<void> }
)

type Values = Record<'nome' | 'email' | 'cpf' | 'rg' | 'endereco' | 'cnpjInstituicao' | 'idCurso' | 'login' | 'senha', string>

function startValues(initial?: Aluno): Values {
  return {
    nome: initial?.nome ?? '',
    email: initial?.email ?? '',
    cpf: '',
    rg: initial?.rg ?? '',
    endereco: initial?.endereco ?? '',
    cnpjInstituicao: initial?.instituicao.cnpj ?? '',
    idCurso: initial ? String(initial.curso.id) : '',
    login: initial?.login ?? '',
    senha: '',
  }
}

export function AlunoForm(props: Props) {
  const editing = props.initial !== undefined
  const [values, setValues] = useState(() => startValues(props.initial))
  const { options, error: optionsError } = useAcademicOptions()
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()

  const bind = (name: keyof Values, mask?: (v: string) => string) => ({
    name,
    value: values[name],
    error: errors[name],
    onChange: (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
      const value = mask ? mask(e.target.value) : e.target.value
      setValues((v) => ({ ...v, [name]: value }))
      clear(name)
    },
  })

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    const dados: AlunoEdicao = {
      nome: values.nome,
      email: values.email,
      rg: values.rg,
      endereco: values.endereco,
      cnpjInstituicao: values.cnpjInstituicao,
      idCurso: values.idCurso ? Number(values.idCurso) : null,
      login: values.login,
      senha: values.senha,
    }
    submit(() => (props.initial ? props.onSubmit(dados) : props.onSubmit({ ...dados, cpf: values.cpf })))
  }

  return (
    <form ref={formRef} noValidate onSubmit={onSubmit} className="grid gap-10">
      {alert && <FormAlert>{alert}</FormAlert>}
      {optionsError && <FormAlert>{optionsError}</FormAlert>}

      <FieldGroup legend="Dados pessoais">
        <TextField label="Nome completo" autoComplete="name" className="md:col-span-2" {...bind('nome')} />
        <TextField label="E-mail" type="email" autoComplete="email" className="md:col-span-2" {...bind('email')} />
        {!editing && (
          <TextField label="CPF" inputMode="numeric" autoComplete="off" placeholder="000.000.000-00" {...bind('cpf', maskCpf)} />
        )}
        <TextField label="RG" autoComplete="off" className={editing ? 'md:col-span-2' : ''} {...bind('rg')} />
        <TextField label="Endereço" autoComplete="street-address" className="md:col-span-2" {...bind('endereco')} />
      </FieldGroup>

      <FieldGroup legend="Instituição de ensino">
        <SelectField label="Instituição" placeholder={options ? 'Escolha da lista' : 'Carregando…'} {...bind('cnpjInstituicao')}>
          {options?.instituicoes.map((i) => (
            <option key={i.cnpj} value={i.cnpj}>
              {i.nome}
            </option>
          ))}
        </SelectField>
        <SelectField label="Curso" placeholder={options ? 'Escolha da lista' : 'Carregando…'} {...bind('idCurso')}>
          {options?.cursos.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nome}
            </option>
          ))}
        </SelectField>
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
