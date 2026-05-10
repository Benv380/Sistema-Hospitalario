# Informe de Arquitectura — Sistema RedNorte
**Fecha:** Mayo 2026 | **Estado:** Refactoring completado 

---

## 1. Visión General

RedNorte es un sistema hospitalario compuesto por **3 microservicios independientes** más un **frontend React**, siguiendo el patrón **BFF (Backend For Frontend)**. Cada servicio posee su propia base de datos, expone su propia API REST y se comunica entre sí mediante llamadas HTTP internas.

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend React :5173                      │
│           (Vite + React Router + Axios + TailwindCSS)       │
└──────────┬──────────────┬────────────────┬──────────────────┘
           │              │                │
    :8081 (Auth+       :8082 (Auth+     :8083 (Lista de Espera)
    Pacientes)         Médicos/Staff)   [BFF Aggregator]
           │              │                │
    ┌──────▼──────┐ ┌─────▼──────┐ ┌──────▼──────┐
    │ paciente_db │ │  medico_db │ │lista_espera_db│
    └─────────────┘ └────────────┘ └──────────────┘
```

---

## 2. Microservicio 1 — paciente-service

| Atributo | Valor |
|---|---|
| Puerto | `8081` |
| Base de datos | `paciente_db` |
| Paquete base | `cl.duoc.rednorte.paciente` |

### Responsabilidades
- Autenticación de **pacientes** (registro + login)
- CRUD completo de fichas de paciente
- Exposición de endpoint interno para consultas inter-servicio

### Endpoints

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | — | Login devuelve JWT + redirectUrl |
| POST | `/api/auth/register` | — | Registro de nuevo paciente |
| GET | `/api/v1/pacientes` | PACIENTE, FUNCIONARIO, ADMIN | Listar todos |
| GET | `/api/v1/pacientes/buscar?q=` | PACIENTE, FUNCIONARIO, ADMIN | Buscar por RUT/nombre/apellido |
| GET | `/api/v1/pacientes/{id}` | PACIENTE, FUNCIONARIO, ADMIN | Obtener por ID |
| PUT | `/api/v1/pacientes/{id}` | PACIENTE, FUNCIONARIO, ADMIN | Actualizar ficha |
| DELETE | `/api/v1/pacientes/{id}` | ADMIN | Eliminar |
| GET | `/api/internal/pacientes/{id}` | Sin auth (inter-servicio) | Datos planos para lista-espera |

### Modelo de datos (paciente_db)
```sql
usuarios (id, email, password_hash, nombre_completo, rol, activo)
pacientes (id, rut, nombre, apellido, fecha_nacimiento, email, telefono, direccion)
```

### AuthResponse
```json
{ "token": "...", "rol": "PACIENTE", "nombre": "Juan Pérez",
  "redirectUrl": "/paciente/dashboard", "id": "5" }
```

---

## 3. Microservicio 2 — medico-service

| Atributo | Valor |
|---|---|
| Puerto | `8082` |
| Base de datos | `medico_db` |
| Paquete base | `cl.duoc.rednorte.medico` |

### Responsabilidades
- Autenticación de **médicos, funcionarios y administradores**
- CRUD de médicos
- Gestión de cuentas de staff (solo ADMIN_SOFTWARE)

### Roles gestionados
`MEDICO` · `FUNCIONARIO` · `ADMIN_HOSPITAL` · `ADMIN_SOFTWARE`

### Endpoints

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | — | Login de staff |
| POST | `/api/auth/crear-usuario` | ADMIN | Crear cuenta de médico/funcionario |
| GET | `/api/v1/medicos` | Autenticado | Listar médicos |
| GET | `/api/v1/medicos/{id}` | Autenticado | Obtener por ID |
| GET | `/api/internal/medicos/{id}` | Sin auth (inter-servicio) | Datos para validación |

### Modelo de datos (medico_db)
```sql
usuarios (id, email, password_hash, nombre_completo, rol, activo)
medicos (id, usuario_id, rut, nombre, apellido, especialidad, hospital, email, telefono)
```

### AuthResponse
```json
{ "token": "...", "rol": "MEDICO", "nombre": "Dr. Torres",
  "redirectUrl": "/medico/dashboard", "id": "2" }
