import { useEffect, useState, type FormEvent } from 'react'
import { api, query, type Objetivo } from '../api'
import { useSesion } from '../App'
import { Aviso, Chip, intentar, Opciones } from '../componentes'

type Form = { id?: number; leyenda: string; descripcion: string; organismoId: string; situacionId: string }

export default function Objetivos() {
  const { cat, yo, puede } = useSesion()
  const [organismo, setOrganismo] = useState('')
  const [lista, setLista] = useState<Objetivo[]>([])
  const [form, setForm] = useState<Form | null>(null)
  const [error, setError] = useState<string | null>(null)

  const cargar = () => api<Objetivo[]>('/objetivos' + query({ organismoId: organismo })).then(setLista)
  useEffect(() => { cargar() }, [organismo])

  const nuevo = () => setForm({ leyenda: '', descripcion: '', organismoId: organismo || String(yo.global ? '' : yo.organismoId), situacionId: String(cat.semaforo[0]?.id ?? '') })
  const editar = (o: Objetivo) => setForm({ id: o.id, leyenda: o.leyenda, descripcion: o.descripcion ?? '', organismoId: String(o.organismoId), situacionId: String(o.situacionId ?? '') })

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const f = form!
    const datos = { leyenda: f.leyenda, descripcion: f.descripcion, organismoId: Number(f.organismoId), situacionId: Number(f.situacionId) }
    if (await intentar(() => f.id ? api(`/objetivos/${f.id}`, 'PUT', datos) : api('/objetivos', 'POST', datos), setError)) {
      setForm(null)
      cargar()
    }
  }

  async function borrar(o: Objetivo) {
    if (confirm(`¿Dar de baja el objetivo "${o.leyenda}"? Sus metas dejan de verse en el tablero.`))
      if (await intentar(() => api(`/objetivos/${o.id}`, 'DELETE'), setError)) cargar()
  }

  return (
    <section className="tarjeta">
      <div className="barra-titulo">
        <h2>Objetivos</h2>
        {yo.global && <select value={organismo} onChange={e => setOrganismo(e.target.value)}><Opciones items={cat.organismos} vacio="Todos los organismos" /></select>}
        {puede('nuevoObjetivos') && <button onClick={nuevo}>Nuevo objetivo</button>}
      </div>
      <Aviso error={error} />

      {form && (
        <form className="formulario" onSubmit={guardar}>
          <h3>{form.id ? 'Editar objetivo' : 'Nuevo objetivo'}</h3>
          <label>Nombre<input value={form.leyenda} onChange={e => setForm({ ...form, leyenda: e.target.value })} required /></label>
          <label>Organismo<select value={form.organismoId} onChange={e => setForm({ ...form, organismoId: e.target.value })} required><Opciones items={cat.organismos} vacio="" /></select></label>
          <label>Situación<select value={form.situacionId} onChange={e => setForm({ ...form, situacionId: e.target.value })} required><Opciones items={cat.situaciones} vacio="" /></select></label>
          <label className="ancho">Descripción<textarea value={form.descripcion} onChange={e => setForm({ ...form, descripcion: e.target.value })} rows={3} /></label>
          <div className="acciones"><button>Guardar</button><button type="button" className="secundario" onClick={() => setForm(null)}>Cancelar</button></div>
        </form>
      )}

      <table>
        <thead><tr><th>Organismo</th><th>Objetivo</th><th>Situación</th><th /></tr></thead>
        <tbody>
          {lista.map(o => (
            <tr key={o.id}>
              <td>{o.organismo}</td>
              <td>{o.leyenda}{o.descripcion && o.descripcion !== o.leyenda && <small className="tenue bloque">{o.descripcion}</small>}</td>
              <td><Chip cat={cat} id={o.situacionId} texto={o.situacion} /></td>
              <td className="nowrap">
                {puede('editarObjetivos') && <button className="enlace" onClick={() => editar(o)}>Editar</button>}
                {puede('borrarObjetivos') && <button className="enlace peligro" onClick={() => borrar(o)}>Borrar</button>}
              </td>
            </tr>
          ))}
          {lista.length === 0 && <tr><td colSpan={4} className="tenue">Sin objetivos</td></tr>}
        </tbody>
      </table>
    </section>
  )
}
