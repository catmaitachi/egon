import { useCallback, useEffect, useState } from 'react'
import { EnvelopeSimpleIcon, GiftIcon, ListBulletsIcon } from '@phosphor-icons/react'
import { coins, tipoVantagem } from '../content'
import { api, fotoUrl, type Aviso, type Extrato, type TrocaFeita, type Vantagem } from '../lib/api'
import { useTitle } from '../lib/useTitle'
import { ActionButton } from '../components/controls'
import { Dialog } from '../components/Dialog'
import { FormAlert } from '../components/form'
import { Abas, AvisosLista, Carregando, ExtratoLista, PainelShell, Preco, Saldo, Vazio } from '../components/Painel'
import { Notice } from '../components/Records'
import { PageTitle } from '../components/Shell'

type AbaAluno = 'vantagens' | 'extrato' | 'avisos'

const abas = [
  { id: 'vantagens' as const, label: 'Vantagens', icon: GiftIcon },
  { id: 'extrato' as const, label: 'Extrato', icon: ListBulletsIcon },
  { id: 'avisos' as const, label: 'E-mails', icon: EnvelopeSimpleIcon },
]

export function PainelAluno() {
  useTitle('Painel do aluno')
  return (
    <PainelShell perfil="ALUNO">
      <ConteudoAluno />
    </PainelShell>
  )
}

function ConteudoAluno() {
  const [aba, setAba] = useState<AbaAluno>('vantagens')
  const [extrato, setExtrato] = useState<Extrato | null>(null)
  const [vantagens, setVantagens] = useState<Vantagem[] | null>(null)
  const [avisos, setAvisos] = useState<Aviso[] | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [escolhida, setEscolhida] = useState<Vantagem | null>(null)
  const [feita, setFeita] = useState<TrocaFeita | null>(null)

  const carregar = useCallback(
    () =>
      Promise.all([api.alunos.extrato(), api.vantagens.listar(), api.avisos()]).then(
        ([e, v, a]) => {
          setExtrato(e)
          setVantagens(v)
          setAvisos(a)
        },
        (e: Error) => setErro(e.message),
      ),
    [],
  )

  useEffect(() => {
    carregar()
  }, [carregar])

  const saldo = extrato?.saldo ?? null

  const trocar = async (v: Vantagem) => {
    const resultado = await api.trocas.resgatar(v.id)
    setEscolhida(null)
    setFeita(resultado)
    carregar()
  }

  return (
    <>
      <PageTitle eyebrow="Painel do aluno" title="Seus Egos" />
      <div className="mt-8 grid gap-8">
        {erro && <FormAlert>{erro}</FormAlert>}
        <Saldo valor={saldo} rotulo="Saldo disponível" />
        <Abas abas={abas} atual={aba} onChange={setAba} />

        {aba === 'vantagens' && (
          <Catalogo vantagens={vantagens} saldo={saldo} onEscolher={setEscolhida} />
        )}
        {aba === 'extrato' && (
          <ExtratoLista
            lancamentos={extrato?.lancamentos ?? null}
            vazio="Nada por aqui ainda. Quando um professor reconhecer você, os Egos aparecem neste extrato."
          />
        )}
        {aba === 'avisos' && <AvisosLista avisos={avisos} />}
      </div>

      <Dialog open={escolhida !== null} onClose={() => setEscolhida(null)} title="Confirmar troca">
        {escolhida && saldo !== null && (
          <ConfirmarTroca vantagem={escolhida} saldo={saldo} onCancel={() => setEscolhida(null)} onConfirm={trocar} />
        )}
      </Dialog>

      <Dialog open={feita !== null} onClose={() => setFeita(null)} title="Troca feita">
        {feita && <CupomGerado troca={feita} onClose={() => setFeita(null)} />}
      </Dialog>
    </>
  )
}

