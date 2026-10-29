import { useEffect, useState, type FormEvent } from 'react'
import { api, query, type Obra } from '../api'
import { useSesion } from '../App'
import { Aviso, Chip, fecha, intentar, Mapa, Opciones } from '../componentes'

type Form = {
  id?: number; leyenda: string; latitud: string; longitud: string; fechaInicio: string; fechaFinal: string
  organismoId: string; situacionId: string; localidadId: string
}

export default function Obras() {
  const { cat, yo, puede } = useSesion()
  const [filtro, setFiltro] = useState({ organismoId: '', situacionId: '', localidadId: '' })
  const [lista, setLista] = useState<Obra[]>([])
  const [form, setForm] = useState<Form | null>(null)
  const [error, setError] = useState<string | null>(null)
  const entes = cat.organismos.filter(o => o.descentralizado)

  const cargar = () => api<Obra[]>('/obras' + query(filtro)).then(setLista)
  useEffect(() => { cargar() }, [filtro])

  const nuevo = () => setForm({ leyenda: '', latitud: '', longitud: '', fechaInicio: '', fechaFinal: '',
    organismoId: filtro.organismoId || (yo.global ? '' : String(yo.organismoId)), situacionId: String(cat.semaforo[0]?.id ?? ''), localidadId: filtro.localidadId })
  const editar = (o: Obra) => setForm({ id: o.id, leyenda: o.leyenda, latitud: String(o.latitud ?? ''), longitud: String(o.longitud ?? ''),
    fechaInicio: o.fechaInicio ?? '', fechaFinal: o.fechaFinal ?? '', organismoId: String(o.organismoId),
    situacionId: String(o.situacionId ?? ''), localidadId: String(o.localidadId ?? '') })

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const f = form!
    const datos = { ...f, id: undefined, latitud: Number(f.latitud), longitud: Number(f.longitud),
      organismoId: Number(f.organismoId), situacionId: Number(f.situacionId), localidadId: Number(f.localidadId) }
    if (await intentar(() => f.id ? api(`/obras/${f.id}`, 'PUT', datos) : api('/obras', 'POST', datos), setError)) {
      setForm(null)
      cargar()
    }
  }

  async function borrar(o: Obra) {
    if (confirm(`¿Dar de baja la obra "${o.leyenda}"?`) && await intentar(() => api(`/obras/${o.id}`, 'DELETE'), setError)) cargar()
  }

  const set = (cambio: Partial<typeof filtro>) => setFiltro({ ...filtro, ...cambio })
  // Mientras se edita, el mapa muestra el punto elegido y un click lo mueve.
  const enMapa = form ? [...lista.filter(o => o.id !== form.id), {
    ...lista[0], id: -1, leyenda: form.leyenda || 'Nueva ubicación', situacionId: Number(form.situacionId),
    latitud: form.latitud ? Number(form.latitud) : null, longitud: form.longitud ? Number(form.longitud) : null,
  } as Obra] : lista

  return (
    <>
    <h1 className="titulo-pagina">Obras</h1>
    <p className="subtitulo">Obras de los entes ejecutores, ubicadas en el mapa de la provincia.</p>
    <section className="tarjeta">
      <div className="barra-titulo">
        <h2 className="tenue">{lista.length} obras</h2>
        {yo.global && <select value={filtro.organismoId} onChange={e => set({ organismoId: e.target.value })}><Opciones items={entes} vacio="Todos los entes" /></select>}
        <select value={filtro.situacionId} onChange={e => set({ situacionId: e.target.value })}><Opciones items={cat.situaciones} vacio="Todas las situaciones" /></select>
        <select value={filtro.localidadId} onChange={e => set({ localidadId: e.target.value })}><Opciones items={cat.localidades} vacio="Todas las localidades" /></select>
        {puede('nuevaObra') && <button onClick={nuevo}>Nueva obra</button>}
      </div>
      <Aviso error={error} />

      {form && (
        <form className="formulario" onSubmit={guardar}>
          <h3>{form.id ? 'Editar obra' : 'Nueva obra'} <small className="tenue">· hacé click en el mapa para ubicarla</small></h3>
          <label className="ancho">Nombre<input value={form.leyenda} onChange={e => setForm({ ...form, leyenda: e.target.value })} required /></label>
          <label>Ente<select value={form.organismoId} onChange={e => setForm({ ...form, organismoId: e.target.value })} required><Opciones items={entes.length ? entes : cat.organismos} vacio="" /></select></label>
          <label>Localidad<select value={form.localidadId} onChange={e => setForm({ ...form, localidadId: e.target.value })} required><Opciones items={cat.localidades} vacio="" /></select></label>
          <label>Situación<select value={form.situacionId} onChange={e => setForm({ ...form, situacionId: e.target.value })} required><Opciones items={cat.situaciones} vacio="" /></select></label>
          <label>Latitud<input type="number" step="any" min={-90} max={90} value={form.latitud} onChange={e => setForm({ ...form, latitud: e.target.value })} required /></label>
          <label>Longitud<input type="number" step="any" min={-180} max={180} value={form.longitud} onChange={e => setForm({ ...form, longitud: e.target.value })} required /></label>
          <label>Inicio<input type="date" value={form.fechaInicio} onChange={e => setForm({ ...form, fechaInicio: e.target.value })} required /></label>
          <label>Fin<input type="date" value={form.fechaFinal} onChange={e => setForm({ ...form, fechaFinal: e.target.value })} required /></label>
          <div className="acciones"><button>Guardar</button><button type="button" className="secundario" onClick={() => setForm(null)}>Cancelar</button></div>
        </form>
      )}

      <Mapa cat={cat} obras={enMapa}
            onClick={form ? (lat, lng) => setForm({ ...form, latitud: lat.toFixed(5), longitud: lng.toFixed(5) }) : undefined} />

      <table>
        <thead><tr><th>Obra</th><th>Ente</th><th>Localidad</th><th>Situación</th><th>Plazo</th><th /></tr></thead>
        <tbody>
          {lista.map(o => (
            <tr key={o.id}>
              <td>{o.leyenda}</td><td>{o.organismo}</td><td>{o.localidad}</td>
              <td><Chip cat={cat} id={o.situacionId} texto={o.situacion} /></td>
              <td className="nowrap">{fecha(o.fechaInicio)} – {fecha(o.fechaFinal)}</td>
              <td className="nowrap">
                {puede('editarObra') && <button className="enlace" onClick={() => editar(o)}>Editar</button>}
                {puede('borrarObra') && <button className="enlace peligro" onClick={() => borrar(o)}>Borrar</button>}
              </td>
            </tr>
          ))}
          {lista.length === 0 && <tr><td colSpan={6} className="tenue">Sin obras</td></tr>}
        </tbody>
      </table>
    </section>
    </>
  )
}
