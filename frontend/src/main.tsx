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
import { RolagemNoTopo } from './components/RolagemNoTopo'
import { CadastroAluno } from './pages/CadastroAluno'
import { CadastroEmpresa } from './pages/CadastroEmpresa'
import { Alunos } from './pages/Alunos'
import { Empresas } from './pages/Empresas'
import { Entrar } from './pages/Entrar'
import { NaoEncontrada } from './pages/NaoEncontrada'
import { PainelAluno } from './pages/PainelAluno'
import { PainelEmpresa } from './pages/PainelEmpresa'
import { PainelProfessor } from './pages/PainelProfessor'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MotionConfig reducedMotion="user">
      <BrowserRouter>
        <RolagemNoTopo />
        <Routes>
          <Route path="/" element={<App />} />
          <Route path={routes.signup} element={<CadastroAluno />} />
          <Route path={routes.partner} element={<CadastroEmpresa />} />
          <Route path={routes.students} element={<Alunos />} />
          <Route path={routes.partners} element={<Empresas />} />
          <Route path={routes.login} element={<Entrar />} />
          <Route path={routes.painelAluno} element={<PainelAluno />} />
          <Route path={routes.painelProfessor} element={<PainelProfessor />} />
          <Route path={routes.painelEmpresa} element={<PainelEmpresa />} />
          <Route path="*" element={<NaoEncontrada />} />
        </Routes>
      </BrowserRouter>
    </MotionConfig>
  </StrictMode>,
)
