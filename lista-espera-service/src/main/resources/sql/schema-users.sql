CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    rol             VARCHAR(30)  NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    nombre_completo VARCHAR(255)
);

-- Tablas pacientes, medicos, funcionarios, citas, diagnosticos, examenes, lista_espera
-- se pueden generar como entidades JPA en paquetes:
--   cl.duoc.rednorte.pacientes
--   cl.duoc.rednorte.medicos
--   cl.duoc.rednorte.funcionarios
--   cl.duoc.rednorte.citas
--   cl.duoc.rednorte.diagnosticos
--   cl.duoc.rednorte.examenes
--   cl.duoc.rednorte.listaespera