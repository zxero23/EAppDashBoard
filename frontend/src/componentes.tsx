import { CircleMarker, MapContainer, Popup, TileLayer, useMapEvents } from 'react-leaflet'
import { CartesianGrid, Legend, Line, LineChart, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { Catalogos, Indicador, Meta, Obra } from './api'

const COLORES = ['verde', 'amarillo', 'rojo', 'azul'] as const
const HEX: Record<string, string> = { verde: '#3f9b4f', amarillo: '#e0a800', rojo: '#d9534f', azul: '#2b7bb9', gris: '#8a94a0' }

/** Color del semaforo segun la posicion de la situacion en catalogos.semaforo (verde, amarillo, rojo, azul). */
export function color(cat: Catalogos, situacionId: number | null | undefined) {
  const i = cat.semaforo.findIndex(s => s.id === situacionId)
  return i >= 0 ? COLORES[i] : 'gris'
}

export function Leyenda({ cat }: { cat: Catalogos }) {
  return (
    <div className="leyenda">
      {cat.semaforo.map(s => (
        <span key={s.id}><i className={'punto ' + color(cat, s.id)} />{s.leyenda}</span>
      ))}
    </div>
  )
}

export function Chip({ cat, id, texto }: { cat: Catalogos; id: number | null; texto: string | null }) {
  return <span className={'chip ' + color(cat, id)}>{texto ?? '—'}</span>
}

export function Avance({ valor }: { valor: number | null }) {
  if (valor === null) return <span className="tenue">sin datos</span>
  return (
    <span className="avance" title={valor + '%'}>
      <span className="barra"><span style={{ width: Math.min(valor, 100) + '%' }} /></span>
      {valor.toLocaleString('es-AR')}%
    </span>
  )
}

export function Grafico({ indicador, meta }: { indicador: Indicador; meta: Meta }) {
  return (
    <figure className="grafico">
      <figcaption>
        <strong>{indicador.leyenda}</strong>
        <span>{meta.reduccion ? 'Reducir a' : 'Alcanzar'} {meta.cantidad.toLocaleString('es-AR')} · {indicador.descripcion}</span>
      </figcaption>
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={indicador.puntos} margin={{ top: 10, right: 30, bottom: 0, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e3e7ec" />
          <XAxis dataKey="periodo" fontSize={12} />
          <YAxis fontSize={12} />
          <Tooltip />
          <Legend />
          <ReferenceLine y={meta.cantidad} stroke={HEX.verde} strokeDasharray="6 4" label={{ value: 'Meta', fill: HEX.verde, fontSize: 12 }} />
          <Line type="monotone" dataKey="valor" name={indicador.variable} stroke={HEX.azul} strokeWidth={2} dot={{ r: 3 }} />
        </LineChart>
      </ResponsiveContainer>
    </figure>
  )
}

function ClickEnMapa({ onClick }: { onClick: (lat: number, lng: number) => void }) {
  useMapEvents({ click: e => onClick(e.latlng.lat, e.latlng.lng) })
  return null
}

/** Mapa de Misiones con las obras coloreadas por situacion. onClick permite elegir coordenadas. */
export function Mapa({ cat, obras, onClick, alto = 420 }: {
  cat: Catalogos; obras: Obra[]; onClick?: (lat: number, lng: number) => void; alto?: number
}) {
  return (
    <MapContainer center={[-26.9, -54.65]} zoom={7} style={{ height: alto }} className="mapa">
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                 attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' />
      {obras.filter(o => o.latitud !== null && o.longitud !== null).map(o => (
        <CircleMarker key={o.id} center={[o.latitud!, o.longitud!]} radius={7}
                      pathOptions={{ color: '#fff', weight: 1.5, fillColor: HEX[color(cat, o.situacionId)], fillOpacity: 0.9 }}>
          <Popup>
            <strong>{o.leyenda}</strong><br />
            {o.organismo} · {o.localidad}<br />
            {o.situacion}<br />
            {fecha(o.fechaInicio)} – {fecha(o.fechaFinal)}
          </Popup>
        </CircleMarker>
      ))}
      {onClick && <ClickEnMapa onClick={onClick} />}
    </MapContainer>
  )
}

export function fecha(iso: string | null) {
  return iso ? new Date(iso + 'T00:00:00').toLocaleDateString('es-AR') : '—'
}

export function Opciones({ items, vacio }: { items: { id: number; leyenda: string }[]; vacio?: string }) {
  return <>
    {vacio !== undefined && <option value="">{vacio}</option>}
    {items.map(i => <option key={i.id} value={i.id}>{i.leyenda}</option>)}
  </>
}

/** Ejecuta una accion de guardado/borrado mostrando el error del backend si falla. */
export async function intentar(accion: () => Promise<unknown>, setError: (e: string | null) => void) {
  setError(null)
  try {
    await accion()
    return true
  } catch (e) {
    setError((e as Error).message)
    return false
  }
}

export function Aviso({ error }: { error: string | null }) {
  return error ? <p className="aviso">{error}</p> : null
}
