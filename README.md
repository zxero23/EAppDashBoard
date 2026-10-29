# EAppDashBoard · Tablero de Gestión v2

Tablero de control de gestión de la Provincia de Misiones: cada ministerio carga **objetivos → metas → indicadores**
y los entes ejecutores cargan **obras** georreferenciadas. El tablero muestra un semáforo por organismo
(en término · en alerta · riesgo alto · terminado), el avance de cada meta contra su indicador y las obras en el mapa.

## Estructura

```
backend/   Spring Boot 4.1.2 (Java 21) · API REST /api, JPA sobre PostgreSQL, Spring Security
frontend/  React 19 + Vite + TypeScript · Recharts (gráficos) y Leaflet/OpenStreetMap (mapa)
legacy/    v1 (Java EE + ICEfaces sobre Payara Micro), como referencia
```

El backend usa **el mismo esquema** que la v1 (`tableroDB`) y no lo modifica (`ddl-auto: none`), así que las dos
versiones pueden convivir sobre la misma base durante la migración.

## Desarrollo

Requisitos: JDK 21+, Maven 3.6.3+, Node 20+ y PostgreSQL con la base `tableroDB`.

```bash
# backend en http://localhost:8080
cd backend
DB_PASSWORD=... mvn spring-boot:run

# frontend en http://localhost:5173 (proxy de /api al backend)
cd frontend
npm install
npm run dev
```

Variables del backend: `DB_URL` (por defecto `jdbc:postgresql://localhost:5432/tableroDB`), `DB_USER` (`postgres`),
`DB_PASSWORD` (obligatoria, no hay valor por defecto), `PORT` (`8080`), `MIGRAR_CLAVES` (`true`).

## Build de producción

```bash
cd frontend && npm run build      # deja el front en backend/src/main/resources/static
cd ../backend && mvn package      # un solo jar que sirve front + API
DB_PASSWORD=... java -jar target/tablero-backend-2.0.0-SNAPSHOT.jar
```

## Seguridad y compatibilidad con la v1

- Login con sesión (cookie) y CSRF por cookie `XSRF-TOKEN`. Los permisos son las mismas **acciones** de la v1
  (`nuevaMeta`, `editarObra`, `borrarObjetivos`, …) asignadas por grupo o al usuario, y se validan en el backend.
- Alcance por organismo: un usuario del organismo `0` ve toda la provincia; el resto solo ve y edita lo suyo.
- **Claves**: la v1 las guardaba en texto plano. La v2 acepta esas claves y, con `MIGRAR_CLAVES=true`, las reemplaza
  por su hash BCrypt en el primer login. **Un usuario migrado ya no puede entrar a la v1.** Para convivir con la v1
  sin migrar, usar `MIGRAR_CLAVES=false`.
- Los indicadores siguen guardándose como `String[]` serializados (formato de la v1). Al leerlos solo se aceptan
  `String`/`String[]` (filtro de deserialización).

## Diferencias con la v1

- Los ids de situación del semáforo (7, 8, 10, 11) están en un solo lugar: `backend/.../modelo/Semaforo.java`.
- Igual que en la v1, la cantidad de una meta es un **% de variación respecto del primer valor del indicador**
  ("reducir 20" = bajar un 20% desde la línea de base). Lo nuevo es el **avance**: qué parte de ese cambio ya se
  logró con el último valor, más un panel con último valor, valor meta, variación contra el período anterior y
  cuánto falta. En el gráfico, las barras de los períodos que ya cumplen la meta se pintan de verde.
- El mapa usa OpenStreetMap: no hace falta API key de Google.

## Pendiente respecto de la v1

Administración de usuarios, grupos, menús y acciones; ABM de organismos, localidades y situaciones; reportes PDF
(Jasper); fotos y adjuntos de obras; capas KML de indicadores.
