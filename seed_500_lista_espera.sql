-- ============================================================
-- SCRIPT PASO 2: Lista de espera + citas para 500 pacientes
--
-- EJECUCION (reemplaza <UID_START> con el valor real):
--   psql -U postgres -d lista_espera_db \
--        -v uid_start=<UID_START> \
--        -f seed_500_lista_espera.sql
--
-- Para obtener <UID_START> ejecuta primero en paciente_db:
--   SELECT min(id) FROM usuarios WHERE email LIKE 'bulk.p%@rednorte.cl';
--
-- Ejemplo si uid_start=22:
--   psql -U postgres -d lista_espera_db -v uid_start=22 -f seed_500_lista_espera.sql
-- ============================================================

DO $$
DECLARE
  uid_start  BIGINT := :uid_start;
  uid        BIGINT;
  i          INT;
  le_id      BIGINT;
  estado_le  TEXT;
  estado_cita TEXT;
  medicos    TEXT[] := ARRAY['Carlos Mendoza','Isabel Torres','Patricia Morales','Rodrigo Vasquez'];
  especialidades TEXT[] := ARRAY['Cardiologia','Traumatologia','Ginecologia','Neurologia'];
  med_idx    INT;
  espec      TEXT;
  medico     TEXT;
  prioridad  INT;
  fecha_sol  TIMESTAMP;
  fecha_cita TIMESTAMP;
  box_num    TEXT;
  obs_text   TEXT;
BEGIN
  FOR i IN 0..499 LOOP
    uid     := uid_start + i;
    med_idx := (i % 4) + 1;
    espec   := especialidades[med_idx];
    medico  := medicos[med_idx];
    prioridad := (i % 3) + 1;
    box_num := ((med_idx * 2) + (i % 3))::text;
    fecha_sol := NOW() - ((i % 120) + 1) * INTERVAL '1 day' + (i % 8) * INTERVAL '1 hour';

    IF i < 200 THEN
      estado_le   := 'ATENDIDO';
      estado_cita := 'REALIZADA';
      fecha_cita  := fecha_sol + ((5 + i % 20) * INTERVAL '1 day') + (8 + i % 4) * INTERVAL '1 hour';
      obs_text    := 'Atencion completada. Paciente dado de alta.';
    ELSIF i < 300 THEN
      estado_le   := 'ASIGNADO';
      estado_cita := CASE WHEN i % 2 = 0 THEN 'PROGRAMADA' ELSE 'CONFIRMADA' END;
      fecha_cita  := NOW() + ((i - 200 + 1) * INTERVAL '1 day') + (9 + i % 8) * INTERVAL '1 hour';
      obs_text    := 'Cita agendada. Confirmar asistencia con 24h de anticipacion.';
    ELSE
      estado_le   := 'PENDIENTE';
      estado_cita := NULL;
      fecha_cita  := NULL;
      obs_text    := 'En lista de espera. Prioridad ' || prioridad || '.';
    END IF;

    INSERT INTO lista_espera
      (paciente_id,especialidad,hospital,estado,prioridad,fecha_solicitud,fecha_atencion,observaciones)
    VALUES (
      uid, espec, 'Hospital Rednorte Norte', estado_le, prioridad,
      fecha_sol,
      CASE WHEN estado_le = 'ATENDIDO' THEN fecha_cita ELSE NULL END,
      obs_text
    ) RETURNING id INTO le_id;

    IF estado_cita IS NOT NULL THEN
      INSERT INTO citas_medicas
        (lista_espera_id,nombre_medico,especialidad,fecha_hora_cita,hospital,box_numero,estado,observaciones)
      VALUES (
        le_id, medico, espec, fecha_cita, 'Hospital Rednorte Norte', box_num, estado_cita,
        CASE
          WHEN estado_cita = 'REALIZADA'  THEN 'Consulta realizada sin complicaciones.'
          WHEN estado_cita = 'PROGRAMADA' THEN 'Traer examenes previos y carnet de identidad.'
          WHEN estado_cita = 'CONFIRMADA' THEN 'Cita confirmada por paciente. Box asignado.'
        END
      );
    END IF;
  END LOOP;

  RAISE NOTICE 'Seed completado: 500 solicitudes distribuidas en 4 medicos.';
END $$;
