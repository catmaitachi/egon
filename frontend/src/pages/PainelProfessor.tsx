import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { HandCoinsIcon, ListBulletsIcon } from '@phosphor-icons/react'
import { brand, coins } from '../content'
import { api, type AlunoResumo, type Extrato, type Professor } from '../lib/api'
import { useFormSubmit } from '../lib/useFormSubmit'
import { useTitle } from '../lib/useTitle'
import { ActionButton } from '../components/controls'
import { FormAlert, SelectField, TextAreaField, TextField } from '../components/form'
import { Abas, Carregando, ExtratoLista, PainelShell, Saldo, Vazio } from '../components/Painel'
import { Notice } from '../components/Records'
import { PageTitle, Panel } from '../components/Shell'

type AbaProfessor = 'enviar' | 'extrato'

const abas = [
  { id: 'enviar' as const, label: `Enviar ${brand.coin}s`, icon: HandCoinsIcon },
  { id: 'extrato' as const, label: 'Extrato', icon: ListBulletsIcon },
]

export function PainelProfessor() {
  useTitle('Painel do professor')
  return (
    <PainelShell perfil="PROFESSOR">
      <ConteudoProfessor />
    </PainelShell>
  )
}

function ConteudoProfessor() {
  const [aba, setAba] = useState<AbaProfessor>('enviar')
  const [professor, setProfessor] = useState<Professor | null>(null)
  const [alunos, setAlunos] = useState<AlunoResumo[] | null>(null)
  const [extrato, setExtrato] = useState<Extrato | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const carregar = useCallback(
    () =>
      Promise.all([api.professores.eu(), api.professores.alunos(), api.professores.extrato()]).then(
        ([p, a, e]) => {
          setProfessor(p)
          setAlunos(a)
          setExtrato(e)
        },
        (e: Error) => setErro(e.message),
      ),
    [],
  )

  useEffect(() => {
    carregar()
  }, [carregar])

  return (
    <>
      <PageTitle
        eyebrow="Painel do professor"
        title="Reconheça seus alunos"
        lead={professor ? `${professor.departamento} · ${professor.instituicao.nome}` : undefined}
      />
      <div className="mt-8 grid gap-8">
        {erro && <FormAlert>{erro}</FormAlert>}
        <Saldo valor={extrato?.saldo ?? null} rotulo="Para distribuir">
          <p className="max-w-[22rem] text-sm leading-relaxed text-ink-soft">
            Você recebe 1.000 {brand.coin}s a cada semestre. O que sobra acumula para o próximo.
          </p>
        </Saldo>
        <Abas abas={abas} atual={aba} onChange={setAba} />

        {aba === 'enviar' &&
          (alunos === null || extrato === null ? (
            <Carregando />
          ) : alunos.length === 0 ? (
            <Vazio>Nenhum aluno da sua instituição criou conta ainda.</Vazio>
          ) : (
            <Panel className="px-6 py-8 md:px-10 md:py-10">
              <EnvioForm alunos={alunos} saldo={extrato.saldo} onEnviado={carregar} />
            </Panel>
          ))}
        {aba === 'extrato' && (
          <ExtratoLista lancamentos={extrato?.lancamentos ?? null} vazio="Nenhum lançamento ainda." />
        )}
      </div>
    </>
  )
}

// HU06: aluno da instituição, quantidade e um motivo obrigatório. O aluno recebe o motivo por e-mail (HU07).
function EnvioForm({ alunos, saldo, onEnviado }: { alunos: AlunoResumo[]; saldo: number; onEnviado: () => void }) {
  const [busca, setBusca] = useState('')
  const [cpfAluno, setCpfAluno] = useState('')
  const [valor, setValor] = useState('')
  const [motivo, setMotivo] = useState('')
  const [feito, setFeito] = useState<string | null>(null)
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()

  // O aluno escolhido continua na lista mesmo que a busca não o encontre mais.
  const visiveis = useMemo(() => {
    const termo = busca.trim().toLocaleLowerCase('pt-BR')
    return alunos.filter((a) => !termo || a.cpf === cpfAluno || a.nome.toLocaleLowerCase('pt-BR').includes(termo))
  }, [alunos, busca, cpfAluno])

  const onSubmit = (e: FormEvent) => {
    e.preventDefault()
    setFeito(null)
    submit(async () => {
      const envio = await api.transacoes.enviar({ cpfAluno, valor: valor === '' ? null : Number(valor), motivo })
      setFeito(`Você enviou ${coins(envio.valor)} para ${envio.aluno}. O aviso, com a sua mensagem, foi por e-mail.`)
      setValor('')
      setMotivo('')
      onEnviado()
    })
  }

  return (
    <form ref={formRef} noValidate onSubmit={onSubmit} className="grid gap-6">
      {feito && <Notice>{feito}</Notice>}
      {alert && <FormAlert>{alert}</FormAlert>}
      <div className="grid gap-5 md:grid-cols-2">
        <TextField
          label="Buscar aluno"
          name="busca"
          type="search"
          autoComplete="off"
          placeholder="Nome do aluno"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
        />
        <SelectField
          label="Aluno"
          name="cpfAluno"
          placeholder={visiveis.length ? 'Escolha da lista' : 'Nenhum aluno com esse nome'}
          value={cpfAluno}
          error={errors.cpfAluno}
          onChange={(e) => {
            setCpfAluno(e.target.value)
            clear('cpfAluno')
          }}
        >
          {visiveis.map((a) => (
            <option key={a.cpf} value={a.cpf}>
              {a.nome} · {a.curso}
            </option>
          ))}
        </SelectField>
        <TextField
          label="Quantidade"
          name="valor"
          type="number"
          inputMode="numeric"
          min={1}
          max={saldo}
          step={1}
          value={valor}
          error={errors.valor}
          hint={`Você tem ${coins(saldo)} para distribuir.`}
          onChange={(e) => {
            setValor(e.target.value)
            clear('valor')
          }}
        />
        <TextAreaField
          label="Motivo do reconhecimento"
          name="motivo"
          maxLength={500}
          placeholder="Participação, dedicação, ajuda a um colega…"
          className="md:col-span-2"
          value={motivo}
          error={errors.motivo}
          hint="Obrigatório. Vai junto no e-mail que o aluno recebe."
          onChange={(e) => {
            setMotivo(e.target.value)
            clear('motivo')
          }}
        />
      </div>
      <div className="flex justify-end">
        <ActionButton type="submit" disabled={sending}>
          {sending ? 'Enviando…' : `Enviar ${brand.coin}s`}
        </ActionButton>
      </div>
    </form>
  )
}
