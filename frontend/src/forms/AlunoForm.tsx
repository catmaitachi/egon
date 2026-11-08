import { ArrowClockwiseIcon } from '@phosphor-icons/react'
import { api, type Aluno, type AlunoEdicao } from '../lib/api'
import { maskCpf } from '../lib/format'
import { useLoad } from '../lib/useLoad'
import { ActionButton } from '../components/controls'
import { FieldGroup, FormAlert, SelectField, TextField } from '../components/fields'
import { validarAluno } from './regras'
import { FormActions } from './parts'
import { minusculo, useCampos } from './useCampos'

type Props = {
  // Sem aluno é o cadastro (HU02); com aluno é a edição: sem CPF (é a chave) e com senha opcional.
  aluno?: Aluno
  label: string
  busyLabel: string
  // O CPF vai separado porque só o cadastro o envia.
  onSubmit: (dados: AlunoEdicao, cpf: string) => Promise<void>
  onCancel?: () => void
}

export function AlunoForm({ aluno, label, busyLabel, onSubmit, onCancel }: Props) {
  const edicao = aluno !== undefined
  const { formRef, sending, alert, campo, enviar } = useCampos({
    nome: aluno?.nome ?? '',
    email: aluno?.email ?? '',
    cpf: '',
    rg: aluno?.rg ?? '',
    endereco: aluno?.endereco ?? '',
    idInstituicao: aluno?.instituicao.id ?? '',
    idCurso: aluno ? String(aluno.curso.id) : '',
    login: aluno?.login ?? '',
    senha: '',
  })
  // Instituição e curso vêm de listas da API, nunca digitados.
  const listas = useLoad(() => Promise.all([api.instituicoes.listar(), api.cursos.listar()]))
  const [instituicoes, cursos] = listas.data ?? [[], []]
  const escolha = listas.data ? 'Escolha da lista' : 'Carregando…'

  const submit = enviar(
    (v) => validarAluno(v, edicao),
    (v) =>
      onSubmit(
        {
          nome: v.nome,
          email: v.email,
          rg: v.rg,
          endereco: v.endereco,
          idInstituicao: v.idInstituicao,
          idCurso: Number(v.idCurso),
          login: v.login,
          senha: v.senha,
        },
        v.cpf,
      ),
  )

  return (
    <form ref={formRef} noValidate onSubmit={submit} className="grid gap-10 font-normal">
      <FieldGroup legend="Dados pessoais">
        <TextField label="Nome completo" autoComplete="name" className="md:col-span-2" {...campo('nome')} />
        <TextField label="E-mail" type="email" autoComplete="email" spellCheck={false} className="md:col-span-2" {...campo('email')} />
        {!edicao && (
          <TextField label="CPF" inputMode="numeric" autoComplete="off" placeholder="000.000.000-00" {...campo('cpf', maskCpf)} />
        )}
        <TextField label="RG" autoComplete="off" className={edicao ? 'md:col-span-2' : ''} {...campo('rg')} />
        <TextField label="Endereço" autoComplete="street-address" className="md:col-span-2" {...campo('endereco')} />
      </FieldGroup>

      <FieldGroup legend="Instituição de ensino">
        {listas.error && (
          <div className="grid justify-items-start gap-3 md:col-span-2">
            <FormAlert>{listas.error.message}</FormAlert>
            <ActionButton variant="outline" onClick={listas.reload}>
              <ArrowClockwiseIcon size={18} aria-hidden />
              Carregar as listas de novo
            </ActionButton>
          </div>
        )}
        <SelectField label="Instituição" placeholder={escolha} {...campo('idInstituicao')}>
          {instituicoes.map((i) => (
            <option key={i.id} value={i.id}>
              {i.nome}
            </option>
          ))}
        </SelectField>
        <SelectField label="Curso" placeholder={escolha} {...campo('idCurso')}>
          {cursos.map((c) => (
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
