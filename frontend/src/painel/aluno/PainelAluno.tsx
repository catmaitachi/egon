import { useRef, useState } from 'react'
import { coinName, coins, notifications } from '../../content'
import { api, fotoUrl, type Cupom, type Movimento, type VantagemCatalogo } from '../../lib/api'
import { useConta } from '../../lib/sessao'
import { useLiveNotices } from '../../lib/useLiveNotices'
import { useAcao } from '../../lib/useAcao'
import { useLoad } from '../../lib/useLoad'
import { useNotificacoes } from '../../lib/useNotificacoes'
import { Balance } from '../../components/Balance'
import { Bento, Bloco, Coluna } from '../../components/Bento'
import { ActionButton } from '../../components/controls'
import { LiveNotices, LiveStatus } from '../../components/LiveNotices'
import { ListState, rowClass } from '../../components/states'
import { ExtratoLista } from '../Extrato'
import { Preco } from '../Preco'
import { resumoDasRegras } from '../textos'
import { ConfirmarResgate, CupomDialog, MeusCupons } from './Cupons'
import { RepassarEgos } from './RepassarEgos'

// HU11 e o repasse entre colegas. Blocos, do mais importante ao menos: quanto eu tenho; o que
// chegou e saiu; repassar; repasses esperando o professor; o que já dá para trocar.
export function PainelAluno() {
  const conta = useConta()
  const extrato = useLoad(() => api.alunos.extrato(conta.id), conta.id)
  const catalogo = useLoad(api.vantagens.catalogo)
  const cupons = useLoad(() => api.alunos.cupons(conta.id), conta.id)
  const [resgatando, setResgatando] = useState<VantagemCatalogo | null>(null)
  const [aberto, setAberto] = useState<Cupom | null>(null)
  const avisos = useLiveNotices()
  // O mesmo evento não aparece duas vezes (resposta da ação e WebSocket, ou abas).
  const vistos = useRef(new Set<string>())

  const estado = useNotificacoes({
    onMensagem: (n) => {
      const id =
        n.tipo === 'TRANSFERENCIA' ? `${n.transferencia.id}:${n.evento}`
        : n.tipo === 'CUPOM' ? `${n.cupom.codigo}:${n.evento}`
        : n.transacao.id
      if (vistos.current.has(id)) return
      vistos.current.add(id)
      // Envio ou repasse mexe nas carteiras: o extrato inteiro vem de novo, do servidor.
      extrato.reload()
      if (n.tipo === 'MOEDAS_RECEBIDAS') avisos.push(id, notifications.aluno(n))
      if (n.tipo === 'TRANSFERENCIA') avisos.push(id, notifications.repasseAluno(n, n.transferencia.remetente.id === conta.id))
      if (n.tipo === 'CUPOM') {
        cupons.reload()
        avisos.push(id, notifications.cupomAluno(n))
      }
    },
    // Com a conexão fechada algo pode ter chegado sem aviso: o extrato recarrega a cada abertura.
    onConectar: () => {
      extrato.reload()
      cupons.reload()
    },
  })

  const e = extrato.data
  const pendentes = e && e.movimentos.filter((m) => m.tipo === 'TRANSFERENCIA_ENVIADA' && m.status === 'PENDENTE')
  const carregaMovimentos = { ...extrato, data: e?.movimentos ?? null }

  return (
    <>
      <Bento>
        <Coluna className="lg:col-span-5">
          <Bloco titulo="Quanto eu tenho" destaque className="order-1 md:col-span-6 lg:order-none">
            {e ? <Balance value={e.saldo} label="Saldo total" /> : <Esqueleto />}
            {e && e.carteiras.length > 0 && (
              <ul className="mt-6 divide-y divide-line border-t border-line">
                {e.carteiras.map((c) => (
                  <li key={c.professor.id} className="flex items-baseline justify-between gap-4 py-3">
                    <span className="min-w-0">
                      <span className="block font-medium break-words">De {c.professor.nome}</span>
                      <span className="block text-sm text-ink-soft">{resumoDasRegras(c.regras)}</span>
                    </span>
                    <span className="shrink-0 font-medium tabular-nums">{coins(c.saldo)}</span>
                  </li>
                ))}
              </ul>
            )}
            {e && e.carteiras.length === 0 && (
              <p className="mt-4 text-ink-soft">Os {coinName(2)} chegam quando um professor reconhece seu esforço em aula.</p>
            )}
          </Bloco>

          <Bloco titulo={`Repassar ${coinName(2)}`} className="order-3 md:col-span-6 lg:order-none">
            <RepassarEgos aluno={conta.id} carteiras={e?.carteiras ?? null} onRepassado={extrato.reload} />
          </Bloco>

          <Bloco titulo="Esperando o professor" flush elastico="esticar" className="order-4 md:col-span-6 lg:order-none">
            <Pendentes itens={pendentes} erro={extrato.error?.message} onRetry={extrato.reload} onCancelado={extrato.reload} />
          </Bloco>
        </Coluna>

        <Coluna className="lg:col-span-7">
          <Bloco
            titulo="Extrato"
            flush
            acoes={
              <div role="status">
                <LiveStatus estado={estado} />
              </div>
            }
            elastico="rolar"
            className="order-2 md:col-span-6 lg:order-none"
          >
            <ExtratoLista
              movimentos={carregaMovimentos}
              vazio={`Nenhum movimento ainda. Os ${coinName(2)} que você receber aparecem aqui, com a mensagem de quem enviou.`}
            />
          </Bloco>

          <Bloco titulo="O que já dá para trocar" flush className="order-5 md:col-span-6 lg:order-none">
            <Catalogo catalogo={catalogo} saldo={e?.saldo ?? null} onResgatar={setResgatando} />
          </Bloco>

          <Bloco titulo="Meus cupons" flush className="order-6 md:col-span-6 lg:order-none">
            <MeusCupons cupons={cupons} onAbrir={setAberto} />
          </Bloco>
        </Coluna>
      </Bento>
      <ConfirmarResgate
        aluno={conta.id}
        vantagem={resgatando}
        onCancel={() => setResgatando(null)}
        onResgatado={(c) => {
          // O aviso do WebSocket deste resgate seria repetido: o cupom já abre na tela.
          vistos.current.add(`${c.codigo}:EMITIDO`)
          setResgatando(null)
          setAberto(c)
          extrato.reload()
          cupons.reload()
        }}
      />
      <CupomDialog cupom={aberto} onClose={() => setAberto(null)} />
      <LiveNotices items={avisos.items} onClose={avisos.close} />
    </>
  )
}