```

---

## 4. Microservicio 3 — lista-espera-service *(BFF)*

| Atributo | Valor |
|---|---|
| Puerto | `8083` |
| Base de datos | `lista_espera_db` |
| Paquete base | `cl.duoc.rednorte.listaespera` |

### Responsabilidades
- Gestión completa del ciclo de vida de solicitudes de lista de espera
- Agendamiento y seguimiento de citas médicas
- **BFF (Backend For Frontend)**: agrega datos en una sola llamada por vista
- Comunicación REST interna con `paciente-service` para obtener datos del paciente al registrar
- Cálculo de posición y tiempo estimado de espera
- Notificaciones (patrón Factory Method — EMAIL / SMS)

### Patrón BFF implementado

El `BffController` expone endpoints de agregación que reducen los round-trips del frontend:

| Vista | Sin BFF | Con BFF |
|---|---|---|
| PacienteDashboard | 3 llamadas API | **1 llamada** `/bff/paciente/{id}` |
| MedicoDashboard | 2 llamadas API | **1 llamada** `/bff/medico/{id}` |

### Endpoints

#### Lista de Espera (`/api/v1/lista-espera`)
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| GET | `/pendientes` | FUNCIONARIO, MEDICO, ADMIN | Cola completa ordenada por prioridad |
| GET | `/estado/{estado}` | Todos | Filtrar por PENDIENTE/ASIGNADO/ATENDIDO/CANCELADO |
| GET | `/paciente/{id}` | Todos | Solicitudes de un paciente |
| GET | `/{id}` | Todos | Obtener solicitud por ID |
| POST | `/` | PACIENTE, FUNCIONARIO | **Crear nueva solicitud** |
| PUT | `/{id}/asignar` | FUNCIONARIO, ADMIN | PENDIENTE → ASIGNADO |
| PUT | `/{id}/atender` | MEDICO, FUNCIONARIO, ADMIN | ASIGNADO → ATENDIDO |
| PUT | `/{id}/cancelar` | PACIENTE, FUNCIONARIO, ADMIN | → CANCELADO |
| DELETE | `/{id}` | ADMIN | Eliminar físicamente |

#### Citas Médicas (`/api/v1/citas`)
| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| POST | `/` | FUNCIONARIO | Agendar cita (vincula a solicitud) |
| GET | `/{id}` | Todos | Cita por ID |
| GET | `/solicitud/{id}` | Todos | Cita de una solicitud |
| GET | `/estado/{estado}` | FUNCIONARIO, MEDICO, ADMIN | Por estado |
| GET | `/paciente/{id}` | Todos | Citas de un paciente |
| GET | `/medico/{id}` | MEDICO, ADMIN | Citas de un médico |
| GET | `/reasignacion` | FUNCIONARIO, ADMIN | Citas disponibles para reasignar |
| PUT | `/{id}/confirmar` | PACIENTE, FUNCIONARIO | PROGRAMADA → CONFIRMADA |
| PUT | `/{id}/realizar` | MEDICO | → REALIZADA |
| PUT | `/{id}/cancelar` | PACIENTE, FUNCIONARIO, ADMIN | → CANCELADA |
| PUT | `/{id}/no-asistio` | FUNCIONARIO, MEDICO, ADMIN | → NO_ASISTIO |

#### BFF (`/api/v1/bff`)
| Método | Ruta | Rol | Respuesta |
|---|---|---|---|
| GET | `/paciente/{id}` | PACIENTE, FUNCIONARIO, ADMIN | `{ solicitudes, citas, solicitudesActivas, proximaCita }` |
| GET | `/medico/{id}` | MEDICO, FUNCIONARIO, ADMIN | `{ citasHoy, totalPendientes, atendidosHoy, citasProgramadasHoy }` |

### Ciclo de estados

```
SOLICITUD:  PENDIENTE ──(asignar)──► ASIGNADO ──(atender)──► ATENDIDO
                │                       │
                └──────(cancelar)────────┘──► CANCELADO

