import { useEffect, useId, useRef, useState } from 'react'
import { ImageIcon, WarningCircleIcon } from '@phosphor-icons/react'
import { fotoUrl, type TipoVantagem, type Vantagem, type VantagemDados } from '../../lib/api'
import { SelectField, TextAreaField, TextField } from '../../components/fields'
import { FormActions } from '../../forms/parts'
import type { Erros } from '../../forms/regras'
import { useCampos } from '../../forms/useCampos'
import { nomeDoTipo } from '../textos'

const FOTO_MAX = 2 * 1024 * 1024
const FOTO_TIPOS = ['image/jpeg', 'image/png', 'image/webp']
const DESCRICAO_MAX = 1000

// As regras da HU08 que dá para conferir antes de enviar; o servidor confere de novo (inclusive os bytes da foto).
function validar(v: Record<'nome' | 'descricao' | 'custo' | 'tipo', string>, foto: File | null, edicao: boolean): Erros {
  const erros: Erros = {}
  const custo = Number(v.custo)
  if (!v.nome.trim()) erros.nome = 'Informe o nome da vantagem.'
  else if (v.nome.trim().length > 100) erros.nome = 'Use até 100 caracteres.'
  if (!v.descricao.trim()) erros.descricao = 'Descreva a vantagem.'
  if (!v.custo.trim()) erros.custo = 'Informe o custo.'
  else if (!Number.isInteger(custo) || custo <= 0) erros.custo = 'Use um número inteiro maior que zero.'
  if (!v.tipo) erros.tipo = 'Escolha o tipo.'
  if (!foto && !edicao) erros.foto = 'Escolha uma foto da vantagem.'
  else if (foto && !FOTO_TIPOS.includes(foto.type)) erros.foto = 'Use uma imagem JPEG, PNG ou WebP.'
  else if (foto && foto.size > FOTO_MAX) erros.foto = 'A foto pode ter até 2 MB.'
  return erros
}

type Props = {
  // Sem vantagem é o cadastro; com vantagem, a edição (foto opcional: sem foto nova, fica a atual).
  vantagem?: Vantagem
  label: string
  busyLabel: string
  onSubmit: (dados: VantagemDados) => Promise<void>
  onCancel?: () => void
}

// HU08: nome, descrição, tipo, custo em Egos e foto.
export function VantagemForm({ vantagem, label, busyLabel, onSubmit, onCancel }: Props) {
  const edicao = vantagem !== undefined
  const [foto, setFoto] = useState<File | null>(null)
  const [erroFoto, setErroFoto] = useState<string | undefined>()
  const [previa, setPrevia] = useState<string | null>(null)
  const fotoId = useId()
  const { formRef, sending, alert, campo, enviar } = useCampos({
    nome: vantagem?.nome ?? '',
    descricao: vantagem?.descricao ?? '',
    custo: vantagem?.custo.toString() ?? '',
    tipo: vantagem?.tipo ?? '',
  })

  // A prévia é um endereço local do arquivo escolhido; liberado quando troca ou o formulário sai.
  const previaAtual = useRef<string | null>(null)
  useEffect(() => () => {
    if (previaAtual.current) URL.revokeObjectURL(previaAtual.current)
  }, [])
  const escolher = (arquivo: File | null) => {
    if (previaAtual.current) URL.revokeObjectURL(previaAtual.current)
    previaAtual.current = arquivo ? URL.createObjectURL(arquivo) : null
    setPrevia(previaAtual.current)
    setFoto(arquivo)
    setErroFoto(undefined)
  }

  const submit = enviar(
    (v) => {
      const erros = validar(v, foto, edicao)
      setErroFoto(erros.foto)
      return erros
    },
    async (v) => {
      try {
        await onSubmit({ ...v, tipo: v.tipo as TipoVantagem, foto })
      } catch (e) {
        // O erro da foto vem da API num campo que não está no useCampos (é um arquivo).
        if (e instanceof Error && 'campos' in e) setErroFoto((e as { campos: Erros }).campos.foto)
        throw e
      }
    },
  )

  const imagem = previa ?? (vantagem ? fotoUrl(vantagem) : null)

  return (
    <form ref={formRef} noValidate onSubmit={submit} className="grid gap-5">
      <TextField label="Nome" maxLength={100} {...campo('nome')} />
      <TextAreaField label="Descrição" maxLength={DESCRICAO_MAX} rows={3} {...campo('descricao')} />
      <div className="grid gap-5 @md:grid-cols-2">
        <TextField label="Custo" inputMode="numeric" autoComplete="off" {...campo('custo')} />
        <SelectField label="Tipo" placeholder="Escolha" {...campo('tipo')}>
          {(Object.keys(nomeDoTipo) as TipoVantagem[]).map((t) => (
            <option key={t} value={t}>
              {nomeDoTipo[t]}
            </option>
          ))}
        </SelectField>
      </div>
      <div>
        <p className="mb-2 font-display text-xs font-semibold tracking-[0.08em] text-ink-soft uppercase" id={`${fotoId}-rotulo`}>
          Foto
        </p>
        <div className="flex items-center gap-4">
          {imagem ? (
            <img src={imagem} alt="Prévia da foto" className="size-20 shrink-0 rounded-[20px] object-cover ring-1 ring-line" />
          ) : (
            <span aria-hidden className="grid size-20 shrink-0 place-items-center rounded-[20px] text-ink-soft ring-1 ring-line">
              <ImageIcon size={28} />
            </span>
          )}
          <label className="inline-flex h-11 cursor-pointer items-center rounded-full px-5 font-display text-sm font-semibold tracking-wide uppercase ring-1 ring-snow/40 transition-colors duration-200 hover:bg-snow hover:text-night has-[:focus-visible]:outline-2 has-[:focus-visible]:outline-offset-4 has-[:focus-visible]:outline-snow">
            {foto || edicao ? 'Trocar foto' : 'Escolher foto'}
            <input
              id={fotoId}
              name="foto"
              type="file"
              accept={FOTO_TIPOS.join(',')}
              aria-labelledby={`${fotoId}-rotulo`}
              aria-invalid={erroFoto ? true : undefined}
              aria-describedby={`${fotoId}-dica`}
              className="sr-only"
              onChange={(e) => escolher(e.target.files?.[0] ?? null)}
            />
          </label>
        </div>
        {erroFoto ? (
          <p id={`${fotoId}-dica`} className="mt-2 flex items-start gap-1.5 text-sm text-danger">
            <WarningCircleIcon size={16} weight="bold" aria-hidden className="mt-0.5 shrink-0" />
            {erroFoto}
          </p>
        ) : (
          <p id={`${fotoId}-dica`} className="mt-2 text-sm text-ink-soft">
            JPEG, PNG ou WebP, até 2 MB.{edicao && ' Sem foto nova, fica a atual.'}
          </p>
        )}
      </div>
      <FormActions alert={alert} sending={sending} label={label} busyLabel={busyLabel} onCancel={onCancel} />
    </form>
  )
}
