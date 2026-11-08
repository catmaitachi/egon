import { useEffect, useState, type FormEvent } from 'react'
import { coins } from '../../content'
import { api, type Cupom } from '../../lib/api'
import { dataHora } from '../../lib/format'
import { useFormSubmit } from '../../lib/useFormSubmit'
import { ActionButton } from '../../components/controls'
import { FormAlert, TextField } from '../../components/fields'
import { Notice } from '../../components/states'

type Props = { empresa: string; inicial: string; onConfirmado: (c: Cupom) => void }

// HU10 na loja: a empresa digita o código (ou chega pelo link do QR, com ele preenchido), vê quem
// resgatou o quê e confirma a troca. Cupom já usado não confirma de novo.
export function ConferirCupom({ empresa, inicial, onConfirmado }: Props) {
  const { formRef, sending, errors, alert, submit, clear } = useFormSubmit()
  const [codigo, setCodigo] = useState(inicial)
  const [cupom, setCupom] = useState<Cupom | null>(null)
  const [confirmando, setConfirmando] = useState(false)
  const [erroConfirmar, setErroConfirmar] = useState<string | null>(null)
  const [feito, setFeito] = useState<string | null>(null)

  const buscar = (valor: string) =>
    submit(async () => {
      setCupom(null)
      setFeito(null)
      setErroConfirmar(null)
      setCupom(await api.empresas.buscarCupom(empresa, valor))
    })

  // Vindo do QR, o código já está na URL: confere sem a pessoa precisar clicar.
  useEffect(() => {
    if (inicial) buscar(inicial)
    // eslint-disable-next-line react-hooks/exhaustive-deps -- só na primeira montagem: o código veio pelo link do QR
  }, [])

  const enviar = (e: FormEvent) => {
    e.preventDefault()
    buscar(codigo)
  }

  const confirmar = async () => {
    if (!cupom) return
    setConfirmando(true)
    setErroConfirmar(null)
    try {
      const usado = await api.empresas.confirmarCupom(empresa, cupom.codigo)
      setCupom(usado)
      setFeito(`Troca confirmada: entregue ${usado.vantagem.nome} para ${usado.aluno.nome}.`)
      onConfirmado(usado)
    } catch (e) {
      setErroConfirmar(e instanceof Error ? e.message : 'Algo deu errado. Tente de novo.')
    } finally {
      setConfirmando(false)
    }
  }

  return (
    <div className="grid gap-5">
      <form ref={formRef} noValidate onSubmit={enviar} className="flex flex-col gap-3 @md:flex-row @md:items-start">
        <TextField
          label="Código do cupom"
          placeholder="EGON-XXXX-XXXX"
          autoComplete="off"
          autoCapitalize="characters"
          spellCheck={false}
          className="min-w-0 flex-1"
          value={codigo}
          error={errors.codigo}
          onChange={(e) => {
            setCodigo(e.target.value)
            clear('codigo')
          }}
        />
        <ActionButton type="submit" variant="outline" busy={sending} className="@md:mt-7">
          {sending ? 'Conferindo…' : 'Conferir'}
        </ActionButton>
      </form>
      {alert && <FormAlert>{alert}</FormAlert>}
      {cupom && (
        <div className="grid gap-4 rounded-[20px] p-5 ring-1 ring-line">
          <dl className="grid gap-3 @md:grid-cols-2">
            <div>
              <dt className="text-sm text-ink-soft">Vantagem</dt>
              <dd className="font-medium break-words">{cupom.vantagem.nome}</dd>
            </div>
            <div>
              <dt className="text-sm text-ink-soft">Aluno</dt>
              <dd className="font-medium break-words">{cupom.aluno.nome}</dd>
            </div>
            <div>
              <dt className="text-sm text-ink-soft">Resgatado em</dt>
              <dd>{dataHora(cupom.data)}, por {coins(cupom.valor)}</dd>
            </div>
            <div>
              <dt className="text-sm text-ink-soft">Situação</dt>
              <dd>{cupom.status === 'EMITIDO' ? 'Pronto para usar' : `Já usado em ${dataHora(cupom.dataUso ?? cupom.data)}`}</dd>
            </div>
          </dl>
          {erroConfirmar && <FormAlert>{erroConfirmar}</FormAlert>}
          <Notice>{feito}</Notice>
          {cupom.status === 'EMITIDO' && (
            <div>
              <ActionButton variant="outline" busy={confirmando} onClick={confirmar}>
                {confirmando ? 'Confirmando…' : 'Confirmar troca'}
              </ActionButton>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