CITA:  PROGRAMADA ──(confirmar)──► CONFIRMADA ──(realizar)──► REALIZADA
           │                                                      
           └──(cancelar)──► CANCELADA  (solicitud vuelve a PENDIENTE)
           └──(no-asistio)──► NO_ASISTIO
```

### Modelo de datos (lista_espera_db)
```sql
lista_espera (
  id BIGSERIAL PRIMARY KEY,
  paciente_id         BIGINT NOT NULL,       -- referencia lógica (no FK)
  paciente_nombre     VARCHAR(200) NOT NULL, -- desnormalizado
  paciente_rut        VARCHAR(12)  NOT NULL,
  paciente_email      VARCHAR(150),
  especialidad        VARCHAR(100) NOT NULL,
  hospital            VARCHAR(100) NOT NULL,
  estado              VARCHAR(20)  NOT NULL,  -- PENDIENTE|ASIGNADO|ATENDIDO|CANCELADO
  prioridad           INT NOT NULL,           -- 1=Alta 2=Media 3=Baja
  fecha_solicitud     TIMESTAMP NOT NULL,
  fecha_atencion      TIMESTAMP,
  observaciones       VARCHAR(500)
)

citas_medicas (
  id BIGSERIAL PRIMARY KEY,
  lista_espera_id  BIGINT UNIQUE NOT NULL REFERENCES lista_espera(id),
  medico_id        BIGINT,                   -- referencia lógica
  nombre_medico    VARCHAR(100) NOT NULL,
  especialidad     VARCHAR(100) NOT NULL,
  hospital         VARCHAR(100) NOT NULL,
  box_numero       VARCHAR(10)  NOT NULL,
  fecha_hora_cita  TIMESTAMP    NOT NULL,
  estado           VARCHAR(20)  NOT NULL,    -- PROGRAMADA|CONFIRMADA|REALIZADA|CANCELADA|NO_ASISTIO
  observaciones    VARCHAR(500)
)
```

### Campos calculados (no persisten)
- `posicion` (`@Transient`) — número de solicitudes PENDIENTE antes que la actual
- `tiempoEstimadoMinutos` (`@Transient`) — `(posicion - 1) × 30`

### Comunicación inter-servicio
```
lista-espera-service ──GET──► paciente-service
  /api/internal/pacientes/{id}
  → obtiene nombre, rut, email del paciente al crear solicitud
  → PacienteClient usa RestTemplate con fallback ante fallo
```

### Circuit Breaker
- `obtenerPendientes()` protegido con Resilience4j
- Si `paciente-service` falla → devuelve lista vacía (fallback)
- Configuración: ventana=10, umbral fallo=50%, espera=10s

---

## 5. Frontend React

| Atributo | Valor |
|---|---|
| Puerto | `5173` |
| Framework | React 18 + Vite |
| Estilos | TailwindCSS |
| HTTP | Axios (3 instancias independientes) |

### Instancias Axios

| Archivo | Base URL | Servicio |
|---|---|---|
| `pacienteApi.js` | `http://localhost:8081/api` | paciente-service |
| `medicoApi.js` | `http://localhost:8082/api` | medico-service |
| `listaEsperaApi.js` | `http://localhost:8083/api` | lista-espera-service |

Cada instancia adjunta automáticamente el JWT (`Bearer <token>`) en cada request y redirige a `/login` ante un 401.

### Servicios frontend