// HU09: os cards mostram foto, descrição, empresa e custo, e quais o saldo já alcança.
function Catalogo({
  vantagens,
  saldo,
  onEscolher,
}: {
  vantagens: Vantagem[] | null
  saldo: number | null
  onEscolher: (v: Vantagem) => void
}) {
  if (!vantagens || saldo === null) return <Carregando />
  if (vantagens.length === 0) return <Vazio>Nenhuma empresa publicou vantagens ainda.</Vazio>
  const alcance = vantagens.filter((v) => v.custo <= saldo).length
  return (
    <div className="grid gap-4">
      <p className="text-sm text-ink-soft">
        {alcance === 0
          ? 'Seu saldo ainda não alcança nenhuma vantagem.'
          : alcance === 1
            ? '1 vantagem ao alcance do seu saldo.'
            : `${alcance} vantagens ao alcance do seu saldo.`}
      </p>
      <ul className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {vantagens.map((v) => {
          const falta = v.custo - saldo
          return (
            <li key={v.id}>
              <article
                className={`flex h-full flex-col overflow-hidden rounded-[20px] bg-coal ring-1 transition-shadow duration-300 ${
                  falta <= 0 ? 'ring-snow/40' : 'ring-line'
                }`}
              >
                <img src={fotoUrl(v)} alt={v.nome} loading="lazy" className="aspect-[4/3] w-full bg-night object-cover" />
                <div className="flex flex-1 flex-col gap-3 p-6">
                  <p className="font-display text-xs font-semibold tracking-[0.14em] text-ink-soft uppercase">
                    {tipoVantagem[v.tipo]} · {v.empresa.nome}
                  </p>
                  <h3 className="font-display text-xl leading-tight font-bold">{v.nome}</h3>
                  <p className="leading-relaxed text-ink-soft">{v.descricao}</p>
                  <div className="mt-auto flex flex-wrap items-center justify-between gap-3 pt-3">
                    <Preco valor={v.custo} />
                    {falta <= 0 ? (
                      <ActionButton onClick={() => onEscolher(v)}>Trocar</ActionButton>
                    ) : (
                      <span className="text-sm text-ink-soft">Faltam {coins(falta)}</span>
                    )}
                  </div>
                </div>
              </article>
            </li>
          )
        })}
      </ul>
    </div>
  )
}

// Montado a cada abertura do diálogo, então o estado de envio recomeça.
function ConfirmarTroca({
  vantagem,
  saldo,
  onCancel,
  onConfirm,
}: {
  vantagem: Vantagem
  saldo: number
  onCancel: () => void
  onConfirm: (v: Vantagem) => Promise<void>
}) {
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  const confirmar = async () => {
    setEnviando(true)
    setErro(null)
    try {
      await onConfirm(vantagem)
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Algo deu errado. Tente de novo.')
      setEnviando(false)
    }
  }

  return (
    <div className="grid gap-6">
      {erro && <FormAlert>{erro}</FormAlert>}
      <p className="text-lg leading-relaxed">
        Trocar <strong className="font-semibold">{coins(vantagem.custo)}</strong> por{' '}
        <strong className="font-semibold">{vantagem.nome}</strong>, de {vantagem.empresa.nome}? Seu saldo passa de{' '}
        {coins(saldo)} para {coins(saldo - vantagem.custo)}.
      </p>
      <div className="flex flex-wrap justify-end gap-3">
        <ActionButton variant="outline" onClick={onCancel}>
          Cancelar
        </ActionButton>
        <ActionButton onClick={confirmar} disabled={enviando}>
          {enviando ? 'Trocando…' : 'Confirmar troca'}
        </ActionButton>
      </div>
    </div>
  )
}

// HU10: o código que o aluno apresenta na empresa. O mesmo vai por e-mail para os dois lados.
function CupomGerado({ troca, onClose }: { troca: TrocaFeita; onClose: () => void }) {
  return (
    <div className="grid gap-6">
      <Notice>
        {troca.vantagem}, de {troca.empresa}. Debitamos {coins(troca.valor)}; seu saldo agora é {coins(troca.saldoAluno)}.
      </Notice>
      <div className="rounded-[20px] bg-night px-6 py-8 text-center ring-1 ring-snow/40">
        <p className="font-display text-xs font-semibold tracking-[0.18em] text-ink-soft uppercase">Código do cupom</p>
        <p className="mt-3 font-display text-3xl font-extrabold tracking-[0.12em] select-all md:text-4xl">{troca.codigoCupom}</p>
      </div>
      <p className="leading-relaxed text-ink-soft">
        Apresente este código à empresa parceira para usar a vantagem. Ele também foi para o seu e-mail, e a empresa
        recebeu uma cópia para conferir.
      </p>
      <div className="flex justify-end">
        <ActionButton variant="outline" onClick={onClose}>
          Fechar
        </ActionButton>
      </div>
    </div>
  )
}
