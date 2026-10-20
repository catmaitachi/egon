import { useEffect } from 'react'
import { brand } from '../content'

export function useTitle(title: string) {
  useEffect(() => {
    document.title = `${title} · ${brand.name}`
    return () => {
      document.title = brand.name
    }
  }, [title])
}
