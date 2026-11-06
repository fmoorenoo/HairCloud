-- 0001_baseline: esquema original de HairCloud (extraído del volcado pg_dump 17).
--
-- En una base de datos existente esta migración NO se ejecuta: el comando
-- `flask --app wsgi db-upgrade` detecta las tablas y la marca como aplicada.
-- En una base de datos vacía crea el esquema heredado para que 0002 lo
-- transforme exactamente igual que en producción.

CREATE TABLE public.local (
    localid serial PRIMARY KEY,
    nombre character varying(100) NOT NULL,
    direccion character varying(255) NOT NULL,
    telefono character varying(15) NOT NULL CONSTRAINT local_telefono_key UNIQUE,
    horarioapertura time without time zone NOT NULL,
    horariocierre time without time zone NOT NULL,
    descripcion text,
    localidad character varying(100) NOT NULL,
    imagen_url text
);

CREATE TABLE public.usuarios (
    usuarioid serial PRIMARY KEY,
    nombreusuario character varying(50) NOT NULL CONSTRAINT usuarios_nombreusuario_key UNIQUE,
    "contraseña" text NOT NULL,
    rol character varying(20) DEFAULT 'cliente' NOT NULL,
    localid integer,
    email character varying(100) NOT NULL CONSTRAINT usuarios_email_key UNIQUE,
    CONSTRAINT check_rol CHECK (rol IN ('cliente', 'peluquero', 'semiadmin', 'admin', 'superadmin')),
    CONSTRAINT fk_admin_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE
);

