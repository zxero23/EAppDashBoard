import { useEffect, useRef, useState } from 'react'
import { api, query, type Fila, type Indicador, type Item, type Meta, type Obra, type Tablero } from '../api'
import { useSesion } from '../App'
import { Avance, Chip, color, fecha, Mapa, PanelIndicadores } from '../componentes'

type Seleccion = { tipo: 'metas' | 'obras'; organismo: Fila | null; situacion: Item | null }

export default function TableroPagina() {
  const { cat } = useSesion()
  const [tablero, setTablero] = useState<Tablero | null>(null)
  const [sel, setSel] = useState<Seleccion | null>(null)
  const [metas, setMetas] = useState<Meta[]>([])
  const [obras, setObras] = useState<Obra[]>([])
  const [meta, setMeta] = useState<Meta | null>(null)
  const [indicadores, setIndicadores] = useState<Indicador[]>([])
  const panel = useRef<HTMLElement>(null)

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
    panel.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
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

  const totalMetas = (id: number) => tablero.ministerios.reduce((s, f) => s + (f.conteos[id] ?? 0), 0)
  const totalObras = (id: number) => tablero.entes.reduce((s, f) => s + (f.conteos[id] ?? 0), 0)

  return (
    <>
    <h1 className="titulo-pagina">Tablero</h1>
    <p className="subtitulo">Estado de las metas de cada ministerio y de las obras en ejecución.</p>

    {/* Resumen provincial: también funciona como leyenda del semáforo. Click = todas las metas en esa situación. */}
    <div className="resumen">
      {cat.semaforo.map(s => (
        <div key={s.id} className="clic" role="button" tabIndex={0}
             onClick={() => setSel({ tipo: 'metas', organismo: null, situacion: s })}
             onKeyDown={e => e.key === 'Enter' && setSel({ tipo: 'metas', organismo: null, situacion: s })}>
          <span><i className={'punto ' + color(cat, s.id)} />{s.leyenda.charAt(0) + s.leyenda.slice(1).toLowerCase()}</span>
          <strong>{totalMetas(s.id)}</strong>
          <small>metas · {totalObras(s.id)} obras</small>
        </div>
      ))}
    </div>

    <div className="tablero">
      <div className="columna">
        {tabla('Metas por ministerio', 'metas', tablero.ministerios, 'metas')}
        {tabla('Obras por ente ejecutor', 'obras', tablero.entes, 'obras')}
        <button className="ancho" onClick={() => setSel({ tipo: 'obras', organismo: null, situacion: null })}>Ver todas las obras en el mapa</button>
      </div>

      <div className="detalle">
        {!sel && (
          <section className="tarjeta vacia">
            <strong>Elegí qué querés ver</strong>
            Tocá un número del semáforo o una tarjeta del resumen para ver sus metas u obras.
          </section>
        )}

        {sel?.tipo === 'metas' && meta && (
          <section className="tarjeta" ref={panel}>
            <div className="barra-titulo">
              <h2>{meta.leyenda}<small className="tenue bloque">{meta.organismo} · {meta.objetivo}</small></h2>
              <Chip cat={cat} id={meta.situacionId} texto={meta.situacion} />
              <button className="secundario circular" onClick={() => setMeta(null)} aria-label="Cerrar indicadores">✕</button>
            </div>
            <PanelIndicadores key={meta.id} meta={meta} indicadores={indicadores} />
          </section>
        )}

        {sel?.tipo === 'metas' && (
          <section className="tarjeta">
            <h2>{titulo} <small className="tenue">({metas.length}) · hacé click en una meta para ver su indicador</small></h2>
            <table>
              <thead><tr><th>Objetivo</th><th>Meta</th><th>Situación</th><th>Plazo</th><th>Avance</th></tr></thead>
              <tbody>
                {metas.map(m => (
                  <tr key={m.id} className={'clic ' + (meta?.id === m.id ? 'activa' : '')} onClick={() => setMeta(m)}>
                    <td>{m.objetivo}</td>
                    <td>{m.leyenda}{m.indicadores === 0 && <small className="tenue bloque">sin indicador</small>}</td>
                    <td><Chip cat={cat} id={m.situacionId} texto={m.situacion} /></td>
                    <td className="nowrap">{fecha(m.fechaInicio)} – {fecha(m.fechaFinal)}</td>
                    <td><Avance valor={m.avance} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
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
    </>
  )
}