| Archivo | Instancia axios | Propósito |
|---|---|---|
| `authService.js` | axios directo (sin instancia) | Login multi-servicio con fallback |
| `pacienteService.js` | `pacienteApi` | CRUD + búsqueda de pacientes |
| `medicoService.js` | `pacienteApi` + `listaEsperaApi` | Datos de médico y lista |
| `listaEsperaService.js` | `listaEsperaApi` | Gestión de solicitudes |
| `citaService.js` | `listaEsperaApi` | Gestión de citas |
| `bffService.js` | `listaEsperaApi` | Llamadas BFF agregadas |
| `reasignacionService.js` | `listaEsperaApi` | Citas disponibles para reasignar |

### Flujo de login (multi-servicio)
```
Usuario ingresa email + password
        │
        ▼
authService.login()
        │
        ├──► POST :8081/api/auth/login (paciente-service)
        │         Si OK → token con rol=PACIENTE
        │
        └── Si falla ──► POST :8082/api/auth/login (medico-service)
                              Si OK → token con rol=MEDICO|FUNCIONARIO|ADMIN
                              Si falla → error "Credenciales incorrectas"

Token guardado en localStorage → adjuntado en todas las requests siguientes
JWT válido en los 3 servicios (mismo secreto compartido)
```

### Rutas por rol

| Rol | Dashboard | Funcionalidades |
|---|---|---|
| PACIENTE | `/paciente/dashboard` | Ver solicitudes + posición en cola, nueva solicitud, mis citas |
| MEDICO | `/medico/dashboard` | Agenda del día, atender pacientes, historia clínica |
| FUNCIONARIO | `/funcionario/dashboard` | Registrar paciente en lista de espera, gestión |
| ADMIN_HOSPITAL | `/admin/dashboard` | Gestión global, lista de espera completa |
| ADMIN_SOFTWARE | `/admin-soft/dashboard` | Gestión de cuentas y permisos |

---

## 6. Seguridad

### JWT compartido
- Mismo `jwt.secret` en los 3 servicios → token emitido en cualquier servicio es válido en los otros
- Expiración: 24 horas (`86400000` ms)
- Claim extra `"rol"` en el payload para autorización sin consultar BD

### Validación de JWT en lista-espera-service
```
JwtAuthFilter.doFilterInternal()
  1. Extrae token del header Authorization: Bearer <token>
  2. Parsea email = JwtService.extractEmail(token)
  3. Parsea rol   = JwtService.extractRol(token)
  4. Verifica que no haya expirado
  5. Construye UserDetails en memoria con ROLE_{rol}
  6. Setea SecurityContextHolder → Spring aplica @PreAuthorize
  (Sin consulta a BD — no hay tabla de usuarios en lista_espera_db)
```

### Endpoints internos (sin auth)
- `GET /api/internal/**` en paciente-service y medico-service → solo para llamadas entre servicios
- No exponer estos endpoints en producción sin firewall de red

---

## 7. Bases de datos

### Creación (ejecutar en orden)
```sql
-- Conectar como superusuario a PostgreSQL
CREATE DATABASE paciente_db;
CREATE DATABASE medico_db;
CREATE DATABASE lista_espera_db;
```

### Tablas (creadas automáticamente con ddl-auto=update)
Cada servicio crea sus tablas al arrancar por primera vez.

### Datos de prueba
Cada servicio tiene `src/main/resources/sql/data.sql`.

