import type { Admin, AdminCadastro, Curso, Instituicao, InstituicaoCadastro, Professor, ProfessorCadastro } from '../lib/api'
import { api } from '../lib/api'
import { maskCnpj, maskCpf } from '../lib/format'
import { useLoad } from '../lib/useLoad'
import { FieldGroup, SelectField, TextField } from '../components/fields'
import { FormActions } from './parts'
import { validarAdmin, validarInstituicao, validarProfessor, type Erros } from './regras'
import { minusculo, useCampos } from './useCampos'

type Base<T, D> = { item?: T; label: string; busyLabel: string; onSubmit: (dados: D) => Promise<void>; onCancel: () => void }

// Instituição conveniada (HU04): o CNPJ só no cadastro; depois identifica e não muda.
export function InstituicaoForm({ item, label, busyLabel, onSubmit, onCancel }: Base<Instituicao, InstituicaoCadastro>) {
  const edicao = item !== undefined
  const { formRef, sending, alert, campo, enviar } = useCampos({ cnpj: '', nome: item?.nome ?? '', tipo: item?.tipo ?? '' })
  return (
    <form ref={formRef} noValidate onSubmit={enviar((v) => validarInstituicao(v, edicao), (v) => onSubmit(v))} className="grid gap-5 font-normal">
      {!edicao && <TextField label="CNPJ" autoComplete="off" {...campo('cnpj', maskCnpj)} />}
      <TextField label="Nome" maxLength={100} {...campo('nome')} />
      <TextField label="Tipo" placeholder="Universidade, Faculdade, Centro Universitário…" maxLength={30} {...campo('tipo')} />
      <FormActions alert={alert} sending={sending} label={label} busyLabel={busyLabel} onCancel={onCancel} />
    </form>
  )
}

export function CursoForm({ item, label, busyLabel, onSubmit, onCancel }: Base<Curso, string>) {
  const { formRef, sending, alert, campo, enviar } = useCampos({ nome: item?.nome ?? '' })
  return (
    <form
      ref={formRef}
      noValidate
      onSubmit={enviar((v): Erros => (v.nome.trim() ? {} : { nome: 'Informe o nome do curso.' }), (v) => onSubmit(v.nome))}
      className="grid gap-5 font-normal"
    >
      <TextField label="Nome do curso" maxLength={100} {...campo('nome')} />
      <FormActions alert={alert} sending={sending} label={label} busyLabel={busyLabel} onCancel={onCancel} />
    </form>
  )
}

// Pré-cadastro de professor pelo admin (HU04). Ao ser criado ele já recebe a cota do semestre.
export function ProfessorForm({ item, label, busyLabel, onSubmit, onCancel }: Base<Professor, ProfessorCadastro>) {
  const edicao = item !== undefined
  const instituicoes = useLoad(api.instituicoes.listar)
  const { formRef, sending, alert, campo, enviar } = useCampos({
    cpf: '',
    nome: item?.nome ?? '',
    email: item?.email ?? '',
    departamento: item?.departamento ?? '',
    idInstituicao: item?.instituicao.id ?? '',
    login: item?.login ?? '',
    senha: '',
  })
  return (
    <form ref={formRef} noValidate onSubmit={enviar((v) => validarProfessor(v, edicao), (v) => onSubmit(v))} className="grid gap-10 font-normal">
      <FieldGroup legend="Professor">
        {!edicao && <TextField label="CPF" inputMode="numeric" autoComplete="off" {...campo('cpf', maskCpf)} />}
        <TextField label="Nome completo" maxLength={100} className={edicao ? 'md:col-span-2' : ''} {...campo('nome')} />
        <TextField
          label="E-mail"
          type="email"
          autoComplete="off"
          spellCheck={false}
          className="md:col-span-2"
          hint="Recebe a confirmação de cada envio de Egos."
          {...campo('email')}
        />
        <TextField label="Departamento" maxLength={100} {...campo('departamento')} />
        <SelectField label="Instituição" placeholder={instituicoes.data ? 'Escolha' : 'Carregando…'} {...campo('idInstituicao')}>
          {instituicoes.data?.map((i) => (
            <option key={i.id} value={i.id}>
              {i.nome}
            </option>
          ))}
        </SelectField>
      </FieldGroup>
      <FieldGroup legend="Acesso">
        <TextField label="Login" autoComplete="off" autoCapitalize="none" {...campo('login', minusculo)} />
        <TextField
          label={edicao ? 'Nova senha' : 'Senha inicial'}
          type="password"
          autoComplete="new-password"
          hint={edicao ? 'Em branco, fica a atual.' : 'De 8 a 72 caracteres. Passe ao professor.'}
          {...campo('senha')}
        />
      </FieldGroup>
      <FormActions alert={alert} sending={sending} label={label} busyLabel={busyLabel} onCancel={onCancel} />
    </form>
  )
}

export function AdminForm({ item, label, busyLabel, onSubmit, onCancel }: Base<Admin, AdminCadastro>) {
  const edicao = item !== undefined
  const { formRef, sending, alert, campo, enviar } = useCampos({ nome: item?.nome ?? '', login: item?.login ?? '', senha: '' })
  return (
    <form ref={formRef} noValidate onSubmit={enviar((v) => validarAdmin(v, edicao), (v) => onSubmit(v))} className="grid gap-5 font-normal">
      <TextField label="Nome" maxLength={100} {...campo('nome')} />
      <TextField label="Login" autoComplete="off" autoCapitalize="none" {...campo('login', minusculo)} />
      <TextField
        label={edicao ? 'Nova senha' : 'Senha'}
        type="password"
        autoComplete="new-password"
        hint={edicao ? 'Em branco, fica a atual.' : 'De 8 a 72 caracteres.'}
        {...campo('senha')}
      />
      <FormActions alert={alert} sending={sending} label={label} busyLabel={busyLabel} onCancel={onCancel} />
    </form>
  )
}
