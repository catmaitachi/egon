import { useEffect, useRef, useState } from 'react'
import { animate, useReducedMotion } from 'motion/react'
import PatternWaves from './PatternWaves'
import { useCtaHover } from '../lib/stage'

const SNOW = '#FBF9FA'
const ACCENT = '#6A4CFF'

// Fundo do hero: Pattern Waves discreto, pontos brancos com pouca opacidade.
// Só o hover da ação principal tinge as ondas com o acento.
export function WavesStage() {
  const reduce = useReducedMotion()
  const [color, setColor] = useState(SNOW)
  const current = useRef(SNOW)
  const target = useCtaHover() ? ACCENT : SNOW

  useEffect(() => {
    if (reduce) {
      current.current = target
      setColor(target)
      return
    }
    const controls = animate(current.current, target, {
      duration: 0.9,
      ease: [0.16, 1, 0.3, 1],
      onUpdate: (v) => {
        current.current = v
        setColor(v)
      },
    })
    return () => controls.stop()
  }, [target, reduce])

  return (
    <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
      <PatternWaves
        preset="ocean"
        color={color}
        backgroundColor="#0B0A0B"
        opacity={0.42}
        contrast={1}
        depth={0.35}
        shine={0.6}
        speed={0.32}
        cursorStrength={0.35}
        fade="edges"
        fadeSize={0.6}
        paused={!!reduce}
      />
      {/* Funde as ondas no preto da página no pé do hero */}
      <div className="absolute inset-x-0 bottom-0 h-1/3 bg-[linear-gradient(to_bottom,transparent,var(--night))]" />
    </div>
  )
}
