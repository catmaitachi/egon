import { useEffect } from 'react'
import { useLocation } from 'react-router'

// O roteador não mexe na rolagem: sem isto, uma tela nova abriria na altura em que a anterior
// estava. Só a troca de caminho conta; os atalhos da landing (#como-funciona) seguem rolando suave.
export function RolagemNoTopo() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' })
  }, [pathname])
  return null
}
