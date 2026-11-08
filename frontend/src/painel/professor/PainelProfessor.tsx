import { useRef } from 'react'
import { coinName, coins, notifications } from '../../content'
import { api, type Extrato } from '../../lib/api'
import { useConta } from '../../lib/sessao'
import { useLiveNotices } from '../../lib/useLiveNotices'
import { useLoad } from '../../lib/useLoad'
import { useNotificacoes } from '../../lib/useNotificacoes'
import { Balance } from '../../components/Balance'
import { Bento, Bloco, Coluna } from '../../components/Bento'
import { LiveNotices, LiveStatus } from '../../components/LiveNotices'
import { EnviarEgos } from '../EnviarEgos'
import { ExtratoLista } from '../Extrato'
import { Pedidos } from './Pedidos'
import { RegrasMoeda } from './RegrasMoeda'

// HU05, HU06, HU12 e o repasse. Blocos: quanto ainda posso enviar; enviar; pedidos de repasse
// esperando decisão; quanto da cota do semestre já distribuí; regras dos meus Egos; extrato.
export function PainelProfessor() {
  const conta = useConta()
  const extrato = useLoad(() => api.professores.extrato(conta.id), conta.id)
  const pedidos = useLoad(() => api.professores.transferencias(conta.id), conta.id)
  const avisos = useLiveNotices()
  const vistos = useRef(new Set<string>())

  const marcar = (id: string) => {
    if (vistos.current.has(id)) return false
    vistos.current.add(id)
    return true
  }

  const estado = useNotificacoes({
    onMensagem: (n) => {
      if (n.tipo === 'ENVIO_CONFIRMADO' && marcar(n.transacao.id)) {
        extrato.reload()
        avisos.push(n.transacao.id, notifications.professor(n))
      }
      if (n.tipo === 'TRANSFERENCIA') {
        const id = `${n.transferencia.id}:${n.evento}`
        if (!marcar(id)) return
        pedidos.reload()
        avisos.push(id, notifications.repasseProfessor(n))
      }
    },
    onConectar: () => {
      extrato.reload()
      pedidos.reload()
    },
  })

  const e = extrato.data
  return (
    <>
      <Bento>
        <Coluna className="lg:col-span-5">
          <Bloco titulo="Quanto ainda posso enviar" destaque className="order-1 md:col-span-6 lg:order-none">
            {e ? <Balance value={e.saldo} label="Disponível" /> : <p role="status" className="text-ink-soft">Carregando saldo…</p>}
            <p className="mt-4 text-ink-soft">O que sobra no semestre acumula para o próximo.</p>
          </Bloco>

          <Bloco titulo="Pedidos de repasse" flush elastico="rolar" className="order-3 md:col-span-6 lg:order-none">
            <Pedidos pedidos={pedidos} onDecidido={pedidos.reload} />
          </Bloco>

          <Bloco titulo="Cota do semestre" className="order-4 md:col-span-6 lg:order-none">
            <Cota extrato={e} />
          </Bloco>
        </Coluna>

        <Coluna className="lg:col-span-7">
          <Bloco titulo={`Enviar ${coinName(2)}`} className="order-2 md:col-span-6 lg:order-none">
            <EnviarEgos
              professor={conta.id}
              saldo={e?.saldo ?? null}
              onEnviado={(t, saldo) => {
                if (!marcar(t.id)) return
                extrato.reload()
                avisos.push(t.id, notifications.professor({ transacao: t, saldo }))
              }}
            />
          </Bloco>

          <Bloco titulo={`Regras dos meus ${coinName(2)}`} flush elastico="esticar" className="order-5 md:col-span-6 lg:order-none">
            <RegrasMoeda professor={conta.id} />
          </Bloco>
        </Coluna>

        <Bloco
          titulo="Extrato"
          flush
          acoes={
            <div role="status">
              <LiveStatus estado={estado} />
            </div>
          }
          className="order-6 md:col-span-6 lg:col-span-12 lg:order-none"
        >
          <ExtratoLista
            movimentos={{ ...extrato, data: e?.movimentos ?? null }}
            vazio="Nenhum movimento ainda. A cota de cada semestre e os envios que você fizer aparecem aqui."
          />
        </Bloco>
      </Bento>
      <LiveNotices items={avisos.items} onClose={avisos.close} />
    </>
  )
}

// Quanto da cota mais recente já foi enviado: soma dos envios desde que ela entrou.
function Cota({ extrato }: { extrato: Extrato | null }) {
  if (!extrato) return <p role="status" className="text-ink-soft">Carregando…</p>
  const cota = extrato.movimentos.find((m) => m.tipo === 'COTA')
  if (!cota) return <p className="text-ink-soft">A cota de 1.000 {coinName(2)} entra a cada semestre.</p>
  const enviado = extrato.movimentos
    .filter((m) => m.tipo === 'ENVIO' && new Date(m.data) >= new Date(cota.data))
    .reduce((soma, m) => soma + m.valor, 0)
  const parte = Math.min(1, enviado / cota.valor)
  return (
    <div>
      <p className="text-lg">
        Distribuí <strong className="font-medium">{coins(enviado)}</strong> de {coins(cota.valor)} do semestre {cota.semestre}.
      </p>
      <div
        role="meter"
        aria-label="Cota do semestre distribuída"
        aria-valuemin={0}
        aria-valuemax={cota.valor}
        aria-valuenow={enviado}
        className="mt-4 h-2 overflow-hidden rounded-full bg-snow/10"
      >
        <div className="h-full origin-left rounded-full bg-snow transition-transform duration-300" style={{ transform: `scaleX(${parte})` }} />
      </div>
    </div>
  )
}
