-- 0002_consolidacion: integridad, seguridad y rendimiento.
--
-- Se ejecuta dentro de una única transacción: si algún paso falla no se
-- aplica nada. Está pensada para funcionar sobre los datos existentes
-- (limpia huérfanos y duplicados antes de añadir restricciones).

CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ---------------------------------------------------------------------------
-- usuarios: columna de hash sin caracteres no ASCII, control de sesiones y
-- bloqueo por intentos fallidos.
-- ---------------------------------------------------------------------------
ALTER TABLE usuarios RENAME COLUMN "contraseña" TO password_hash;
ALTER TABLE usuarios
    ADD COLUMN token_version integer NOT NULL DEFAULT 0,
    ADD COLUMN failed_logins integer NOT NULL DEFAULT 0,
    ADD COLUMN locked_until timestamp without time zone,
    ADD COLUMN last_login_at timestamp without time zone,
    ADD COLUMN created_at timestamp without time zone NOT NULL DEFAULT now();

UPDATE usuarios SET email = lower(trim(email)) WHERE email <> lower(trim(email));
CREATE UNIQUE INDEX usuarios_email_lower_key ON usuarios (lower(email));
CREATE UNIQUE INDEX usuarios_nombreusuario_lower_key ON usuarios (lower(nombreusuario));

-- ---------------------------------------------------------------------------
-- Sesiones (refresh tokens rotatorios; solo se guarda el hash)
-- ---------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id bigserial PRIMARY KEY,
    usuarioid integer NOT NULL REFERENCES usuarios(usuarioid) ON DELETE CASCADE,
    token_hash char(64) NOT NULL UNIQUE,
    family_id varchar(64) NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    revoked_at timestamp without time zone,
    replaced_by char(64),
    user_agent varchar(200)
);
CREATE INDEX refresh_tokens_usuario_idx ON refresh_tokens (usuarioid) WHERE revoked_at IS NULL;
CREATE INDEX refresh_tokens_family_idx ON refresh_tokens (family_id);

-- ---------------------------------------------------------------------------
-- Auditoría de seguridad
-- ---------------------------------------------------------------------------
CREATE TABLE audit_log (
    id bigserial PRIMARY KEY,
    evento varchar(50) NOT NULL,
    usuarioid integer REFERENCES usuarios(usuarioid) ON DELETE SET NULL,
    actor_id integer REFERENCES usuarios(usuarioid) ON DELETE SET NULL,
    ip varchar(64),
    detalles jsonb NOT NULL DEFAULT '{}'::jsonb,
    fecha timestamp without time zone NOT NULL DEFAULT now()
);
CREATE INDEX audit_log_usuario_idx ON audit_log (usuarioid, fecha DESC);
CREATE INDEX audit_log_fecha_idx ON audit_log (fecha);

-- ---------------------------------------------------------------------------
-- Códigos de verificación: hash en lugar de código en claro, intentos y
-- token de un solo uso. Los códigos son efímeros: se vacía la tabla.
-- ---------------------------------------------------------------------------
TRUNCATE codigos_verificacion;
ALTER TABLE codigos_verificacion
    DROP COLUMN codigo,
    ADD COLUMN codigo_hash char(64),
    ADD COLUMN intentos integer NOT NULL DEFAULT 0,
    ADD COLUMN created_at timestamp without time zone NOT NULL DEFAULT now(),
    ADD COLUMN token_hash char(64),
    ADD COLUMN token_expira timestamp without time zone,
    ADD CONSTRAINT codigos_tipo_check CHECK (tipo IN ('email_verification', 'password_reset'));
CREATE UNIQUE INDEX codigos_token_hash_key ON codigos_verificacion (token_hash) WHERE token_hash IS NOT NULL;

-- ---------------------------------------------------------------------------
-- peluqueros
-- ---------------------------------------------------------------------------
UPDATE peluqueros SET activo = TRUE WHERE activo IS NULL;
ALTER TABLE peluqueros ALTER COLUMN activo SET DEFAULT TRUE, ALTER COLUMN activo SET NOT NULL;
CREATE INDEX peluqueros_local_idx ON peluqueros (localid) WHERE activo;

-- ---------------------------------------------------------------------------
-- servicios: baja lógica (antes borrar un servicio borraba sus citas)
-- ---------------------------------------------------------------------------
ALTER TABLE servicios ADD COLUMN activo boolean NOT NULL DEFAULT TRUE;
ALTER TABLE servicios
    ADD CONSTRAINT servicios_duracion_check CHECK (duracion BETWEEN 5 AND 480) NOT VALID,
    ADD CONSTRAINT servicios_precio_check CHECK (precio >= 0) NOT VALID;
CREATE INDEX servicios_local_idx ON servicios (localid) WHERE activo;

-- ---------------------------------------------------------------------------
-- citas
-- ---------------------------------------------------------------------------
UPDATE citas c SET fechafin = c.fechainicio + make_interval(mins => s.duracion)
FROM servicios s WHERE c.servicioid = s.servicioid AND c.fechafin IS NULL;
UPDATE citas SET estado = 'Pendiente' WHERE estado IS NULL;

ALTER TABLE citas
    ALTER COLUMN fechafin SET NOT NULL,
    ALTER COLUMN estado SET NOT NULL,
    ADD COLUMN motivo_cancelacion text,
    ADD COLUMN cancelada_por integer REFERENCES usuarios(usuarioid) ON DELETE SET NULL,
    ADD COLUMN created_at timestamp without time zone NOT NULL DEFAULT now(),
    ADD COLUMN updated_at timestamp without time zone NOT NULL DEFAULT now(),
    ADD CONSTRAINT citas_rango_check CHECK (fechafin > fechainicio) NOT VALID;