function Esqueleto() {
  return (
    <div aria-hidden>
      <span className="block h-3 w-16 rounded-full bg-snow/5 motion-safe:animate-pulse" />
      <span className="mt-3 block h-11 w-44 rounded-full bg-snow/8 motion-safe:animate-pulse" />
    </div>
  )
}

type PendentesProps = { itens: Movimento[] | null; erro?: string; onRetry: () => void; onCancelado: () => void }

// Repasses que esperam o professor de origem. Os Egos já saíram (reservados) e voltam se cancelar.
function Pendentes({ itens, erro, onRetry, onCancelado }: PendentesProps) {
  return (
    <ListState
      items={itens}
      getKey={(m) => m.id}
      loading="Carregando repasses…"
      empty={{ text: 'Nenhum repasse esperando aprovação.' }}
      error={erro}
      onRetry={onRetry}
    >
      {(m) => <Pendente m={m} onCancelado={onCancelado} />}
    </ListState>
  )
}

function Pendente({ m, onCancelado }: { m: Movimento; onCancelado: () => void }) {
  return (
    <div className={`${rowClass} flex flex-wrap items-center justify-between gap-3`}>
      <p className="min-w-0">
        <span className="block font-medium break-words">
          {coins(m.valor)} para {m.pessoa?.nome}
        </span>
        <span className="block text-sm text-ink-soft">Espera {m.origem?.nome}</span>
      </p>
      <Cancelar id={m.id} onCancelado={onCancelado} />
    </div>
  )
}

function Cancelar({ id, onCancelado }: { id: string; onCancelado: () => void }) {
  const cancelar = useAcao(() => api.transferencias.cancelar(id).then(onCancelado))
  return (
    <div className="grid justify-items-end gap-1">
      <ActionButton variant="outline" busy={cancelar.busy} onClick={cancelar.run}>
        Cancelar
      </ActionButton>
      {cancelar.erro && <p className="text-sm text-danger">{cancelar.erro}</p>}
    </div>
  )
}

type CatalogoProps = {
  catalogo: ReturnType<typeof useLoad<VantagemCatalogo[]>>
  saldo: number | null
  onResgatar: (v: VantagemCatalogo) => void
}

// HU09: as vantagens que cabem no saldo agora, das mais baratas às mais caras, prontas para resgatar.
function Catalogo({ catalogo, saldo, onResgatar }: CatalogoProps) {
  const cabem = catalogo.data && saldo !== null ? catalogo.data.filter((v) => v.custo <= saldo) : null
  const faltam = catalogo.data && saldo !== null ? catalogo.data.length - (cabem?.length ?? 0) : 0
  const maisBarata = catalogo.data?.[0]
  return (
    <>
      <ListState
        items={cabem}
        getKey={(v) => v.id}
        loading="Carregando vantagens…"
        empty={{
          text: !catalogo.data?.length
            ? 'As empresas parceiras ainda não publicaram vantagens.'
            : `Nenhuma vantagem cabe no seu saldo ainda. A mais barata custa ${coins(maisBarata!.custo)}.`,
        }}
        error={catalogo.error?.message}
        onRetry={catalogo.reload}
      >
        {(v) => (
          <div className={`${rowClass} flex items-center gap-4`}>
            <img src={fotoUrl(v)} alt="" loading="lazy" className="size-16 shrink-0 rounded-[20px] object-cover ring-1 ring-line" />
            <div className="min-w-0 flex-1">
              <p className="font-medium break-words">{v.nome}</p>
              <p className="text-sm text-ink-soft">{v.empresa.nome}</p>
            </div>
            <div className="flex flex-col items-end gap-2 @md:flex-row @md:items-center @md:gap-4">
              <Preco valor={v.custo} />
              <ActionButton variant="outline" onClick={() => onResgatar(v)}>
                Resgatar
              </ActionButton>
            </div>
          </div>
        )}
      </ListState>
      {faltam > 0 && cabem && cabem.length > 0 && (
        <p className={`${rowClass} border-t border-line text-sm text-ink-soft`}>
          Mais {faltam} {faltam === 1 ? 'vantagem pede' : 'vantagens pedem'} mais {coinName(2)}.
        </p>
      )}
    </>
  )
}
