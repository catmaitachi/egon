import { CaretRightIcon } from '@phosphor-icons/react'
import { Link } from 'react-router'
import { coinName, coins, routes } from '../../content'
import { api, type Resumo } from '../../lib/api'
import { useLoad } from '../../lib/useLoad'
import { Balance } from '../../components/Balance'
import { Bento, Bloco, Coluna } from '../../components/Bento'
import { ActionButton, ActionLink } from '../../components/controls'
import { FormAlert } from '../../components/fields'

const fmt = new Intl.NumberFormat('pt-BR')
const mesCurto = new Intl.DateTimeFormat('pt-BR', { month: 'short', timeZone: 'UTC' })
const mesLongo = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric', timeZone: 'UTC' })
const dataDoMes = (mes: string) => new Date(`${mes}-01T12:00:00Z`)

// O painel do admin. Blocos: quantos Egos existem e onde estão (a conta tem de fechar);
// quem está no sistema; o reconhecimento está acontecendo (envios por mês); por instituição.
export function PainelAdmin() {
  const resumo = useLoad(api.resumo)
  const r = resumo.data

  if (resumo.error) {
    return (
      <div className="grid justify-items-start gap-4">
        <FormAlert>{resumo.error.message}</FormAlert>
        <ActionButton variant="outline" onClick={resumo.reload}>
          Tentar de novo
        </ActionButton>
      </div>
    )
  }

  return (
    <Bento>
      <Coluna className="lg:col-span-7">
        <Bloco titulo={`Onde estão os ${coinName(2)}`} destaque className="order-1 md:col-span-6 lg:order-none">
          {r ? <Egos egos={r.egos} /> : <Carregando />}
        </Bloco>
        <Bloco titulo="Envios por mês" elastico="esticar" className="order-3 md:col-span-6 lg:order-none">
          {r ? <EnviosPorMes meses={r.enviosPorMes} /> : <Carregando />}
        </Bloco>
      </Coluna>
      <Coluna className="lg:col-span-5">
        <Bloco titulo="Quem está no sistema" flush className="order-2 md:col-span-6 lg:order-none">
          {r ? <Contagens contagens={r.contagens} /> : <Carregando />}
        </Bloco>
        <Bloco titulo="Por instituição" flush elastico="rolar" className="order-4 md:col-span-6 lg:order-none">
          {r ? <PorInstituicao linhas={r.porInstituicao} /> : <Carregando />}
        </Bloco>
      </Coluna>
    </Bento>
  )
}

function Carregando() {
  return (
    <p role="status" className="p-5 text-ink-soft sm:p-7">
      Carregando…
    </p>
  )
}

// Só a cota cria Ego e só o resgate o tira: o que entrou está com professores, nas carteiras dos
// alunos, reservado em repasses que esperam aprovação ou gasto em resgates. A barra mostra as quatro partes.
function Egos({ egos }: { egos: Resumo['egos'] }) {
  const partes = [
    { nome: 'Com professores', valor: egos.comProfessores, cor: 'bg-snow' },
    { nome: 'Nas carteiras dos alunos', valor: egos.comAlunos, cor: 'bg-snow/60' },
    { nome: 'Reservados em repasses', valor: egos.reservados, cor: 'bg-snow/25' },
    { nome: 'Gastos em resgates', valor: egos.resgatados, cor: 'bg-snow/10' },
  ]
  const fecha = egos.emitidos === egos.comProfessores + egos.comAlunos + egos.reservados + egos.resgatados
  return (
    <div>
      <Balance value={egos.emitidos} label="Emitidos pela cota" />
      <div aria-hidden className="mt-6 flex h-3 overflow-hidden rounded-full bg-snow/10">
        {egos.emitidos > 0 &&
          partes.map((p) => <span key={p.nome} className={p.cor} style={{ width: `${(p.valor / egos.emitidos) * 100}%` }} />)}
      </div>
      <dl className="mt-5 grid grid-cols-2 gap-3 @xl:grid-cols-4">
        {partes.map((p) => (
          <div key={p.nome}>
            <dt className="flex items-center gap-2 text-sm text-ink-soft">
              <span aria-hidden className={`size-2.5 rounded-full ${p.cor}`} />
              {p.nome}
            </dt>
            <dd className="mt-1 font-medium tabular-nums">{coins(p.valor)}</dd>
          </div>
        ))}
      </dl>
      <p className={`mt-5 text-sm ${fecha ? 'text-ink-soft' : 'text-danger'}`}>
        {fecha
          ? `A conta fecha: ${fmt.format(egos.emitidos)} = ${fmt.format(egos.comProfessores)} + ${fmt.format(egos.comAlunos)} + ${fmt.format(egos.reservados)} + ${fmt.format(egos.resgatados)}.`
          : `A conta não fecha: há ${coinName(2)} fora do lugar. Verifique o banco antes de qualquer envio.`}
      </p>
    </div>
  )
}