-- Borrar un servicio o un peluquero ya no destruye el historial de citas.
ALTER TABLE citas
    DROP CONSTRAINT citas_servicioid_fkey,
    ADD CONSTRAINT citas_servicioid_fkey FOREIGN KEY (servicioid) REFERENCES servicios(servicioid) ON DELETE RESTRICT,
    DROP CONSTRAINT citas_peluqueroid_fkey,
    ADD CONSTRAINT citas_peluqueroid_fkey FOREIGN KEY (peluqueroid) REFERENCES peluqueros(peluqueroid) ON DELETE RESTRICT;

-- Las citas históricas que se solapaban (la app antigua no lo impedía) se
-- conservan, pero se excluyen de la restricción marcándolas como legado.
ALTER TABLE citas ADD COLUMN solape_legado boolean NOT NULL DEFAULT FALSE;
UPDATE citas c SET solape_legado = TRUE
WHERE c.estado <> 'Cancelada' AND EXISTS (
    SELECT 1 FROM citas o
    WHERE o.citaid <> c.citaid AND o.peluqueroid = c.peluqueroid AND o.estado <> 'Cancelada'
      AND o.fechainicio < c.fechafin AND c.fechainicio < o.fechafin
);

-- Defensa en profundidad contra la doble reserva: la BD rechaza cualquier
-- solape de citas activas del mismo peluquero, aunque falle la aplicación.
ALTER TABLE citas ADD CONSTRAINT citas_sin_solape
    EXCLUDE USING gist (peluqueroid WITH =, tsrange(fechainicio, fechafin, '[)') WITH &&)
    WHERE (estado <> 'Cancelada' AND NOT solape_legado);

CREATE INDEX citas_peluquero_inicio_idx ON citas (peluqueroid, fechainicio);
CREATE INDEX citas_cliente_inicio_idx ON citas (clienteid, fechainicio);
CREATE INDEX citas_local_inicio_idx ON citas (localid, fechainicio);

-- El trigger nunca estuvo asociado a la tabla; el estado efectivo se calcula en la vista.
DROP FUNCTION IF EXISTS actualizar_estado_cita();

CREATE VIEW v_citas AS
SELECT c.*,
       CASE WHEN c.estado = 'Pendiente' AND c.fechafin <= LOCALTIMESTAMP THEN 'Completada'
            ELSE c.estado END::varchar(20) AS estado_efectivo
FROM citas c;

-- ---------------------------------------------------------------------------
-- actividad_peluquero: integridad referencial (antes sin claves foráneas)
-- ---------------------------------------------------------------------------
DELETE FROM actividad_peluquero a
WHERE NOT EXISTS (SELECT 1 FROM citas c WHERE c.citaid = a.citaid)
   OR NOT EXISTS (SELECT 1 FROM peluqueros p WHERE p.peluqueroid = a.peluqueroid)
   OR NOT EXISTS (SELECT 1 FROM clientes cl WHERE cl.clienteid = a.clienteid);
ALTER TABLE actividad_peluquero
    ADD CONSTRAINT actividad_cita_fkey FOREIGN KEY (citaid) REFERENCES citas(citaid) ON DELETE CASCADE,
    ADD CONSTRAINT actividad_peluquero_fkey FOREIGN KEY (peluqueroid) REFERENCES peluqueros(peluqueroid) ON DELETE CASCADE,
    ADD CONSTRAINT actividad_cliente_fkey FOREIGN KEY (clienteid) REFERENCES clientes(clienteid) ON DELETE CASCADE;
CREATE INDEX actividad_peluquero_fecha_idx ON actividad_peluquero (peluqueroid, fecha DESC);

-- ---------------------------------------------------------------------------
-- horarios y bloqueos
-- ---------------------------------------------------------------------------
DELETE FROM horarios_peluqueros WHERE peluqueroid IS NULL;
DELETE FROM bloqueoshorarios WHERE peluqueroid IS NULL;
ALTER TABLE horarios_peluqueros
    ALTER COLUMN peluqueroid SET NOT NULL,
    ADD CONSTRAINT horarios_rango_check CHECK (horainicio < horafin) NOT VALID;
ALTER TABLE bloqueoshorarios
    ALTER COLUMN peluqueroid SET NOT NULL,
    ADD CONSTRAINT bloqueos_rango_check CHECK (horainicio < horafin) NOT VALID;
CREATE INDEX horarios_peluquero_dia_idx ON horarios_peluqueros (peluqueroid, diasemana);
CREATE INDEX bloqueos_peluquero_fecha_idx ON bloqueoshorarios (peluqueroid, fecha);

-- ---------------------------------------------------------------------------
-- favoritos: el ON CONFLICT DO NOTHING original no tenía clave única en la
-- que apoyarse, así que se acumulaban duplicados.
-- ---------------------------------------------------------------------------
DELETE FROM favoritos_clientes f USING favoritos_clientes g
WHERE f.clienteid = g.clienteid AND f.localid = g.localid AND f.id > g.id;
ALTER TABLE favoritos_clientes ADD CONSTRAINT favoritos_cliente_local_key UNIQUE (clienteid, localid);

-- ---------------------------------------------------------------------------
-- reseñas
-- ---------------------------------------------------------------------------
ALTER TABLE resenas ADD CONSTRAINT resenas_comentario_len CHECK (char_length(comentario) <= 1000) NOT VALID;
CREATE INDEX resenas_local_idx ON resenas (localid, fecharesena DESC) WHERE peluqueroid IS NULL;

-- ---------------------------------------------------------------------------
-- local
-- ---------------------------------------------------------------------------
ALTER TABLE local ADD CONSTRAINT local_horario_check CHECK (horarioapertura < horariocierre) NOT VALID;