CREATE TABLE public.clientes (
    clienteid serial PRIMARY KEY,
    usuarioid integer NOT NULL CONSTRAINT clientes_usuarioid_key UNIQUE
        REFERENCES public.usuarios(usuarioid) ON DELETE CASCADE,
    nombre character varying(100) NOT NULL,
    telefono character varying(15) CONSTRAINT clientes_telefono_key UNIQUE,
    fecharegistro date DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.peluqueros (
    peluqueroid serial PRIMARY KEY,
    usuarioid integer NOT NULL CONSTRAINT peluqueros_usuarioid_key UNIQUE
        REFERENCES public.usuarios(usuarioid) ON DELETE CASCADE,
    nombre character varying(100) NOT NULL,
    telefono character varying(15) CONSTRAINT peluqueros_telefono_key UNIQUE,
    especialidad character varying(100),
    fechacontratacion date NOT NULL,
    localid integer NOT NULL CONSTRAINT fk_peluquero_local REFERENCES public.local(localid) ON DELETE CASCADE,
    activo boolean
);

CREATE TABLE public.servicios (
    servicioid serial PRIMARY KEY,
    nombre character varying(100) NOT NULL,
    descripcion text,
    duracion integer NOT NULL,
    precio numeric(10,2) NOT NULL,
    localid integer NOT NULL CONSTRAINT fk_servicio_local REFERENCES public.local(localid) ON DELETE CASCADE
);

CREATE TABLE public.citas (
    citaid serial PRIMARY KEY,
    clienteid integer NOT NULL CONSTRAINT citas_clienteid_fkey REFERENCES public.clientes(clienteid) ON DELETE CASCADE,
    peluqueroid integer NOT NULL CONSTRAINT citas_peluqueroid_fkey REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE,
    servicioid integer NOT NULL CONSTRAINT citas_servicioid_fkey REFERENCES public.servicios(servicioid) ON DELETE CASCADE,
    fechainicio timestamp without time zone NOT NULL,
    estado character varying(20) DEFAULT 'Pendiente',
    localid integer NOT NULL CONSTRAINT fk_cita_local REFERENCES public.local(localid) ON DELETE CASCADE,
    fechafin timestamp without time zone,
    CONSTRAINT citas_estado_check CHECK (estado IN ('Pendiente', 'Completada', 'Cancelada', 'No completada'))
);

CREATE TABLE public.actividad_peluquero (
    actividadid serial PRIMARY KEY,
    peluqueroid integer NOT NULL,
    tipo text NOT NULL,
    citaid integer NOT NULL,
    clienteid integer NOT NULL,
    fecha timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT actividad_tipo_check CHECK (tipo IN ('Cancelada', 'Reserva'))
);

CREATE TABLE public.bloqueoshorarios (
    bloqueoid serial PRIMARY KEY,
    peluqueroid integer REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE,
    fecha date NOT NULL,
    horainicio time without time zone NOT NULL,
    horafin time without time zone NOT NULL,
    motivo text
);

CREATE TABLE public.codigos_verificacion (
    id serial PRIMARY KEY,
    email character varying(100) NOT NULL,
    codigo character varying(6) NOT NULL,
    expiracion timestamp without time zone NOT NULL,
    tipo character varying(20) DEFAULT 'email_verification' NOT NULL,
    CONSTRAINT unique_email_tipo UNIQUE (email, tipo)
);

CREATE TABLE public.configuracion (
    configid serial PRIMARY KEY,
    clave character varying(50) NOT NULL UNIQUE,
    valor text NOT NULL
);

CREATE TABLE public.favoritos_clientes (
    id serial PRIMARY KEY,
    clienteid integer NOT NULL REFERENCES public.clientes(clienteid) ON DELETE CASCADE,
    localid integer NOT NULL REFERENCES public.local(localid) ON DELETE CASCADE,
    fecha_agregado timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.historial_citas (
    historial_id serial PRIMARY KEY,
    citaid integer NOT NULL REFERENCES public.citas(citaid) ON DELETE CASCADE,
    usuarioid integer REFERENCES public.usuarios(usuarioid) ON DELETE SET NULL,
    accion character varying(50),
    detalles text,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT historial_citas_accion_check CHECK (accion IN ('Creada', 'Modificada', 'Cancelada', 'Pagada'))
);

CREATE TABLE public.horarios_peluqueros (
    horarioid serial PRIMARY KEY,
    peluqueroid integer CONSTRAINT horarios_peluqueroid_fkey REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE,
    diasemana character varying(10) NOT NULL,
    horainicio time without time zone NOT NULL,
    horafin time without time zone NOT NULL,
    CONSTRAINT horarios_diasemana_check CHECK (diasemana IN ('Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'))
);

CREATE TABLE public.notificaciones (
    notificacionid serial PRIMARY KEY,
    clienteid integer REFERENCES public.clientes(clienteid) ON DELETE CASCADE,
    mensaje text NOT NULL,
    fechaenvio timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado character varying(20) DEFAULT 'Enviada',
    localid integer NOT NULL CONSTRAINT fk_notificacion_local REFERENCES public.local(localid) ON DELETE CASCADE,
    CONSTRAINT notificaciones_estado_check CHECK (estado IN ('Enviada', 'Leída'))
);

CREATE TABLE public.pagos (
    pago_id serial PRIMARY KEY,
    citaid integer NOT NULL REFERENCES public.citas(citaid) ON DELETE CASCADE,
    clienteid integer NOT NULL REFERENCES public.clientes(clienteid) ON DELETE CASCADE,
    peluqueroid integer NOT NULL REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE,
    metodo_pago character varying(20) DEFAULT 'desconocido',
    monto numeric(10,2) NOT NULL,
    estado_pago character varying(20) DEFAULT 'pendiente',
    fecha_pago timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    localid integer NOT NULL CONSTRAINT fk_pago_local REFERENCES public.local(localid) ON DELETE CASCADE,
    CONSTRAINT pagos_estado_pago_check CHECK (estado_pago IN ('pendiente', 'pagado', 'reembolsado')),
    CONSTRAINT pagos_metodo_pago_check CHECK (metodo_pago IN ('efectivo', 'tarjeta', 'transferencia', 'bizum', 'paypal', 'google_pay', 'desconocido'))
);

CREATE TABLE public.reportes (
    reporteid serial PRIMARY KEY,
    fecha_inicio date NOT NULL CONSTRAINT reportes_fecha_key UNIQUE,
    total_clientes_atendidos integer DEFAULT 0,
    total_citas_realizadas integer DEFAULT 0,
    total_servicios_realizados integer DEFAULT 0,
    servicio_mas_solicitado integer REFERENCES public.servicios(servicioid),
    ingresos_totales numeric(10,2) DEFAULT 0.00,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    localid integer NOT NULL CONSTRAINT fk_reporte_local REFERENCES public.local(localid) ON DELETE CASCADE,
    fecha_fin date NOT NULL,
    peluqueroid integer CONSTRAINT fk_peluquero REFERENCES public.peluqueros(peluqueroid) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE public.resenas (
    resenaid serial PRIMARY KEY,
    clienteid integer NOT NULL REFERENCES public.clientes(clienteid) ON DELETE CASCADE,
    peluqueroid integer REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE,
    calificacion integer NOT NULL,
    comentario text,
    fecharesena timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    localid integer NOT NULL CONSTRAINT fk_resena_local REFERENCES public.local(localid) ON DELETE CASCADE,
    CONSTRAINT resenas_calificacion_check CHECK (calificacion >= 1 AND calificacion <= 5)
);

CREATE FUNCTION public.actualizar_estado_cita() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.fechafin IS NOT NULL AND NEW.fechafin < NOW() THEN
        NEW.estado := 'Completada';
    END IF;
    RETURN NEW;
END;
$$;
