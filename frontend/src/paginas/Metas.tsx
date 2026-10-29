import { useEffect, useState, type FormEvent } from 'react'
import { api, query, type Actividad, type Indicador, type Meta, type Objetivo } from '../api'
import { useSesion } from '../App'
import { Avance, Aviso, Chip, fecha, intentar, Opciones, PanelIndicadores } from '../componentes'

type Form = {
  id?: number; leyenda: string; descripcion: string; cantidad: string; reduccion: boolean
  fechaInicio: string; fechaFinal: string; objetivoId: string; situacionId: string
}
type FormIndicador = { id?: number; leyenda: string; descripcion: string; variable: string; puntos: { periodo: string; valor: string }[] }

export default function Metas() {
  const { cat, yo, puede } = useSesion()
  const [filtro, setFiltro] = useState({ organismoId: '', objetivoId: '', situacionId: '' })
  const [objetivos, setObjetivos] = useState<Objetivo[]>([])
  const [lista, setLista] = useState<Meta[]>([])
  const [form, setForm] = useState<Form | null>(null)
  const [meta, setMeta] = useState<Meta | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => { api<Objetivo[]>('/objetivos' + query({ organismoId: filtro.organismoId })).then(setObjetivos) }, [filtro.organismoId])
  const cargar = () => api<Meta[]>('/metas' + query(filtro)).then(l => { setLista(l); setMeta(m => m && (l.find(x => x.id === m.id) ?? null)) })
  useEffect(() => { cargar() }, [filtro])

  const nuevo = () => setForm({ leyenda: '', descripcion: '', cantidad: '', reduccion: false, fechaInicio: '', fechaFinal: '',
    objetivoId: filtro.objetivoId, situacionId: String(cat.semaforo[0]?.id ?? '') })
  const editar = (m: Meta) => setForm({ id: m.id, leyenda: m.leyenda, descripcion: m.descripcion ?? '', cantidad: String(m.cantidad),
    reduccion: m.reduccion, fechaInicio: m.fechaInicio ?? '', fechaFinal: m.fechaFinal ?? '', objetivoId: String(m.objetivoId), situacionId: String(m.situacionId ?? '') })

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const f = form!
    const datos = { ...f, id: undefined, cantidad: Number(f.cantidad), objetivoId: Number(f.objetivoId), situacionId: Number(f.situacionId) }
    if (await intentar(() => f.id ? api(`/metas/${f.id}`, 'PUT', datos) : api('/metas', 'POST', datos), setError)) {
      setForm(null)
      cargar()
    }
  }

  async function borrar(m: Meta) {
    if (confirm(`¿Dar de baja la meta "${m.leyenda}"?`) && await intentar(() => api(`/metas/${m.id}`, 'DELETE'), setError)) cargar()
  }

  const set = (cambio: Partial<typeof filtro>) => setFiltro({ ...filtro, ...cambio })

  return (
    <>
      <section className="tarjeta">
        <div className="barra-titulo">
          <h2>Metas</h2>
          {yo.global && <select value={filtro.organismoId} onChange={e => set({ organismoId: e.target.value, objetivoId: '' })}><Opciones items={cat.organismos} vacio="Todos los organismos" /></select>}
          <select value={filtro.objetivoId} onChange={e => set({ objetivoId: e.target.value })}><Opciones items={objetivos} vacio="Todos los objetivos" /></select>
          <select value={filtro.situacionId} onChange={e => set({ situacionId: e.target.value })}><Opciones items={cat.situaciones} vacio="Todas las situaciones" /></select>
          {puede('nuevaMeta') && <button onClick={nuevo}>Nueva meta</button>}
        </div>
        <Aviso error={error} />

        {form && (
          <form className="formulario" onSubmit={guardar}>
            <h3>{form.id ? 'Editar meta' : 'Nueva meta'}</h3>
            <label className="ancho">Objetivo<select value={form.objetivoId} onChange={e => setForm({ ...form, objetivoId: e.target.value })} required>
              <option value="" />{objetivos.map(o => <option key={o.id} value={o.id}>{o.organismo} · {o.leyenda}</option>)}</select></label>
            <label className="ancho">Meta<input value={form.leyenda} onChange={e => setForm({ ...form, leyenda: e.target.value })} required /></label>
            <label>Sentido<select value={form.reduccion ? '1' : '0'} onChange={e => setForm({ ...form, reduccion: e.target.value === '1' })}>
              <option value="0">Aumentar</option><option value="1">Reducir</option></select></label>
            <label>Variación buscada (%)<input type="number" min={0} value={form.cantidad} onChange={e => setForm({ ...form, cantidad: e.target.value })} required
                                               title="Respecto del primer período del indicador" /></label>
            <label>Inicio<input type="date" value={form.fechaInicio} onChange={e => setForm({ ...form, fechaInicio: e.target.value })} required /></label>
            <label>Fin<input type="date" value={form.fechaFinal} onChange={e => setForm({ ...form, fechaFinal: e.target.value })} required /></label>
            <label>Situación<select value={form.situacionId} onChange={e => setForm({ ...form, situacionId: e.target.value })} required><Opciones items={cat.situaciones} vacio="" /></select></label>
            <label className="ancho">Descripción<textarea value={form.descripcion} onChange={e => setForm({ ...form, descripcion: e.target.value })} rows={2} /></label>
            <div className="acciones"><button>Guardar</button><button type="button" className="secundario" onClick={() => setForm(null)}>Cancelar</button></div>
          </form>
        )}

        <table>
          <thead><tr><th>Organismo</th><th>Objetivo</th><th>Meta</th><th>Situación</th><th>Plazo</th><th>Avance</th><th /></tr></thead>
          <tbody>
            {lista.map(m => (
              <tr key={m.id} className={meta?.id === m.id ? 'activa' : ''}>
                <td>{m.organismo}</td><td>{m.objetivo}</td>
                <td>{m.leyenda}<small className="tenue bloque">{m.reduccion ? 'Reducir' : 'Aumentar'} {m.cantidad.toLocaleString('es-AR')}% respecto del primer período</small></td>
                <td><Chip cat={cat} id={m.situacionId} texto={m.situacion} /></td>
                <td className="nowrap">{fecha(m.fechaInicio)} – {fecha(m.fechaFinal)}</td>
                <td><Avance valor={m.avance} /></td>
                <td className="nowrap">
                  <button className="enlace" onClick={() => setMeta(m)}>Detalle</button>
                  {puede('editarMeta') && <button className="enlace" onClick={() => editar(m)}>Editar</button>}
                  {puede('borrarMeta') && <button className="enlace peligro" onClick={() => borrar(m)}>Borrar</button>}
                </td>
              </tr>
            ))}
            {lista.length === 0 && <tr><td colSpan={7} className="tenue">Sin metas</td></tr>}
          </tbody>
        </table>
      </section>
      {meta && <DetalleMeta key={meta.id} meta={meta} alCambiar={cargar} />}
    </>
  )
}

