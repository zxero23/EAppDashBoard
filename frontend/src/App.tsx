import { createContext, useContext, useEffect, useState } from 'react'
import { BrowserRouter, Navigate, NavLink, Route, Routes } from 'react-router'
import { api, type Catalogos, type Yo } from './api'
import Login from './paginas/Login'
import TableroPagina from './paginas/Tablero'
import Objetivos from './paginas/Objetivos'
import Metas from './paginas/Metas'
import Obras from './paginas/Obras'

type Sesion = { yo: Yo; cat: Catalogos; puede: (permiso: string) => boolean }

const SesionCtx = createContext<Sesion>(null!)
export const useSesion = () => useContext(SesionCtx)

export default function App() {
  const [yo, setYo] = useState<Yo | null | undefined>(undefined)
  const [cat, setCat] = useState<Catalogos | null>(null)

  useEffect(() => {
    api('/auth/csrf').then(() => api<Yo>('/auth/yo')).then(setYo, () => setYo(null))
    const vencida = () => setYo(null)
    window.addEventListener('sesion-vencida', vencida)
    return () => window.removeEventListener('sesion-vencida', vencida)
  }, [])

  useEffect(() => {
    setCat(null)
    if (yo) api<Catalogos>('/catalogos').then(setCat)
  }, [yo])

  if (yo === undefined) return <p className="cargando">Cargando…</p>
  if (!yo) return <Login onIngreso={setYo} />
  if (!cat) return <p className="cargando">Cargando…</p>

  const salir = async () => {
    await api('/auth/logout', 'POST')
    await api('/auth/csrf')
    setYo(null)
  }

  return (
    <SesionCtx.Provider value={{ yo, cat, puede: p => yo.permisos.includes(p) }}>
      <BrowserRouter>
        <header className="encabezado">
          <div className="marca">
            <strong>Tablero de Gestión</strong>
            <span>Provincia de Misiones</span>
          </div>
          <nav>
            <div className="segmentos">
              <NavLink to="/tablero">Tablero</NavLink>
              <NavLink to="/objetivos">Objetivos</NavLink>
              <NavLink to="/metas">Metas</NavLink>
              <NavLink to="/obras">Obras</NavLink>
            </div>
          </nav>
          <div className="usuario">
            <span>{yo.nombre || yo.usuario}<small>{yo.global ? 'Vista provincial' : yo.organismo}</small></span>
            <button className="secundario" onClick={salir}>Salir</button>
          </div>
        </header>
        <main>
          <Routes>
            <Route path="/tablero" element={<TableroPagina />} />
            <Route path="/objetivos" element={<Objetivos />} />
            <Route path="/metas" element={<Metas />} />
            <Route path="/obras" element={<Obras />} />
            <Route path="*" element={<Navigate to="/tablero" replace />} />
          </Routes>
        </main>
      </BrowserRouter>
    </SesionCtx.Provider>
  )
}
