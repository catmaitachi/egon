import { useCallback, useEffect, useState } from 'react'
import { CheckCircleIcon, EnvelopeSimpleIcon, GiftIcon, TicketIcon, TrashIcon, XCircleIcon } from '@phosphor-icons/react'
import { coins, quando, tipoVantagem } from '../content'
import { api, fotoUrl, type Aviso, type Resgate, type Vantagem, type VantagemCadastro } from '../lib/api'
import { useSessao } from '../lib/sessao'
import { useTitle } from '../lib/useTitle'
import { VantagemForm } from '../forms/VantagemForm'
import { ActionButton, IconButton } from '../components/controls'
import { Dialog } from '../components/Dialog'
import { FormAlert, TextField } from '../components/form'
import { Abas, AvisosLista, Carregando, PainelShell, Preco, Vazio } from '../components/Painel'
import { Notice } from '../components/Records'
import { PageTitle, Panel } from '../components/Shell'

type AbaEmpresa = 'vantagens' | 'resgates' | 'avisos'

const abas = [
  { id: 'vantagens' as const, label: 'Vantagens', icon: GiftIcon },
  { id: 'resgates' as const, label: 'Resgates', icon: TicketIcon },
  { id: 'avisos' as const, label: 'E-mails', icon: EnvelopeSimpleIcon },
]

export function PainelEmpresa() {
  useTitle('Painel da empresa')
  return (
    <PainelShell perfil="EMPRESA">
      <ConteudoEmpresa />
    </PainelShell>
  )
}

