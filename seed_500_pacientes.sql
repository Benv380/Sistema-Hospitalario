-- ============================================================
-- SCRIPT DE SEED: 500 pacientes distribuidos entre 4 medicos
-- Password para todos: rednorte2025
-- Hash BCrypt (rounds=10): $2a$10$K92GvikuQalkyY4JEu17iu/GLwpYzaB2M8YjGh3yEFh.0xiKWMg4a
--
-- EJECUCION:
--   Paso 1 (paciente_db):
--     psql -U postgres -d paciente_db -f seed_500_pacientes.sql
--   Paso 2 (lista_espera_db):
--     psql -U postgres -d lista_espera_db -f seed_500_lista_espera.sql
-- ============================================================

DO $$
DECLARE
  hash       TEXT := '$2a$10$K92GvikuQalkyY4JEu17iu/GLwpYzaB2M8YjGh3yEFh.0xiKWMg4a';
  uid        BIGINT;
  i          INT;
  nombre_val TEXT;
  apellido_val TEXT;
  email_val  TEXT;

  nombres_m  TEXT[] := ARRAY['Jose','Miguel','Felipe','Andres','Cristian','Pablo','Ricardo',
                              'Alejandro','Ignacio','Manuel','Gabriel','Eduardo','Fernando',
                              'Rodrigo','Francisco','Hector','Luis','Martin','Nicolas','Jorge'];
  nombres_f  TEXT[] := ARRAY['Claudia','Beatriz','Rosa','Lorena','Carolina','Daniela','Paola',
                              'Francisca','Javiera','Marcela','Alejandra','Sandra','Natalia',
                              'Verónica','Isabel','Patricia','Mónica','Andrea','Valentina','Pilar'];
  apellidos1 TEXT[] := ARRAY['Rojas','Perez','Fuentes','Silva','Gonzalez','Muñoz','Diaz',
                              'Morales','Gutierrez','Reyes','Vargas','Flores','Castro','Ortiz',
                              'Ramirez','Vega','Romero','Alvarez','Torres','Herrera'];
  apellidos2 TEXT[] := ARRAY['Lagos','Pino','Mena','Rios','Soto','Riquelme','Bravo',
                              'Campos','Espinoza','Valdes','Sepulveda','Marin','Araya','Correa',
                              'Figueroa','Navarro','Salinas','Molina','Parra','Leal'];
BEGIN
  FOR i IN 1..500 LOOP
    -- Alternar hombre/mujer por indice
    IF i % 2 = 0 THEN
      nombre_val   := nombres_m[1 + ((i/2 - 1) % array_length(nombres_m,1))];
      apellido_val := apellidos1[1 + ((i - 1) % array_length(apellidos1,1))]
                   || ' ' || apellidos2[1 + ((i + 7) % array_length(apellidos2,1))];
    ELSE
      nombre_val   := nombres_f[1 + (((i+1)/2 - 1) % array_length(nombres_f,1))];
      apellido_val := apellidos1[1 + (i % array_length(apellidos1,1))]
                   || ' ' || apellidos2[1 + ((i + 3) % array_length(apellidos2,1))];
    END IF;

    email_val := 'bulk.p' || lpad(i::text, 4, '0') || '@rednorte.cl';

    -- Insertar usuario
    INSERT INTO usuarios (email, password_hash, nombre_completo, rol, activo)
    VALUES (email_val, hash, nombre_val || ' ' || apellido_val, 'PACIENTE', true)
    RETURNING id INTO uid;

    -- Insertar paciente vinculado
    INSERT INTO pacientes (usuario_id, rut, nombre, apellido, fecha_nacimiento, email, telefono, direccion)
    VALUES (
      uid,
      'B' || lpad(i::text, 4, '0'),
      nombre_val,
      apellido_val,
      (DATE '1960-01-01' + ((i * 47 + 13) % (365*50)) * INTERVAL '1 day')::date,
      email_val,
      '+5691' || lpad((2000000 + i * 1237)::text, 7, '0'),
      'Calle Num ' || i || ', Santiago'
    );
  END LOOP;

  RAISE NOTICE 'Insertados 500 usuarios y pacientes correctamente.';
END $$;
