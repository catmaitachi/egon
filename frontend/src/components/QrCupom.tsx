import { useMemo } from 'react'
import qrcode from 'qrcode-generator'
import { routes } from '../content'

// O QR leva ao link de confirmação: lido pela câmera da empresa logada, abre o painel dela com o
// código já preenchido. Desenhado aqui a partir da matriz (módulos escuros sobre fundo claro, como a
// câmera espera), sem injetar o SVG da biblioteca como HTML.
export function QrCupom({ codigo, tamanho = 220 }: { codigo: string; tamanho?: number }) {
  const { n, caminho } = useMemo(() => {
    const qr = qrcode(0, 'M')
    qr.addData(`${window.location.origin}${routes.painel}?cupom=${encodeURIComponent(codigo)}`)
    qr.make()
    const n = qr.getModuleCount()
    let d = ''
    for (let y = 0; y < n; y++) for (let x = 0; x < n; x++) if (qr.isDark(y, x)) d += `M${x} ${y}h1v1h-1z`
    return { n, caminho: d }
  }, [codigo])
  return (
    <svg
      role="img"
      aria-label={`QR code do cupom ${codigo}`}
      viewBox={`-4 -4 ${n + 8} ${n + 8}`}
      width={tamanho}
      height={tamanho}
      shapeRendering="crispEdges"
      className="rounded-[20px] bg-snow"
    >
      <path d={caminho} className="fill-night" />
    </svg>
  )
}
