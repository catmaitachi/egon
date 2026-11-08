import { useState } from 'react'
import { coinName, coins } from '../../content'
import { api, type Cupom, type VantagemCatalogo } from '../../lib/api'
import { dataHora } from '../../lib/format'
import { ActionButton } from '../../components/controls'
import { Dialog } from '../../components/Dialog'
import { FormAlert } from '../../components/fields'
import { QrCupom } from '../../components/QrCupom'
import { ListState, rowClass } from '../../components/states'

// O cupom para mostrar na loja: QR (leva a empresa direto à confirmação) e o código para ditar.
export function CupomDialog({ cupom, onClose }: { cupom: Cupom | null; onClose: () => void }) {
  return (
    <Dialog open={cupom !== null} onClose={onClose} title={cupom?.vantagem.nome ?? 'Cupom'} narrow>
      {cupom && (
        <div className="grid justify-items-center gap-5 text-center">
          <QrCupom codigo={cupom.codigo} />
          <p className="font-display text-2xl font-bold tracking-[0.06em] tabular-nums">{cupom.codigo}</p>
          {cupom.status === 'EMITIDO' ? (
            <p className="max-w-[30ch] text-ink-soft">
              Mostre na {cupom.empresa.nome}. A empresa lê o QR ou digita o código e confirma a troca.
            </p>
          ) : (
            <p className="text-ink-soft">Usado em {dataHora(cupom.dataUso ?? cupom.data)}.</p>
          )}
        </div>
      )}
    </Dialog>
  )
}

type ConfirmarProps = { aluno: string; vantagem: VantagemCatalogo | null; onCancel: () => void; onResgatado: (c: Cupom) => void }

// Resgatar é gastar: pede confirmação com o custo à vista. O conteúdo só monta aberto, então o
// erro e o "ocupado" recomeçam a cada vantagem.
export function ConfirmarResgate({ aluno, vantagem, onCancel, onResgatado }: ConfirmarProps) {
  return (
    <Dialog
      open={vantagem !== null}
      onClose={onCancel}
      title="Resgatar vantagem"
      description={vantagem && `${vantagem.nome}, na ${vantagem.empresa.nome}, por ${coins(vantagem.custo)}. Os ${coinName(2)} saem do seu saldo na hora e você recebe um cupom.`}
      narrow
    >
      {vantagem && <Acoes aluno={aluno} vantagem={vantagem} onCancel={onCancel} onResgatado={onResgatado} />}
    </Dialog>
  )
}

function Acoes({ aluno, vantagem, onCancel, onResgatado }: Omit<ConfirmarProps, 'vantagem'> & { vantagem: VantagemCatalogo }) {
  const [busy, setBusy] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const resgatar = async () => {
    setBusy(true)
    setErro(null)
    try {
      onResgatado(await api.alunos.resgatar(aluno, vantagem.id))
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Algo deu errado. Tente de novo.')
      setBusy(false)
    }
  }
  return (
    <div className="grid gap-6">
      {erro && <FormAlert>{erro}</FormAlert>}
      <div className="flex flex-wrap justify-end gap-3">
        <ActionButton variant="outline" onClick={onCancel}>
          Cancelar
        </ActionButton>
        <ActionButton busy={busy} onClick={resgatar}>
          {busy ? 'Resgatando…' : 'Resgatar'}
        </ActionButton>
      </div>
    </div>
  )
}

type MeusCuponsProps = {
  cupons: { data: Cupom[] | null; error: { message: string } | null; reload: () => void }
  onAbrir: (c: Cupom) => void
}

// Os cupons do aluno: os que esperam a loja primeiro, depois os já usados.
export function MeusCupons({ cupons, onAbrir }: MeusCuponsProps) {
  const ordenados = cupons.data?.toSorted((a, b) => (a.status === b.status ? 0 : a.status === 'EMITIDO' ? -1 : 1)) ?? null
  return (
    <ListState
      items={ordenados}
      getKey={(c) => c.codigo}
      loading="Carregando cupons…"
      empty={{ text: 'Nenhum cupom ainda. Quando resgatar uma vantagem, o cupom aparece aqui para mostrar na loja.' }}
      error={cupons.error?.message}
      onRetry={cupons.reload}
    >
      {(c) => (
        <div className={`${rowClass} flex flex-wrap items-center justify-between gap-3`}>
          <p className="min-w-0">
            <span className="block font-medium break-words">{c.vantagem.nome}</span>
            <span className="block text-sm text-ink-soft">
              {c.empresa.nome} · <span className="tabular-nums">{c.codigo}</span> ·{' '}
              {c.status === 'EMITIDO' ? 'Para usar' : `Usado em ${dataHora(c.dataUso ?? c.data)}`}
            </span>
          </p>
          <ActionButton variant={c.status === 'EMITIDO' ? 'outline' : 'quiet'} onClick={() => onAbrir(c)}>
            {c.status === 'EMITIDO' ? 'Mostrar cupom' : 'Ver'}
          </ActionButton>
        </div>
      )}
    </ListState>
  )
}
