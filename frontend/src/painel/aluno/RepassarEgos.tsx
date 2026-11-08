import { useState, type FormEvent } from 'react'
import { coinName, coins } from '../../content'
import { api, ApiError, type Carteira, type Transferencia } from '../../lib/api'
import { useFormSubmit } from '../../lib/useFormSubmit'
import { useLoad } from '../../lib/useLoad'
import { ActionButton } from '../../components/controls'
import { FormAlert, SelectField, TextAreaField, TextField } from '../../components/fields'
import { Notice } from '../../components/states'
import { Escolha } from '../Escolha'
import { validarRepasse } from '../regras'
import { resumoDasRegras } from '../textos'

const MOTIVO_MAX = 500

type Props = { aluno: string; carteiras: Carteira[] | null; onRepassado: (t: Transferencia) => void }

// Repassar Egos a um colega, saindo da carteira de um professor, sob as regras dele.
export function RepassarEgos({ aluno, carteiras, onRepassado }: Props) {
  const colegas = useLoad(() => api.alunos.colegas(aluno), aluno)
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()
  const [escolhido, setProfessorId] = useState('')
  const [destinatarioId, setDestinatarioId] = useState('')
  const [valor, setValor] = useState('')
  const [motivo, setMotivo] = useState('')
  const [feito, setFeito] = useState<string | null>(null)

  const comSaldo = carteiras?.filter((c) => c.saldo > 0) ?? []
  // Com uma carteira só, ela já vem escolhida.
  const professorId = escolhido || (comSaldo.length === 1 ? comSaldo[0].professor.id : '')
  const carteira = comSaldo.find((c) => c.professor.id === professorId)

  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    setFeito(null)
    const ok = await submit(async () => {
      const campos = validarRepasse({ professorId, destinatarioId, valor, motivo }, carteira)
      if (Object.keys(campos).length) throw new ApiError('VALIDACAO', Object.values(campos)[0], campos)
      const t = await api.alunos.repassar(aluno, { professorId, destinatarioId, valor: Number(valor), motivo })
      setFeito(
        t.status === 'PENDENTE'
          ? `Pedido enviado. ${coins(t.valor)} ficam reservados até ${t.professor.nome} decidir.`
          : `${coins(t.valor)} foram para ${t.destinatario.nome}.`,
      )
      onRepassado(t)
    })
    if (!ok) return
    setDestinatarioId('')
    setValor('')
    setMotivo('')
  }

  if (carteiras && comSaldo.length === 0) {
    return <p className="text-ink-soft">Quando você receber {coinName(2)} de um professor, pode repassar parte deles a um colega aqui.</p>
  }

  return (
    <form ref={formRef} noValidate onSubmit={enviar} className="grid gap-5">
      <SelectField
        label="De qual professor"
        placeholder="Escolha a carteira"
        value={professorId}
        onChange={(e) => {
          setProfessorId(e.target.value)
          clear('professorId')
        }}
        error={errors.professorId}
        hint={carteira && resumoDasRegras(carteira.regras)}
      >
        {comSaldo.map((c) => (
          <option key={c.professor.id} value={c.professor.id}>
            {c.professor.nome}: {coins(c.saldo)}
          </option>
        ))}
      </SelectField>
      <Escolha
        campo="destinatarioId"
        label="Colega"
        pessoas={colegas}
        valor={destinatarioId}
        onChange={(id) => {
          setDestinatarioId(id)
          clear('destinatarioId')
        }}
        erro={errors.destinatarioId}
        vazio="Nenhum colega da sua instituição tem conta ainda."
      />
      <TextField
        label="Valor"
        inputMode="numeric"
        autoComplete="off"
        value={valor}
        onChange={(e) => {
          setValor(e.target.value)
          clear('valor')
        }}
        error={errors.valor}
        hint={carteira && `Disponível deste professor: ${coins(carteira.saldo)}.`}
      />
      <TextAreaField
        label="Motivo"
        placeholder="Por que o colega merece"
        maxLength={MOTIVO_MAX}
        value={motivo}
        onChange={(e) => {
          setMotivo(e.target.value)
          clear('motivo')
        }}
        error={errors.motivo}
        hint={`${motivo.length}/${MOTIVO_MAX}`}
      />
      {alert && <FormAlert>{alert}</FormAlert>}
      <Notice>{feito}</Notice>
      <div>
        <ActionButton type="submit" busy={sending} className="w-full @md:w-auto">
          {sending ? 'Enviando…' : `Repassar ${coinName(2)}`}
        </ActionButton>
      </div>
    </form>
  )
}