function ConteudoEmpresa() {
  const sessao = useSessao()
  const cnpj = sessao?.id ?? ''
  const [aba, setAba] = useState<AbaEmpresa>('vantagens')
  const [vantagens, setVantagens] = useState<Vantagem[] | null>(null)
  const [resgates, setResgates] = useState<Resgate[] | null>(null)
  const [avisos, setAvisos] = useState<Aviso[] | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [removendo, setRemovendo] = useState<Vantagem | null>(null)

  const carregar = useCallback(
    () =>
      Promise.all([api.vantagens.listar(cnpj), api.trocas.resgates(), api.avisos()]).then(
        ([v, r, a]) => {
          setVantagens(v)
          setResgates(r)
          setAvisos(a)
        },
        (e: Error) => setErro(e.message),
      ),
    [cnpj],
  )

  useEffect(() => {
    carregar()
  }, [carregar])

  const publicar = async (dados: VantagemCadastro) => {
    const nova = await api.vantagens.cadastrar(dados)
    setVantagens((l) => (l ? [...l, nova].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')) : [nova]))
    setAviso(`Vantagem publicada no catálogo: ${nova.nome}.`)
  }

  const remover = async () => {
    if (!removendo) return
    await api.vantagens.remover(removendo.id)
    setVantagens((l) => l && l.filter((v) => v.id !== removendo.id))
    setAviso(`Vantagem removida do catálogo: ${removendo.nome}.`)
    setRemovendo(null)
  }

  return (
    <>
      <PageTitle eyebrow="Painel da empresa" title={sessao?.nome ?? 'Empresa'} />
      <div className="mt-8 grid gap-8">
        {erro && <FormAlert>{erro}</FormAlert>}
        <Abas abas={abas} atual={aba} onChange={setAba} />

        {aba === 'vantagens' && (
          <>
            {aviso && <Notice>{aviso}</Notice>}
            <Panel className="px-6 py-8 md:px-10 md:py-10">
              <h2 className="mb-6 font-display text-sm font-bold tracking-wide uppercase">Nova vantagem</h2>
              <VantagemForm onSubmit={publicar} />
            </Panel>
            <MinhasVantagens vantagens={vantagens} onRemover={setRemovendo} />
          </>
        )}
        {aba === 'resgates' && <Resgates resgates={resgates} />}
        {aba === 'avisos' && <AvisosLista avisos={avisos} />}
      </div>

      <Dialog open={removendo !== null} onClose={() => setRemovendo(null)} title="Remover vantagem">
        {removendo && <ConfirmarRemocao nome={removendo.nome} onCancel={() => setRemovendo(null)} onConfirm={remover} />}
      </Dialog>
    </>
  )
}

function MinhasVantagens({ vantagens, onRemover }: { vantagens: Vantagem[] | null; onRemover: (v: Vantagem) => void }) {
  if (!vantagens) return <Carregando />
  if (vantagens.length === 0) return <Vazio>Você ainda não publicou vantagens.</Vazio>
  return (
    <section className="grid gap-4">
      <h2 className="font-display text-sm font-bold tracking-wide uppercase">No catálogo</h2>
      <Panel>
        <ul className="divide-y divide-line">
          {vantagens.map((v) => (
            <li key={v.id} className="flex items-center gap-5 px-6 py-5 md:px-8">
              <img src={fotoUrl(v)} alt="" className="size-16 shrink-0 rounded-2xl bg-night object-cover ring-1 ring-line" />
              <div className="min-w-0 flex-1">
                <p className="font-display text-lg font-semibold">{v.nome}</p>
                <p className="mt-1 text-sm text-ink-soft">{tipoVantagem[v.tipo]}</p>
              </div>
              <Preco valor={v.custo} />
              <IconButton label={`Remover ${v.nome}`} icon={TrashIcon} tone="danger" onClick={() => onRemover(v)} />
            </li>
          ))}
        </ul>
      </Panel>
    </section>
  )
}

// HU10: na troca presencial, a empresa confere o código que o aluno apresenta.
function Resgates({ resgates }: { resgates: Resgate[] | null }) {
  const [codigo, setCodigo] = useState('')
  if (!resgates) return <Carregando />
  if (resgates.length === 0) return <Vazio>Nenhuma vantagem sua foi resgatada ainda.</Vazio>

  const procurado = codigo.replace(/[^0-9a-z]/gi, '').toUpperCase()
  const achado = procurado.length === 10 ? resgates.find((r) => r.codigoCupom.replace('-', '') === procurado) : undefined

  return (
    <div className="grid gap-6">
      <Panel className="grid gap-4 px-6 py-7 md:px-9">
        <TextField
          label="Conferir código do cupom"
          name="codigo"
          autoComplete="off"
          autoCapitalize="characters"
          placeholder="ABCDE-23456"
          value={codigo}
          onChange={(e) => setCodigo(e.target.value)}
          hint="Digite o código que o aluno apresentou."
        />
        {procurado.length === 10 &&
          (achado ? (
            <p role="status" className="flex items-start gap-2.5">
              <CheckCircleIcon size={22} weight="fill" className="mt-0.5 shrink-0" />
              <span>
                Código válido: <strong className="font-semibold">{achado.vantagem}</strong> para {achado.aluno.nome}, resgatado em{' '}
                {quando(achado.data)}.
              </span>
            </p>
          ) : (
            <p role="status" className="flex items-start gap-2.5 text-danger">
              <XCircleIcon size={22} weight="fill" className="mt-0.5 shrink-0" />
              Nenhum resgate das suas vantagens tem este código.
            </p>
          ))}
      </Panel>

      <Panel>
        <ul className="divide-y divide-line">
          {resgates.map((r) => (
            <li key={r.id} className="flex flex-col gap-3 px-6 py-5 sm:flex-row sm:items-center md:px-8">
              <div className="min-w-0 flex-1">
                <p className="font-display text-lg font-semibold">{r.vantagem}</p>
                <p className="mt-1 text-ink-soft">
                  {r.aluno.nome} · {r.aluno.email}
                </p>
                <p className="mt-1 text-sm text-ink-soft">
                  {r.aluno.curso}, {r.aluno.instituicao} · {quando(r.data)} · {coins(r.valor)}
                </p>
              </div>
              <span className="font-display text-xl font-extrabold tracking-[0.12em]">{r.codigoCupom}</span>
            </li>
          ))}
        </ul>
      </Panel>
    </div>
  )
}

function ConfirmarRemocao({ nome, onCancel, onConfirm }: { nome: string; onCancel: () => void; onConfirm: () => Promise<void> }) {
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  const confirmar = async () => {
    setEnviando(true)
    setErro(null)
    try {
      await onConfirm()
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Algo deu errado. Tente de novo.')
      setEnviando(false)
    }
  }

  return (
    <div className="grid gap-6">
      {erro && <FormAlert>{erro}</FormAlert>}
      <p className="text-lg leading-relaxed">
        <strong className="font-semibold">{nome}</strong> sai do catálogo e os alunos deixam de poder trocá-la.
      </p>
      <div className="flex flex-wrap justify-end gap-3">
        <ActionButton variant="outline" onClick={onCancel}>
          Cancelar
        </ActionButton>
        <ActionButton variant="danger" onClick={confirmar} disabled={enviando}>
          {enviando ? 'Removendo…' : 'Remover'}
        </ActionButton>
      </div>
    </div>
  )
}
