import { CircleMarker, MapContainer, Popup, TileLayer, useMapEvents } from 'react-leaflet'
import { useState, type ReactNode } from 'react'
import { Bar, CartesianGrid, Cell, ComposedChart, LabelList, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { Catalogos, Indicador, Meta, Obra } from './api'

const COLORES = ['verde', 'amarillo', 'rojo', 'azul'] as const
// Colores del sistema de Apple; se ven bien en modo claro y oscuro (SVG no toma variables CSS en atributos).
const HEX: Record<string, string> = { verde: '#34c759', amarillo: '#ff9f0a', rojo: '#ff3b30', azul: '#0a84ff', gris: '#8e8e93' }

/** Color del semaforo segun la posicion de la situacion en catalogos.semaforo (verde, amarillo, rojo, azul). */
export function color(cat: Catalogos, situacionId: number | null | undefined) {
  const i = cat.semaforo.findIndex(s => s.id === situacionId)
  return i >= 0 ? COLORES[i] : 'gris'
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

const num = (v: number) => v.toLocaleString('es-AR', { maximumFractionDigits: 2 })
const compacto = new Intl.NumberFormat('es-AR', { notation: 'compact', maximumFractionDigits: 1 })

/**
 * Indicadores de una meta: pestañas si hay varios, resumen (ultimo valor, avance, variacion, cuanto falta)
 * y barras por periodo contra la linea de meta. Verde = ese periodo ya cumple la meta.
 */
export function PanelIndicadores({ meta, indicadores, acciones }: {
  meta: Meta; indicadores: Indicador[]; acciones?: (i: Indicador) => ReactNode
}) {
  const [elegido, setElegido] = useState(0)
  const [verTabla, setVerTabla] = useState(false)
  const ind = indicadores[Math.min(elegido, indicadores.length - 1)]
  if (!ind) return <p className="tenue">La meta no tiene indicadores: sin indicador no se puede calcular el avance.</p>

  const objetivo = ind.valorMeta
  const cumple = (v: number) => objetivo !== null && (meta.reduccion ? v <= objetivo : v >= objetivo)
  const conValor = ind.puntos.filter(p => p.valor !== null) as { periodo: string; valor: number }[]
  const base = conValor[0]
  const ultimo = conValor.at(-1)
  const anterior = conValor.at(-2)
  const variacion = ultimo && anterior && anterior.valor !== 0 ? (ultimo.valor - anterior.valor) / Math.abs(anterior.valor) * 100 : null
  const mejora = variacion !== null && (meta.reduccion ? variacion < 0 : variacion > 0)
  const falta = ultimo && objetivo !== null ? (meta.reduccion ? ultimo.valor - objetivo : objetivo - ultimo.valor) : null

  return (
    <div className="indicadores">
      {indicadores.length > 1 && (
        <div className="pestanas" role="tablist">
          {indicadores.map((i, n) => (
            <button key={i.id} role="tab" aria-selected={i === ind} className={i === ind ? 'activa' : ''} onClick={() => setElegido(n)}>{i.leyenda}</button>
          ))}
        </div>
      )}

      <div className="kpis">
        <div><span>Último valor</span><strong>{ultimo ? num(ultimo.valor) : '—'}</strong><small>{ultimo ? `${ind.variable} · ${ultimo.periodo}` : 'sin datos cargados'}</small></div>
        <div>
          <span>Meta · {meta.reduccion ? 'reducir' : 'aumentar'} {num(meta.cantidad)}%</span>
          <strong>{objetivo === null ? '—' : num(objetivo)}</strong>
          <small>{base ? `desde ${num(base.valor)} (${base.periodo}) · al ${fecha(meta.fechaFinal)}` : 'sin línea de base'}</small>
        </div>
        <div>
          <span>Avance</span><strong>{ind.avance === null ? '—' : num(ind.avance) + '%'}</strong>
          <span className="barra ancha"><span className={ind.avance !== null && ind.avance >= 100 ? 'ok' : ''} style={{ width: Math.min(ind.avance ?? 0, 100) + '%' }} /></span>
        </div>
        <div>
          <span>Vs. período anterior</span>
          <strong className={variacion === null ? '' : mejora ? 'sube' : 'baja'}>
            {variacion === null ? '—' : `${variacion > 0 ? '▲' : variacion < 0 ? '▼' : '='} ${num(Math.abs(variacion))}%`}
          </strong>
          <small>{anterior ? `desde ${anterior.periodo}` : 'un solo período'}</small>
        </div>
        <div><span>{falta !== null && falta <= 0 ? 'Estado' : 'Falta'}</span>
          <strong className={falta !== null && falta <= 0 ? 'sube' : ''}>{falta === null ? '—' : falta <= 0 ? 'Meta cumplida' : num(falta)}</strong>
          <small>{falta !== null && falta > 0 ? (meta.reduccion ? 'para bajar a la meta' : 'para llegar a la meta') : ''}</small>
        </div>
      </div>

      <figure className="grafico">
        <figcaption><strong>{ind.leyenda}</strong>{ind.descripcion && ind.descripcion !== ind.leyenda && <span>{ind.descripcion}</span>}</figcaption>
        <ResponsiveContainer width="100%" height={280}>
          <ComposedChart data={ind.puntos} margin={{ top: 22, right: 16, bottom: 0, left: 0 }}>
            <CartesianGrid stroke="rgba(142,142,147,.18)" vertical={false} />
            <XAxis dataKey="periodo" fontSize={12} tickLine={false} axisLine={false} tick={{ fill: HEX.gris }} dy={6} />
            <YAxis fontSize={12} tickFormatter={v => compacto.format(v)} width={52} tickLine={false} axisLine={false} tick={{ fill: HEX.gris }} />
            <Tooltip formatter={v => [num(Number(v)), ind.variable]} cursor={{ fill: 'rgba(142,142,147,.12)', radius: 8 }} />
            <Bar dataKey="valor" maxBarSize={48} radius={[8, 8, 8, 8]} isAnimationActive={false}>
              {ind.puntos.map((p, n) => <Cell key={n} fill={p.valor !== null && cumple(p.valor) ? HEX.verde : HEX.azul} />)}
              <LabelList dataKey="valor" position="top" fontSize={11} fontWeight={600} fill={HEX.gris}
                         formatter={(v: unknown) => v === null || v === undefined ? '' : compacto.format(Number(v))} />
            </Bar>
            {/* Después de las barras para que la línea de meta quede por encima. */}
            {objetivo !== null && (
              <ReferenceLine y={objetivo} stroke={HEX.verde} strokeWidth={2} strokeDasharray="6 4" ifOverflow="extendDomain" />
            )}
          </ComposedChart>
        </ResponsiveContainer>
        <div className="pie-grafico">
          {objetivo !== null && <span><i className="linea-meta" />meta {num(objetivo)}</span>}
          <span><i className="punto verde" />cumple la meta</span>
          <span><i className="punto azul" />todavía no</span>
          <span className="flex" />
          <button className="enlace" onClick={() => setVerTabla(!verTabla)}>{verTabla ? 'Ocultar datos' : 'Ver datos'}</button>
          {acciones?.(ind)}
        </div>
      </figure>

      {verTabla && (
        <table className="datos">
          <thead><tr><th>Período</th><th>{ind.variable}</th><th>Var. vs. {base?.periodo ?? 'inicio'}</th></tr></thead>
          <tbody>
            {ind.puntos.map((p, n) => (
              <tr key={n}>
                <td>{p.periodo}</td>
                <td>{p.valor === null ? '—' : num(p.valor)}</td>
                <td>{p.valor === null || !base || base.valor === 0 ? '—' : num((p.valor - base.valor) / Math.abs(base.valor) * 100) + '%'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
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
    <MapContainer center={[-26.9, -54.65]} zoom={7} style={{ height: alto }} className="mapa" scrollWheelZoom={false}>
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
