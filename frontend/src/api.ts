export type Item = { id: number; leyenda: string }
export type OrganismoItem = Item & { descentralizado: boolean }
export type Yo = {
  id: number; usuario: string; nombre: string; organismoId: number | null; organismo: string | null
  global: boolean; permisos: string[]
}
export type Catalogos = { semaforo: Item[]; situaciones: Item[]; localidades: Item[]; organismos: OrganismoItem[] }
export type Fila = { id: number; leyenda: string; conteos: Record<string, number>; total: number }
export type Tablero = { ministerios: Fila[]; entes: Fila[] }
export type Objetivo = {
  id: number; leyenda: string; descripcion: string | null; organismoId: number; organismo: string
  situacionId: number | null; situacion: string | null
}
export type Meta = {
  id: number; leyenda: string; descripcion: string | null; cantidad: number; reduccion: boolean
  fechaInicio: string | null; fechaFinal: string | null; objetivoId: number; objetivo: string
  organismoId: number; organismo: string; situacionId: number | null; situacion: string | null
  avance: number | null; indicadores: number
}
export type Punto = { periodo: string; valor: number | null }
export type Indicador = { id: number; leyenda: string; descripcion: string | null; variable: string; puntos: Punto[] }
export type Actividad = { id: number; leyenda: string; cantidad: number }
export type Obra = {
  id: number; leyenda: string; latitud: number | null; longitud: number | null
  fechaInicio: string | null; fechaFinal: string | null; organismoId: number; organismo: string
  situacionId: number | null; situacion: string | null; localidadId: number | null; localidad: string | null
}

function cookie(nombre: string) {
  return document.cookie.split('; ').find(c => c.startsWith(nombre + '='))?.split('=')[1] ?? ''
}

/** fetch a /api con cookie de sesion y token CSRF. Un 401 avisa a la app para volver al login. */
export async function api<T>(url: string, metodo = 'GET', cuerpo?: unknown): Promise<T> {
  const res = await fetch('/api' + url, {
    method: metodo,
    credentials: 'same-origin',
    headers: {
      ...(cuerpo !== undefined && { 'Content-Type': 'application/json' }),
      ...(metodo !== 'GET' && { 'X-XSRF-TOKEN': decodeURIComponent(cookie('XSRF-TOKEN')) }),
    },
    body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
  })
  if (res.status === 401 && url !== '/auth/login') window.dispatchEvent(new Event('sesion-vencida'))
  if (!res.ok) {
    const error = await res.json().catch(() => null)
    throw new Error(error?.detail || error?.title || `Error ${res.status}`)
  }
  const texto = await res.text()
  return (texto ? JSON.parse(texto) : undefined) as T
}

/** Arma ?a=1&b=2 omitiendo vacios. */
export function query(params: Record<string, string | number | null | undefined>) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => v !== null && v !== undefined && v !== '' && q.set(k, String(v)))
  const s = q.toString()
  return s ? '?' + s : ''
}
