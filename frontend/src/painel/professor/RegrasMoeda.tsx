import { useState, type FormEvent, type ReactNode } from 'react'
import { ArrowClockwiseIcon } from '@phosphor-icons/react'
import { coinName } from '../../content'
import { api, type Regras } from '../../lib/api'
import { useFormSubmit } from '../../lib/useFormSubmit'
import { useLoad } from '../../lib/useLoad'
import { ActionButton } from '../../components/controls'
import { FormAlert, TextField } from '../../components/fields'
import { Notice } from '../../components/states'

function Opcao({ label, dica, checked, onChange, disabled }: { label: string; dica: ReactNode; checked: boolean; onChange: (v: boolean) => void; disabled?: boolean }) {
  return (
    <label className={`flex cursor-pointer gap-3 ${disabled ? 'opacity-60' : ''}`}>
      <input
        type="checkbox"
        checked={checked}
        disabled={disabled}
        onChange={(e) => onChange(e.target.checked)}
        className="mt-1 size-5 shrink-0 cursor-pointer rounded accent-snow"
      />
      <span>
        <span className="block font-medium">{label}</span>
        <span className="block text-sm text-ink-soft">{dica}</span>
      </span>
    </label>
  )
}

// Texto do campo → número ou "sem limite" (em branco).
const limite = (v: string) => (v.trim() === '' ? null : Number(v))

// As regras de repasse dos Egos deste professor entre alunos. Valem para os próximos pedidos;
// os que já esperam aprovação continuam como estão.
export function RegrasMoeda({ professor }: { professor: string }) {
  const regras = useLoad(() => api.professores.regras(professor), professor)
  if (regras.error) {
    return (
      <div className="grid justify-items-start gap-4 p-5 sm:p-7">
        <FormAlert>{regras.error.message}</FormAlert>
        <ActionButton variant="outline" onClick={regras.reload}>
          <ArrowClockwiseIcon size={18} aria-hidden />
          Tentar de novo
        </ActionButton>
      </div>
    )
  }
  if (!regras.data) {
    return (
      <p role="status" className="p-5 text-ink-soft sm:p-7">
        Carregando regras…
      </p>
    )
  }
  return <Formulario professor={professor} inicial={regras.data} />
}

function Formulario({ professor, inicial }: { professor: string; inicial: Regras }) {
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()
  const [r, setR] = useState(inicial)
  const [maximo, setMaximo] = useState(inicial.valorMaximo?.toString() ?? '')
  const [mensal, setMensal] = useState(inicial.limiteMensal?.toString() ?? '')
  const [salvo, setSalvo] = useState(false)
  // Qualquer mudança depois de salvar apaga o "Regras salvas", que já não descreve o formulário.
  const mudar = (novas: Regras) => {
    setR(novas)
    setSalvo(false)
  }

  const salvar = async (e: FormEvent) => {
    e.preventDefault()
    const ok = await submit(async () => {
      setR(await api.professores.definirRegras(professor, { ...r, valorMaximo: limite(maximo), limiteMensal: limite(mensal) }))
    })
    setSalvo(ok)
  }

  return (
    <form ref={formRef} noValidate onSubmit={salvar} className="grid gap-5 p-5 sm:p-7">
      <Opcao
        label="Permitir repasse entre alunos"
        dica={`Seus alunos podem passar os ${coinName(2)} que você deu a colegas da sua instituição.`}
        checked={r.permiteRepasse}
        onChange={(v) => mudar({ ...r, permiteRepasse: v })}
      />
      <Opcao
        label="Exigir minha aprovação"
        dica={`O pedido espera você aprovar; os ${coinName(2)} ficam reservados até lá.`}
        checked={r.exigeAprovacao}
        disabled={!r.permiteRepasse}
        onChange={(v) => mudar({ ...r, exigeAprovacao: v })}
      />
      <Opcao
        label="Só para colegas do mesmo curso"
        dica="O colega precisa ser do curso de quem repassa."
        checked={r.mesmoCurso}
        disabled={!r.permiteRepasse}
        onChange={(v) => mudar({ ...r, mesmoCurso: v })}
      />
      <div className="grid gap-5 @lg:grid-cols-2">
        <TextField
          label="Máximo por repasse"
          inputMode="numeric"
          placeholder="Sem limite"
          disabled={!r.permiteRepasse}
          value={maximo}
          onChange={(e) => {
            setMaximo(e.target.value)
            setSalvo(false)
            clear('valorMaximo')
          }}
          error={errors.valorMaximo}
        />
        <TextField
          label="Máximo por aluno no mês"
          inputMode="numeric"
          placeholder="Sem limite"
          disabled={!r.permiteRepasse}
          value={mensal}
          onChange={(e) => {
            setMensal(e.target.value)
            setSalvo(false)
            clear('limiteMensal')
          }}
          error={errors.limiteMensal}
        />
      </div>
      {alert && <FormAlert>{alert}</FormAlert>}
      <Notice>{salvo && 'Regras salvas. Valem para os próximos pedidos.'}</Notice>
      <div>
        <ActionButton type="submit" variant="outline" busy={sending}>
          {sending ? 'Salvando…' : 'Salvar regras'}
        </ActionButton>
      </div>
    </form>
  )
}
