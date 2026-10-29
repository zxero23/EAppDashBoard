import { useState, type FormEvent } from 'react'
import { api, type Yo } from '../api'
import { Aviso } from '../componentes'

export default function Login({ onIngreso }: { onIngreso: (yo: Yo) => void }) {
  const [usuario, setUsuario] = useState('')
  const [clave, setClave] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function ingresar(e: FormEvent) {
    e.preventDefault()
    setEnviando(true)
    setError(null)
    try {
      onIngreso(await api<Yo>('/auth/login', 'POST', { usuario, clave }))
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="login">
      <form onSubmit={ingresar}>
        <h1>Tablero de Gestión</h1>
        <p className="tenue">Provincia de Misiones</p>
        <label>Usuario<input value={usuario} onChange={e => setUsuario(e.target.value)} autoComplete="username" required autoFocus /></label>
        <label>Clave<input type="password" value={clave} onChange={e => setClave(e.target.value)} autoComplete="current-password" required /></label>
        <Aviso error={error} />
        <button disabled={enviando}>{enviando ? 'Ingresando…' : 'Ingresar'}</button>
      </form>
    </div>
  )
}
