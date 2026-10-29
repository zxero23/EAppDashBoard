import { useEffect, useState } from 'react'
import { api, query, type Fila, type Indicador, type Item, type Meta, type Obra, type Tablero } from '../api'
import { useSesion } from '../App'
import { Avance, Chip, color, fecha, Grafico, Leyenda, Mapa } from '../componentes'

type Seleccion = { tipo: 'metas' | 'obras'; organismo: Fila | null; situacion: Item | null }

export default function TableroPagina() {
  const { cat } = useSesion()
  const [tablero, setTablero] = useState<Tablero | null>(null)
  const [sel, setSel] = useState<Seleccion | null>(null)
  const [metas, setMetas] = useState<Meta[]>([])
  const [obras, setObras] = useState<Obra[]>([])
  const [meta, setMeta] = useState<Meta | null>(null)
  const [indicadores, setIndicadores] = useState<Indicador[]>([])

  useEffect(() => { api<Tablero>('/tablero').then(setTablero) }, [])

  useEffect(() => {
    setMeta(null)
    if (!sel) return
    const q = query({ organismoId: sel.organismo?.id, situacionId: sel.situacion?.id })
    if (sel.tipo === 'metas') api<Meta[]>('/metas' + q).then(setMetas)
    else api<Obra[]>('/obras' + q).then(setObras)
  }, [sel])

  useEffect(() => {
    setIndicadores([])
    if (meta) api<Indicador[]>(`/metas/${meta.id}/indicadores`).then(setIndicadores)
  }, [meta])

  if (!tablero) return <p className="cargando">Cargando tablero…</p>

  const tabla = (titulo: string, tipo: Seleccion['tipo'], filas: Fila[], unidad: string) => (
    <section className="tarjeta">
      <h2>{titulo}</h2>
      <table className="semaforo">
        <thead><tr><th>Organismo</th>{cat.semaforo.map(s => <th key={s.id} title={s.leyenda}><i className={'punto ' + color(cat, s.id)} /></th>)}<th>Total</th></tr></thead>
        <tbody>
          {filas.map(f => (
            <tr key={f.id} className={sel?.organismo?.id === f.id ? 'activa' : ''}>
              <td>{f.leyenda}</td>
              {cat.semaforo.map(s => {
                const n = f.conteos[s.id] ?? 0
                return (
                  <td key={s.id}>
                    <button className={'conteo ' + color(cat, s.id)} disabled={n === 0}
                            title={`${n} ${unidad} ${s.leyenda.toLowerCase()}`}
                            onClick={() => setSel({ tipo, organismo: f, situacion: s })}>{n}</button>
                  </td>
                )
              })}
              <td><button className="enlace" disabled={f.total === 0} onClick={() => setSel({ tipo, organismo: f, situacion: null })}>{f.total}</button></td>
            </tr>
          ))}
          {filas.length === 0 && <tr><td colSpan={6} className="tenue">Sin organismos</td></tr>}
        </tbody>
      </table>
    </section>
  )

  const titulo = sel && [sel.tipo === 'metas' ? 'Metas' : 'Obras', sel.situacion?.leyenda.toLowerCase(), sel.organismo && '· ' + sel.organismo.leyenda].filter(Boolean).join(' ')

  return (
    <div className="tablero">
      <div className="columna">
        <Leyenda cat={cat} />
        {tabla('Metas por ministerio', 'metas', tablero.ministerios, 'metas')}
        {tabla('Obras por ente ejecutor', 'obras', tablero.entes, 'obras')}
        <button className="ancho" onClick={() => setSel({ tipo: 'obras', organismo: null, situacion: null })}>Ver todas las obras en el mapa</button>
      </div>

      <div className="detalle">
        {!sel && <section className="tarjeta vacia">Elegí un número del semáforo para ver sus metas u obras.</section>}

        {sel?.tipo === 'metas' && (
          <section className="tarjeta">
            <h2>{titulo}</h2>
            <table>
              <thead><tr><th>Objetivo</th><th>Meta</th><th>Situación</th><th>Plazo</th><th>Avance</th><th /></tr></thead>
              <tbody>
                {metas.map(m => (
                  <tr key={m.id} className={meta?.id === m.id ? 'activa' : ''}>
                    <td>{m.objetivo}</td>
                    <td>{m.leyenda}</td>
                    <td><Chip cat={cat} id={m.situacionId} texto={m.situacion} /></td>
                    <td className="nowrap">{fecha(m.fechaInicio)} – {fecha(m.fechaFinal)}</td>
                    <td><Avance valor={m.avance} /></td>
                    <td><button className="enlace" disabled={m.indicadores === 0} onClick={() => setMeta(m)}>Indicadores ({m.indicadores})</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
            {meta && indicadores.map(i => <Grafico key={i.id} indicador={i} meta={meta} />)}
          </section>
        )}

        {sel?.tipo === 'obras' && (
          <section className="tarjeta">
            <h2>{titulo} <small className="tenue">({obras.length})</small></h2>
            <Mapa cat={cat} obras={obras} />
            <table>
              <thead><tr><th>Obra</th><th>Ente</th><th>Localidad</th><th>Situación</th><th>Plazo</th></tr></thead>
              <tbody>
                {obras.map(o => (
                  <tr key={o.id}>
                    <td>{o.leyenda}</td><td>{o.organismo}</td><td>{o.localidad}</td>
                    <td><Chip cat={cat} id={o.situacionId} texto={o.situacion} /></td>
                    <td className="nowrap">{fecha(o.fechaInicio)} – {fecha(o.fechaFinal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        )}
      </div>
    </div>
  )
}
