import { coins } from '../../content'
import { api, type ApiError, type Transferencia } from '../../lib/api'
import { dataHora } from '../../lib/format'
import { useAcao } from '../../lib/useAcao'
import { ActionButton } from '../../components/controls'
import { ListState, rowClass } from '../../components/states'

type Props = {
  pedidos: { data: Transferencia[] | null; error: ApiError | null; reload: () => void }
  onDecidido: () => void
}

// Repasses dos seus Egos esperando sua decisão. Aprovar credita o colega; recusar devolve a quem pediu.
export function Pedidos({ pedidos, onDecidido }: Props) {
  const pendentes = pedidos.data?.filter((t) => t.status === 'PENDENTE') ?? null
  return (
    <ListState
      items={pendentes}
      getKey={(t) => t.id}
      loading="Carregando pedidos…"
      empty={{ text: 'Nenhum pedido de repasse esperando você.' }}
      error={pedidos.error?.message}
      onRetry={pedidos.reload}
    >
      {(t) => <Pedido t={t} onDecidido={onDecidido} />}
    </ListState>
  )
}

function Pedido({ t, onDecidido }: { t: Transferencia; onDecidido: () => void }) {
  const aprovar = useAcao(() => api.transferencias.aprovar(t.id).then(onDecidido))
  const recusar = useAcao(() => api.transferencias.recusar(t.id).then(onDecidido))
  const erro = aprovar.erro ?? recusar.erro
  return (
    <div className={`${rowClass} grid gap-3`}>
      <div>
        <p className="font-medium break-words">
          {t.remetente.nome} → {t.destinatario.nome}: {coins(t.valor)}
        </p>
        <p className="text-sm text-ink-soft">
          <time dateTime={t.data}>{dataHora(t.data)}</time>
        </p>
        <p className="mt-2 leading-relaxed break-words">“{t.motivo}”</p>
      </div>
      <div className="flex flex-wrap gap-3">
        <ActionButton variant="outline" busy={aprovar.busy} disabled={recusar.busy} onClick={aprovar.run}>
          Aprovar
        </ActionButton>
        <ActionButton variant="quiet" busy={recusar.busy} disabled={aprovar.busy} onClick={recusar.run}>
          Recusar
        </ActionButton>
      </div>
      {erro && (
        <p role="alert" className="text-sm text-danger">
          {erro}
        </p>
      )}
    </div>
  )
}
