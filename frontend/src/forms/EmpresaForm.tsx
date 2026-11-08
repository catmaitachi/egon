import type { Empresa, EmpresaEdicao } from '../lib/api'
import { maskCnpj } from '../lib/format'
import { FieldGroup, TextField } from '../components/fields'
import { validarEmpresa } from './regras'
import { FormActions } from './parts'
import { minusculo, useCampos } from './useCampos'

type Props = {
  // Sem empresa é o cadastro (HU03); com empresa é a edição: sem CNPJ (é a chave) e com senha opcional.
  empresa?: Empresa
  label: string
  busyLabel: string
  // O CNPJ vai separado porque só o cadastro o envia.
  onSubmit: (dados: EmpresaEdicao, cnpj: string) => Promise<void>
  onCancel?: () => void
}

export function EmpresaForm({ empresa, label, busyLabel, onSubmit, onCancel }: Props) {
  const edicao = empresa !== undefined
  const { formRef, sending, alert, campo, enviar } = useCampos({
    nome: empresa?.nome ?? '',
    cnpj: '',
    email: empresa?.email ?? '',
    login: empresa?.login ?? '',
    senha: '',
  })

  const submit = enviar(
    (v) => validarEmpresa(v, edicao),
    (v) => onSubmit({ nome: v.nome, email: v.email, login: v.login, senha: v.senha }, v.cnpj),
  )

  return (
    <form ref={formRef} noValidate onSubmit={submit} className="grid gap-10 font-normal">
      <FieldGroup legend="Empresa">
        <TextField label="Nome da empresa" autoComplete="organization" className="md:col-span-2" {...campo('nome')} />
        {!edicao && (
          <TextField
            label="CNPJ"
            autoComplete="off"
            autoCapitalize="characters"
            spellCheck={false}
            placeholder="00.000.000/0000-00"
            hint="Pode ter letras: o CNPJ alfanumérico também vale."
            {...campo('cnpj', maskCnpj)}
          />
        )}
        <TextField
          label="E-mail de contato"
          type="email"
          autoComplete="email"
          spellCheck={false}
          className={edicao ? 'md:col-span-2' : ''}
          {...campo('email')}
        />
      </FieldGroup>

      <FieldGroup legend="Acesso">
        <TextField
          label="Login"
          autoComplete="username"
          autoCapitalize="none"
          spellCheck={false}
          hint="Letras minúsculas, números, ponto, hífen ou sublinhado."
          {...campo('login', minusculo)}
        />
        <TextField
          label={edicao ? 'Nova senha' : 'Senha'}
          type="password"
          autoComplete="new-password"
          hint={edicao ? 'Deixe em branco para manter a atual.' : 'De 8 a 72 caracteres.'}
          {...campo('senha')}
        />
      </FieldGroup>

      <FormActions alert={alert} sending={sending} label={label} busyLabel={busyLabel} onCancel={onCancel} />
    </form>
  )
}