function DetalleMeta({ meta, alCambiar }: { meta: Meta; alCambiar: () => void }) {
  const { puede } = useSesion()
  const [indicadores, setIndicadores] = useState<Indicador[]>([])
  const [actividades, setActividades] = useState<Actividad[]>([])
  const [form, setForm] = useState<FormIndicador | null>(null)
  const [actividad, setActividad] = useState({ leyenda: '', cantidad: '' })
  const [error, setError] = useState<string | null>(null)

  const cargar = () => {
    api<Indicador[]>(`/metas/${meta.id}/indicadores`).then(setIndicadores)
    api<Actividad[]>(`/metas/${meta.id}/actividades`).then(setActividades)
  }
  useEffect(cargar, [meta.id])

  const editar = (i: Indicador) => setForm({ id: i.id, leyenda: i.leyenda, descripcion: i.descripcion ?? '', variable: i.variable,
    puntos: i.puntos.map(p => ({ periodo: p.periodo, valor: p.valor === null ? '' : String(p.valor) })) })
  const setPunto = (n: number, cambio: Partial<FormIndicador['puntos'][0]>) =>
    setForm({ ...form!, puntos: form!.puntos.map((p, i) => i === n ? { ...p, ...cambio } : p) })

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const f = form!
    const datos = { leyenda: f.leyenda, descripcion: f.descripcion, variable: f.variable, puntos: f.puntos.map(p => ({ periodo: p.periodo, valor: Number(p.valor) })) }
    if (await intentar(() => f.id ? api(`/indicadores/${f.id}`, 'PUT', datos) : api(`/metas/${meta.id}/indicadores`, 'POST', datos), setError)) {
      setForm(null)
      cargar()
      alCambiar()
    }
  }

  async function borrar(i: Indicador) {
    if (confirm(`¿Borrar el indicador "${i.leyenda}"?`) && await intentar(() => api(`/indicadores/${i.id}`, 'DELETE'), setError)) {
      cargar()
      alCambiar()
    }
  }

  async function agregarActividad(e: FormEvent) {
    e.preventDefault()
    if (await intentar(() => api(`/metas/${meta.id}/actividades`, 'POST', { leyenda: actividad.leyenda, cantidad: Number(actividad.cantidad) }), setError)) {
      setActividad({ leyenda: '', cantidad: '' })
      cargar()
    }
  }

  return (
    <section className="tarjeta">
      <div className="barra-titulo">
        <h2>{meta.leyenda}</h2>
        {puede('nuevoIndicador') && <button onClick={() => setForm({ leyenda: '', descripcion: '', variable: '', puntos: [{ periodo: '', valor: '' }] })}>Nuevo indicador</button>}
      </div>
      <Aviso error={error} />

      {form && (
        <form className="formulario" onSubmit={guardar}>
          <h3>{form.id ? 'Editar indicador' : 'Nuevo indicador'}</h3>
          <label>Nombre<input value={form.leyenda} onChange={e => setForm({ ...form, leyenda: e.target.value })} required /></label>
          <label>Unidad / variable<input value={form.variable} onChange={e => setForm({ ...form, variable: e.target.value })} required placeholder="ej. VIVIENDAS" /></label>
          <label className="ancho">Descripción<input value={form.descripcion} onChange={e => setForm({ ...form, descripcion: e.target.value })} /></label>
          <div className="ancho">
            <table className="puntos">
              <thead><tr><th>Período</th><th>Valor</th><th /></tr></thead>
              <tbody>
                {form.puntos.map((p, n) => (
                  <tr key={n}>
                    <td><input value={p.periodo} onChange={e => setPunto(n, { periodo: e.target.value })} required placeholder="ej. T1 2026" /></td>
                    <td><input type="number" step="any" value={p.valor} onChange={e => setPunto(n, { valor: e.target.value })} required /></td>
                    <td><button type="button" className="enlace peligro" disabled={form.puntos.length === 1}
                                onClick={() => setForm({ ...form, puntos: form.puntos.filter((_, i) => i !== n) })}>Quitar</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
            <button type="button" className="secundario" onClick={() => setForm({ ...form, puntos: [...form.puntos, { periodo: '', valor: '' }] })}>Agregar período</button>
          </div>
          <div className="acciones"><button>Guardar</button><button type="button" className="secundario" onClick={() => setForm(null)}>Cancelar</button></div>
        </form>
      )}

      <PanelIndicadores meta={meta} indicadores={indicadores} acciones={i => <>
        {puede('editarIndicador') && <button className="enlace" onClick={() => editar(i)}>Editar indicador</button>}
        {puede('borrarIndicador') && <button className="enlace peligro" onClick={() => borrar(i)}>Borrar indicador</button>}
      </>} />

      <h3>Actividades</h3>
      <ul className="actividades">
        {actividades.map(a => (
          <li key={a.id}>
            <span>{a.leyenda}</span><strong>{a.cantidad}</strong>
            {puede('editarMeta') && <button className="enlace peligro"
              onClick={async () => confirm(`¿Quitar "${a.leyenda}"?`) && await intentar(() => api(`/actividades/${a.id}`, 'DELETE'), setError) && cargar()}>Quitar</button>}
          </li>
        ))}
        {actividades.length === 0 && <li className="tenue">Sin actividades</li>}
      </ul>
      {puede('editarMeta') && (
        <form className="en-linea" onSubmit={agregarActividad}>
          <input placeholder="Nueva actividad" value={actividad.leyenda} onChange={e => setActividad({ ...actividad, leyenda: e.target.value })} required />
          <input type="number" min={0} placeholder="Cantidad" value={actividad.cantidad} onChange={e => setActividad({ ...actividad, cantidad: e.target.value })} required />
          <button>Agregar</button>
        </form>
      )}
    </section>
  )
}