**Contraseña de todos los usuarios de prueba:** `password123`
**Hash BCrypt:** `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

| Email | Rol | Servicio |
|---|---|---|
| juan.perez@mail.cl | PACIENTE | paciente-service |
| maria.gonzalez@mail.cl | PACIENTE | paciente-service |
| carlos.silva@mail.cl | PACIENTE | paciente-service |
| dr.torres@medhospital.cl | MEDICO | medico-service |
| dra.vargas@medhospital.cl | MEDICO | medico-service |
| funcionario@hospital.cl | FUNCIONARIO | medico-service |
| admin@rednorte.cl | ADMIN_HOSPITAL | medico-service |

---

## 8. Variables de entorno

### Backend (cada servicio)
| Variable | Default | Descripción |
|---|---|---|
| `DB_URL` | jdbc:postgresql://localhost:5432/{db} | URL de conexión |
| `DB_USERNAME` | postgres | Usuario BD |
| `DB_PASSWORD` | brisas123! | Contraseña BD |
| `JWT_SECRET` | rednorte-super-secret-key-2025... | Secreto JWT (mismo en los 3) |
| `JWT_EXPIRATION` | 86400000 | Expiración en ms (24h) |
| `PACIENTE_SERVICE_URL` | http://localhost:8081 | URL de paciente-service (solo lista-espera usa esto) |

### Frontend
| Variable | Valor |
|---|---|
| `VITE_PACIENTE_API_URL` | http://localhost:8081/api |
| `VITE_MEDICO_API_URL` | http://localhost:8082/api |
| `VITE_LISTA_API_URL` | http://localhost:8083/api |

---

## 9. Orden de arranque

```bash
# 1. PostgreSQL debe estar corriendo

# 2. paciente-service
cd paciente-service && mvn spring-boot:run

# 3. medico-service
cd medico-service && mvn spring-boot:run

# 4. lista-espera-service (depende de paciente-service para inter-servicio)
cd lista-espera-service && mvn spring-boot:run

# 5. Frontend
cd rednorte-frontend && npm run dev
```

---

## 10. Estructura de archivos — lista-espera-service (limpio)

```
lista-espera-service/
├── config/
│   ├── JwtAuthFilter.java       ← valida JWT sin consultar BD
│   ├── JwtService.java          ← extrae email y rol del token
│   ├── RestTemplateConfig.java  ← bean RestTemplate para PacienteClient
│   └── SecurityConfig.java      ← CORS, STATELESS, @PreAuthorize
├── client/
│   └── PacienteClient.java      ← llama a paciente-service internamente
├── controller/
│   ├── BffController.java       ← /api/v1/bff/* (agrega datos por rol)
│   ├── CitaMedicaController.java ← /api/v1/citas/*
│   └── ListaEsperaController.java ← /api/v1/lista-espera/*
├── dto/
│   ├── CitaMedicaDTO.java
│   ├── ListaEsperaDTO.java
│   ├── MedicoDashboardDTO.java  ← BFF response para médico
│   └── PacienteDashboardDTO.java ← BFF response para paciente
├── exception/
│   └── GlobalExceptionHandler.java
├── model/
│   ├── CitaMedica.java
│   ├── ListaEspera.java
│   ├── Notificacion.java        ← interfaz (patrón Factory)
│   ├── NotificacionEmail.java
│   └── NotificacionSMS.java
├── repository/
│   ├── CitaMedicaRepository.java
│   └── ListaEsperaRepository.java
├── service/
│   ├── CitaMedicaService.java
│   ├── ListaEsperaService.java
│   └── NotificacionFactory.java ← Factory Method pattern
└── ListaEsperaServiceApplication.java
```

---

## 11. Patrones de diseño aplicados

| Patrón | Dónde | Por qué |
|---|---|---|
| **BFF (Backend For Frontend)** | `BffController` | Reduce round-trips del frontend; un endpoint por vista |
| **Circuit Breaker** | `ListaEsperaService.obtenerPendientes` | Tolerancia a fallos cuando paciente-service no responde |
| **Factory Method** | `NotificacionFactory` | Desacopla la creación de EMAIL/SMS sin if-else |
| **Repository** | `ListaEsperaRepository`, `CitaMedicaRepository` | Abstracción de acceso a datos |
| **DTO** | `ListaEsperaDTO`, `CitaMedicaDTO`, BFF DTOs | Separa la API pública del modelo de dominio |
| **Stateless JWT** | `JwtAuthFilter` | Auth sin sesión; escala horizontalmente |

---

*Generado automáticamente — RedNorte Hospital System v2.0*
