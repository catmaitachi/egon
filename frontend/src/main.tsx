import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@fontsource/chakra-petch/300.css'
import '@fontsource/chakra-petch/400.css'
import '@fontsource/chakra-petch/500.css'
import '@fontsource/tomorrow/600.css'
import '@fontsource/tomorrow/700.css'
import '@fontsource/tomorrow/800.css'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