function Contagens({ contagens: c }: { contagens: Resumo['contagens'] }) {
  const itens = [
    { rotulo: 'Instituições', valor: c.instituicoes, to: routes.admin.instituicoes },
    { rotulo: 'Cursos', valor: c.cursos, to: routes.admin.cursos },
    { rotulo: 'Professores', valor: c.professores, to: routes.admin.professores },
    { rotulo: 'Alunos', valor: c.alunos, to: routes.admin.alunos },
    { rotulo: 'Empresas', valor: c.empresas, to: routes.admin.empresas },
  ]
  return (
    <>
      <ul className="divide-y divide-line">
        {itens.map((i) => (
          <li key={i.rotulo}>
            <Link
              to={i.to}
              className="group flex items-center justify-between gap-4 rounded-[20px] px-5 py-3.5 transition-colors duration-150 hover:bg-snow/[0.04] sm:px-7"
            >
              <span>{i.rotulo}</span>
              <span className="flex items-center gap-2 font-medium tabular-nums">
                {fmt.format(i.valor)}
                <CaretRightIcon size={16} aria-hidden className="text-ink-soft transition-transform duration-200 group-hover:translate-x-0.5" />
              </span>
            </Link>
          </li>
        ))}
        <li className="flex items-center justify-between gap-4 px-5 py-3.5 sm:px-7">
          <span>Vantagens publicadas</span>
          <span className="pr-6 font-medium tabular-nums">{fmt.format(c.vantagens)}</span>
        </li>
      </ul>
      <div className="flex flex-wrap gap-3 border-t border-line px-5 py-5 sm:px-7">
        <ActionLink to={routes.admin.instituicoes} variant="outline">
          Cadastrar instituição
        </ActionLink>
        <ActionLink to={routes.admin.professores} variant="outline">
          Cadastrar professor
        </ActionLink>
      </div>
    </>
  )
}

// Seis meses, o atual por último. As barras são decoração; a lista diz os números.
function EnviosPorMes({ meses }: { meses: Resumo['enviosPorMes'] }) {
  const maior = Math.max(1, ...meses.map((m) => m.valor))
  const total = meses.reduce((s, m) => s + m.quantidade, 0)
  return (
    <div>
      <ol className="grid grid-cols-6 items-end gap-2 @md:gap-4">
        {meses.map((m) => (
          <li key={m.mes} className="flex flex-col items-center gap-2">
            <span className="text-xs text-ink-soft tabular-nums @md:text-sm" aria-hidden>
              {fmt.format(m.valor)}
            </span>
            <span aria-hidden className="flex h-32 w-full items-end justify-center border-b border-line">
              {/* Barra fina; mês sem envio fica só na linha de base. */}
              {m.valor > 0 && <span className="w-2.5 rounded-full bg-snow @md:w-3" style={{ height: `${Math.max(6, (m.valor / maior) * 100)}%` }} />}
            </span>
            <span className="text-xs text-ink-soft capitalize @md:text-sm">
              <span aria-hidden>{mesCurto.format(dataDoMes(m.mes)).replace('.', '')}</span>
              <span className="sr-only">
                {mesLongo.format(dataDoMes(m.mes))}: {m.quantidade} {m.quantidade === 1 ? 'envio' : 'envios'}, {coins(m.valor)}
              </span>
            </span>
          </li>
        ))}
      </ol>
      <p className="mt-4 text-sm text-ink-soft">
        {total === 0 ? 'Nenhum envio nos últimos seis meses.' : `${total} ${total === 1 ? 'envio' : 'envios'} nos últimos seis meses.`}
      </p>
    </div>
  )
}

function PorInstituicao({ linhas }: { linhas: Resumo['porInstituicao'] }) {
  if (linhas.length === 0) return <p className="p-5 text-ink-soft sm:p-7">Nenhuma instituição cadastrada.</p>
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left">
        <caption className="sr-only">Alunos, professores e {coinName(2)} enviados por instituição</caption>
        <thead className="text-sm text-ink-soft">
          <tr>
            <th scope="col" className="px-5 py-3 font-normal sm:pl-7">Instituição</th>
            <th scope="col" className="px-2 py-3 text-right font-normal">Alunos</th>
            <th scope="col" className="px-2 py-3 text-right font-normal">Prof.</th>
            <th scope="col" className="py-3 pr-5 pl-2 text-right font-normal sm:pr-7">Enviados</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-line border-t border-line">
          {linhas.map((l) => (
            <tr key={l.id}>
              <th scope="row" className="px-5 py-3 font-medium break-words sm:pl-7">{l.nome}</th>
              <td className="px-2 py-3 text-right tabular-nums">{fmt.format(l.alunos)}</td>
              <td className="px-2 py-3 text-right tabular-nums">{fmt.format(l.professores)}</td>
              <td className="py-3 pr-5 pl-2 text-right tabular-nums sm:pr-7">{fmt.format(l.enviado)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
