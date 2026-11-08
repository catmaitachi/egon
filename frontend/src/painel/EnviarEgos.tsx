import { useState, type FormEvent } from 'react'
import { coinName, coins } from '../content'
import { api, ApiError, type Transacao } from '../lib/api'
import { useFormSubmit } from '../lib/useFormSubmit'
import { useLoad } from '../lib/useLoad'
import { ActionButton } from '../components/controls'
import { FormAlert, TextAreaField, TextField } from '../components/fields'
import { Escolha } from './Escolha'
import { validarEnvio } from './regras'

const MOTIVO_MAX = 500

type EnviarEgosProps = {
  professor: string
  // null enquanto o saldo carrega.
  saldo: number | null
  onEnviado: (transacao: Transacao, saldo: number) => void
}

// HU06: escolher um aluno da instituição do professor, o valor e o motivo (obrigatório).
export function EnviarEgos({ professor, saldo, onEnviado }: EnviarEgosProps) {
  const alunos = useLoad(() => api.professores.alunos(professor), professor)
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()
  const [idAluno, setIdAluno] = useState('')
  const [valor, setValor] = useState('')
  const [motivo, setMotivo] = useState('')

  const enviar = async (e: FormEvent) => {
    e.preventDefault()
    if (saldo === null) return
    const ok = await submit(async () => {
      const campos = validarEnvio({ idAluno, valor, motivo }, saldo)
      // Mesmo caminho dos erros da API: mensagem embaixo do campo e foco no primeiro marcado.
      if (Object.keys(campos).length) throw new ApiError('VALIDACAO', Object.values(campos)[0], campos)
      const transacao = await api.professores.enviar(professor, { idAluno, valor: Number(valor), motivo })
      // A resposta não traz o saldo novo. Se o aviso do WebSocket chegou antes, este registro é ignorado.
      onEnviado(transacao, saldo - transacao.valor)
    })
    if (!ok) return
    setIdAluno('')
    setValor('')
    setMotivo('')
  }

  return (
    <form ref={formRef} noValidate onSubmit={enviar} className="grid gap-5">
      <Escolha
        campo="idAluno"
        label="Aluno"
        pessoas={alunos}
        valor={idAluno}
        onChange={(id) => {
          setIdAluno(id)
          clear('idAluno')
        }}
        erro={errors.idAluno}
        vazio="Nenhum aluno da sua instituição tem conta ainda."
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
        hint={saldo !== null && `Disponível: ${coins(saldo)}.`}
      />
      <TextAreaField
        label="Motivo"
        placeholder="O que o aluno fez para merecer"
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
      <div>
        <ActionButton type="submit" busy={sending} disabled={saldo === null} className="w-full @md:w-auto">
          {sending ? 'Enviando…' : `Enviar ${coinName(0)}`}
        </ActionButton>
      </div>
    </form>
  )
}
