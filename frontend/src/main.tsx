import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Route, Routes } from 'react-router'
import { MotionConfig } from 'motion/react'
import '@fontsource/chakra-petch/300.css'
import '@fontsource/chakra-petch/400.css'
import '@fontsource/chakra-petch/500.css'
import '@fontsource/tomorrow/600.css'
import '@fontsource/tomorrow/700.css'
import '@fontsource/tomorrow/800.css'
import './index.css'
import App from './App.tsx'
import { routes } from './content'
import { CadastroAluno } from './pages/CadastroAluno'
import { CadastroEmpresa } from './pages/CadastroEmpresa'
import { Alunos } from './pages/Alunos'
import { Empresas } from './pages/Empresas'
import { NaoEncontrada } from './pages/NaoEncontrada'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MotionConfig reducedMotion="user">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<App />} />
          <Route path={routes.signup} element={<CadastroAluno />} />
          <Route path={routes.partner} element={<CadastroEmpresa />} />
          <Route path={routes.students} element={<Alunos />} />
          <Route path={routes.partners} element={<Empresas />} />
          <Route path="*" element={<NaoEncontrada />} />
        </Routes>
      </BrowserRouter>
    </MotionConfig>
  </StrictMode>,
)
