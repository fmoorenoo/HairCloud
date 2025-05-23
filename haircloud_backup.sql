PGDMP      '                }        	   HairCloud    17.2    17.1 �    �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                           false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                           false            �           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                           false            �           1262    17167 	   HairCloud    DATABASE     ~   CREATE DATABASE "HairCloud" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Spanish_Spain.1252';
    DROP DATABASE "HairCloud";
                     postgres    false            �            1255    17564    actualizar_estado_cita()    FUNCTION     �   CREATE FUNCTION public.actualizar_estado_cita() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.fechafin IS NOT NULL AND NEW.fechafin < NOW() THEN
        NEW.estado := 'Completada';
    END IF;
    RETURN NEW;
END;
$$;
 /   DROP FUNCTION public.actualizar_estado_cita();
       public               postgres    false            �            1259    17574    actividad_peluquero    TABLE     i  CREATE TABLE public.actividad_peluquero (
    actividadid integer NOT NULL,
    peluqueroid integer NOT NULL,
    tipo text NOT NULL,
    citaid integer NOT NULL,
    clienteid integer NOT NULL,
    fecha timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT actividad_tipo_check CHECK ((tipo = ANY (ARRAY['Cancelada'::text, 'Reserva'::text])))
);
 '   DROP TABLE public.actividad_peluquero;
       public         heap r       postgres    false            �            1259    17573 #   actividad_peluquero_actividadid_seq    SEQUENCE     �   CREATE SEQUENCE public.actividad_peluquero_actividadid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 :   DROP SEQUENCE public.actividad_peluquero_actividadid_seq;
       public               postgres    false    250            �           0    0 #   actividad_peluquero_actividadid_seq    SEQUENCE OWNED BY     k   ALTER SEQUENCE public.actividad_peluquero_actividadid_seq OWNED BY public.actividad_peluquero.actividadid;
          public               postgres    false    249            �            1259    17235    bloqueoshorarios    TABLE     �   CREATE TABLE public.bloqueoshorarios (
    bloqueoid integer NOT NULL,
    peluqueroid integer,
    fecha date NOT NULL,
    horainicio time without time zone NOT NULL,
    horafin time without time zone NOT NULL,
    motivo text
);
 $   DROP TABLE public.bloqueoshorarios;
       public         heap r       postgres    false            �            1259    17234    bloqueoshorarios_bloqueoid_seq    SEQUENCE     �   CREATE SEQUENCE public.bloqueoshorarios_bloqueoid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 5   DROP SEQUENCE public.bloqueoshorarios_bloqueoid_seq;
       public               postgres    false    228            �           0    0    bloqueoshorarios_bloqueoid_seq    SEQUENCE OWNED BY     a   ALTER SEQUENCE public.bloqueoshorarios_bloqueoid_seq OWNED BY public.bloqueoshorarios.bloqueoid;
          public               postgres    false    227            �            1259    17249    citas    TABLE        CREATE TABLE public.citas (
    citaid integer NOT NULL,
    clienteid integer NOT NULL,
    peluqueroid integer NOT NULL,
    servicioid integer NOT NULL,
    fechainicio timestamp without time zone NOT NULL,
    estado character varying(20) DEFAULT 'Pendiente'::character varying,
    localid integer NOT NULL,
    fechafin timestamp without time zone,
    CONSTRAINT citas_estado_check CHECK (((estado)::text = ANY (ARRAY['Pendiente'::text, 'Completada'::text, 'Cancelada'::text, 'No completada'::text])))
);
    DROP TABLE public.citas;
       public         heap r       postgres    false            �            1259    17248    citas_citaid_seq    SEQUENCE     �   CREATE SEQUENCE public.citas_citaid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 '   DROP SEQUENCE public.citas_citaid_seq;
       public               postgres    false    230            �           0    0    citas_citaid_seq    SEQUENCE OWNED BY     E   ALTER SEQUENCE public.citas_citaid_seq OWNED BY public.citas.citaid;
          public               postgres    false    229            �            1259    17180    clientes    TABLE     �   CREATE TABLE public.clientes (
    clienteid integer NOT NULL,
    usuarioid integer NOT NULL,
    nombre character varying(100) NOT NULL,
    telefono character varying(15),
    fecharegistro date DEFAULT CURRENT_TIMESTAMP NOT NULL
);
    DROP TABLE public.clientes;
       public         heap r       postgres    false            �            1259    17179    clientes_clienteid_seq    SEQUENCE     �   CREATE SEQUENCE public.clientes_clienteid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.clientes_clienteid_seq;
       public               postgres    false    220            �           0    0    clientes_clienteid_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE public.clientes_clienteid_seq OWNED BY public.clientes.clienteid;
          public               postgres    false    219            �            1259    17540    codigos_verificacion    TABLE     )  CREATE TABLE public.codigos_verificacion (
    id integer NOT NULL,
    email character varying(100) NOT NULL,
    codigo character varying(6) NOT NULL,
    expiracion timestamp without time zone NOT NULL,
    tipo character varying(20) DEFAULT 'email_verification'::character varying NOT NULL
);
 (   DROP TABLE public.codigos_verificacion;
       public         heap r       postgres    false            �            1259    17539 $   codigos_recuperar_contrasenas_id_seq    SEQUENCE     �   CREATE SEQUENCE public.codigos_recuperar_contrasenas_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 ;   DROP SEQUENCE public.codigos_recuperar_contrasenas_id_seq;
       public               postgres    false    248            �           0    0 $   codigos_recuperar_contrasenas_id_seq    SEQUENCE OWNED BY     d   ALTER SEQUENCE public.codigos_recuperar_contrasenas_id_seq OWNED BY public.codigos_verificacion.id;
          public               postgres    false    247            �            1259    17301    configuracion    TABLE     �   CREATE TABLE public.configuracion (
    configid integer NOT NULL,
    clave character varying(50) NOT NULL,
    valor text NOT NULL
);
 !   DROP TABLE public.configuracion;
       public         heap r       postgres    false            �            1259    17300    configuracion_configid_seq    SEQUENCE     �   CREATE SEQUENCE public.configuracion_configid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 1   DROP SEQUENCE public.configuracion_configid_seq;
       public               postgres    false    234            �           0    0    configuracion_configid_seq    SEQUENCE OWNED BY     Y   ALTER SEQUENCE public.configuracion_configid_seq OWNED BY public.configuracion.configid;
          public               postgres    false    233            �            1259    17467    favoritos_clientes    TABLE     �   CREATE TABLE public.favoritos_clientes (
    id integer NOT NULL,
    clienteid integer NOT NULL,
    localid integer NOT NULL,
    fecha_agregado timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
 &   DROP TABLE public.favoritos_clientes;
       public         heap r       postgres    false            �            1259    17466    favoritos_clientes_id_seq    SEQUENCE     �   CREATE SEQUENCE public.favoritos_clientes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 0   DROP SEQUENCE public.favoritos_clientes_id_seq;
       public               postgres    false    246            �           0    0    favoritos_clientes_id_seq    SEQUENCE OWNED BY     W   ALTER SEQUENCE public.favoritos_clientes_id_seq OWNED BY public.favoritos_clientes.id;
          public               postgres    false    245            �            1259    17418    historial_citas    TABLE     �  CREATE TABLE public.historial_citas (
    historial_id integer NOT NULL,
    citaid integer NOT NULL,
    usuarioid integer,
    accion character varying(50),
    detalles text,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT historial_citas_accion_check CHECK (((accion)::text = ANY ((ARRAY['Creada'::character varying, 'Modificada'::character varying, 'Cancelada'::character varying, 'Pagada'::character varying])::text[])))
);
 #   DROP TABLE public.historial_citas;
       public         heap r       postgres    false            �            1259    17417     historial_citas_historial_id_seq    SEQUENCE     �   CREATE SEQUENCE public.historial_citas_historial_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 7   DROP SEQUENCE public.historial_citas_historial_id_seq;
       public               postgres    false    244            �           0    0     historial_citas_historial_id_seq    SEQUENCE OWNED BY     e   ALTER SEQUENCE public.historial_citas_historial_id_seq OWNED BY public.historial_citas.historial_id;
          public               postgres    false    243            �            1259    17222    horarios_peluqueros    TABLE       CREATE TABLE public.horarios_peluqueros (
    horarioid integer NOT NULL,
    peluqueroid integer,
    diasemana character varying(10) NOT NULL,
    horainicio time without time zone NOT NULL,
    horafin time without time zone NOT NULL,
    CONSTRAINT horarios_diasemana_check CHECK (((diasemana)::text = ANY ((ARRAY['Lunes'::character varying, 'Martes'::character varying, 'Miércoles'::character varying, 'Jueves'::character varying, 'Viernes'::character varying, 'Sábado'::character varying, 'Domingo'::character varying])::text[])))
);
 '   DROP TABLE public.horarios_peluqueros;
       public         heap r       postgres    false            �            1259    17221    horarios_horarioid_seq    SEQUENCE     �   CREATE SEQUENCE public.horarios_horarioid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.horarios_horarioid_seq;
       public               postgres    false    226            �           0    0    horarios_horarioid_seq    SEQUENCE OWNED BY     \   ALTER SEQUENCE public.horarios_horarioid_seq OWNED BY public.horarios_peluqueros.horarioid;
          public               postgres    false    225            �            1259    17333    local    TABLE     �  CREATE TABLE public.local (
    localid integer NOT NULL,
    nombre character varying(100) NOT NULL,
    direccion character varying(255) NOT NULL,
    telefono character varying(15) NOT NULL,
    horarioapertura time without time zone NOT NULL,
    horariocierre time without time zone NOT NULL,
    descripcion text,
    localidad character varying(100) NOT NULL,
    imagen_url text
);
    DROP TABLE public.local;
       public         heap r       postgres    false            �            1259    17332    local_localid_seq    SEQUENCE     �   CREATE SEQUENCE public.local_localid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.local_localid_seq;
       public               postgres    false    238            �           0    0    local_localid_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.local_localid_seq OWNED BY public.local.localid;
          public               postgres    false    237            �            1259    17284    notificaciones    TABLE     �  CREATE TABLE public.notificaciones (
    notificacionid integer NOT NULL,
    clienteid integer,
    mensaje text NOT NULL,
    fechaenvio timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado character varying(20) DEFAULT 'Enviada'::character varying,
    localid integer NOT NULL,
    CONSTRAINT notificaciones_estado_check CHECK (((estado)::text = ANY ((ARRAY['Enviada'::character varying, 'Leída'::character varying])::text[])))
);
 "   DROP TABLE public.notificaciones;
       public         heap r       postgres    false            �            1259    17283 !   notificaciones_notificacionid_seq    SEQUENCE     �   CREATE SEQUENCE public.notificaciones_notificacionid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 8   DROP SEQUENCE public.notificaciones_notificacionid_seq;
       public               postgres    false    232            �           0    0 !   notificaciones_notificacionid_seq    SEQUENCE OWNED BY     g   ALTER SEQUENCE public.notificaciones_notificacionid_seq OWNED BY public.notificaciones.notificacionid;
          public               postgres    false    231            �            1259    17388    pagos    TABLE     �  CREATE TABLE public.pagos (
    pago_id integer NOT NULL,
    citaid integer NOT NULL,
    clienteid integer NOT NULL,
    peluqueroid integer NOT NULL,
    metodo_pago character varying(20) DEFAULT 'desconocido'::character varying,
    monto numeric(10,2) NOT NULL,
    estado_pago character varying(20) DEFAULT 'pendiente'::character varying,
    fecha_pago timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    localid integer NOT NULL,
    CONSTRAINT pagos_estado_pago_check CHECK (((estado_pago)::text = ANY ((ARRAY['pendiente'::character varying, 'pagado'::character varying, 'reembolsado'::character varying])::text[]))),
    CONSTRAINT pagos_metodo_pago_check CHECK (((metodo_pago)::text = ANY ((ARRAY['efectivo'::character varying, 'tarjeta'::character varying, 'transferencia'::character varying, 'bizum'::character varying, 'paypal'::character varying, 'google_pay'::character varying, 'desconocido'::character varying])::text[])))
);
    DROP TABLE public.pagos;
       public         heap r       postgres    false            �            1259    17387    pagos_pago_id_seq    SEQUENCE     �   CREATE SEQUENCE public.pagos_pago_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.pagos_pago_id_seq;
       public               postgres    false    242            �           0    0    pagos_pago_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.pagos_pago_id_seq OWNED BY public.pagos.pago_id;
          public               postgres    false    241            �            1259    17197 
   peluqueros    TABLE     5  CREATE TABLE public.peluqueros (
    peluqueroid integer NOT NULL,
    usuarioid integer NOT NULL,
    nombre character varying(100) NOT NULL,
    telefono character varying(15),
    especialidad character varying(100),
    fechacontratacion date NOT NULL,
    localid integer NOT NULL,
    activo boolean
);
    DROP TABLE public.peluqueros;
       public         heap r       postgres    false            �            1259    17196    peluqueros_peluqueroid_seq    SEQUENCE     �   CREATE SEQUENCE public.peluqueros_peluqueroid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 1   DROP SEQUENCE public.peluqueros_peluqueroid_seq;
       public               postgres    false    222            �           0    0    peluqueros_peluqueroid_seq    SEQUENCE OWNED BY     Y   ALTER SEQUENCE public.peluqueros_peluqueroid_seq OWNED BY public.peluqueros.peluqueroid;
          public               postgres    false    221            �            1259    17344    reportes    TABLE     �  CREATE TABLE public.reportes (
    reporteid integer NOT NULL,
    fecha_inicio date NOT NULL,
    total_clientes_atendidos integer DEFAULT 0,
    total_citas_realizadas integer DEFAULT 0,
    total_servicios_realizados integer DEFAULT 0,
    servicio_mas_solicitado integer,
    ingresos_totales numeric(10,2) DEFAULT 0.00,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    localid integer NOT NULL,
    fecha_fin date NOT NULL,
    peluqueroid integer
);
    DROP TABLE public.reportes;
       public         heap r       postgres    false            �            1259    17343    reportes_reporteid_seq    SEQUENCE     �   CREATE SEQUENCE public.reportes_reporteid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.reportes_reporteid_seq;
       public               postgres    false    240            �           0    0    reportes_reporteid_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE public.reportes_reporteid_seq OWNED BY public.reportes.reporteid;
          public               postgres    false    239            �            1259    17312    resenas    TABLE       CREATE TABLE public.resenas (
    resenaid integer NOT NULL,
    clienteid integer NOT NULL,
    peluqueroid integer,
    calificacion integer NOT NULL,
    comentario text,
    fecharesena timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    localid integer NOT NULL,
    CONSTRAINT resenas_calificacion_check CHECK (((calificacion >= 1) AND (calificacion <= 5)))
);
    DROP TABLE public.resenas;
       public         heap r       postgres    false            �            1259    17311    resenas_resenaid_seq    SEQUENCE     �   CREATE SEQUENCE public.resenas_resenaid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 +   DROP SEQUENCE public.resenas_resenaid_seq;
       public               postgres    false    236            �           0    0    resenas_resenaid_seq    SEQUENCE OWNED BY     M   ALTER SEQUENCE public.resenas_resenaid_seq OWNED BY public.resenas.resenaid;
          public               postgres    false    235            �            1259    17213 	   servicios    TABLE     �   CREATE TABLE public.servicios (
    servicioid integer NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    duracion integer NOT NULL,
    precio numeric(10,2) NOT NULL,
    localid integer NOT NULL
);
    DROP TABLE public.servicios;
       public         heap r       postgres    false            �            1259    17212    servicios_servicioid_seq    SEQUENCE     �   CREATE SEQUENCE public.servicios_servicioid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 /   DROP SEQUENCE public.servicios_servicioid_seq;
       public               postgres    false    224            �           0    0    servicios_servicioid_seq    SEQUENCE OWNED BY     U   ALTER SEQUENCE public.servicios_servicioid_seq OWNED BY public.servicios.servicioid;
          public               postgres    false    223            �            1259    17169    usuarios    TABLE       CREATE TABLE public.usuarios (
    usuarioid integer NOT NULL,
    nombreusuario character varying(50) NOT NULL,
    "contraseña" text NOT NULL,
    rol character varying(20) DEFAULT 'cliente'::character varying NOT NULL,
    localid integer,
    email character varying(100) NOT NULL,
    CONSTRAINT check_rol CHECK (((rol)::text = ANY ((ARRAY['cliente'::character varying, 'peluquero'::character varying, 'semiadmin'::character varying, 'admin'::character varying, 'superadmin'::character varying])::text[])))
);
    DROP TABLE public.usuarios;
       public         heap r       postgres    false            �            1259    17168    usuarios_usuarioid_seq    SEQUENCE     �   CREATE SEQUENCE public.usuarios_usuarioid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.usuarios_usuarioid_seq;
       public               postgres    false    218            �           0    0    usuarios_usuarioid_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE public.usuarios_usuarioid_seq OWNED BY public.usuarios.usuarioid;
          public               postgres    false    217            �           2604    17577    actividad_peluquero actividadid    DEFAULT     �   ALTER TABLE ONLY public.actividad_peluquero ALTER COLUMN actividadid SET DEFAULT nextval('public.actividad_peluquero_actividadid_seq'::regclass);
 N   ALTER TABLE public.actividad_peluquero ALTER COLUMN actividadid DROP DEFAULT;
       public               postgres    false    249    250    250            �           2604    17238    bloqueoshorarios bloqueoid    DEFAULT     �   ALTER TABLE ONLY public.bloqueoshorarios ALTER COLUMN bloqueoid SET DEFAULT nextval('public.bloqueoshorarios_bloqueoid_seq'::regclass);
 I   ALTER TABLE public.bloqueoshorarios ALTER COLUMN bloqueoid DROP DEFAULT;
       public               postgres    false    228    227    228            �           2604    17252    citas citaid    DEFAULT     l   ALTER TABLE ONLY public.citas ALTER COLUMN citaid SET DEFAULT nextval('public.citas_citaid_seq'::regclass);
 ;   ALTER TABLE public.citas ALTER COLUMN citaid DROP DEFAULT;
       public               postgres    false    229    230    230            �           2604    17183    clientes clienteid    DEFAULT     x   ALTER TABLE ONLY public.clientes ALTER COLUMN clienteid SET DEFAULT nextval('public.clientes_clienteid_seq'::regclass);
 A   ALTER TABLE public.clientes ALTER COLUMN clienteid DROP DEFAULT;
       public               postgres    false    219    220    220            �           2604    17543    codigos_verificacion id    DEFAULT     �   ALTER TABLE ONLY public.codigos_verificacion ALTER COLUMN id SET DEFAULT nextval('public.codigos_recuperar_contrasenas_id_seq'::regclass);
 F   ALTER TABLE public.codigos_verificacion ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    248    247    248            �           2604    17304    configuracion configid    DEFAULT     �   ALTER TABLE ONLY public.configuracion ALTER COLUMN configid SET DEFAULT nextval('public.configuracion_configid_seq'::regclass);
 E   ALTER TABLE public.configuracion ALTER COLUMN configid DROP DEFAULT;
       public               postgres    false    233    234    234            �           2604    17470    favoritos_clientes id    DEFAULT     ~   ALTER TABLE ONLY public.favoritos_clientes ALTER COLUMN id SET DEFAULT nextval('public.favoritos_clientes_id_seq'::regclass);
 D   ALTER TABLE public.favoritos_clientes ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    245    246    246            �           2604    17421    historial_citas historial_id    DEFAULT     �   ALTER TABLE ONLY public.historial_citas ALTER COLUMN historial_id SET DEFAULT nextval('public.historial_citas_historial_id_seq'::regclass);
 K   ALTER TABLE public.historial_citas ALTER COLUMN historial_id DROP DEFAULT;
       public               postgres    false    244    243    244            �           2604    17225    horarios_peluqueros horarioid    DEFAULT     �   ALTER TABLE ONLY public.horarios_peluqueros ALTER COLUMN horarioid SET DEFAULT nextval('public.horarios_horarioid_seq'::regclass);
 L   ALTER TABLE public.horarios_peluqueros ALTER COLUMN horarioid DROP DEFAULT;
       public               postgres    false    225    226    226            �           2604    17336    local localid    DEFAULT     n   ALTER TABLE ONLY public.local ALTER COLUMN localid SET DEFAULT nextval('public.local_localid_seq'::regclass);
 <   ALTER TABLE public.local ALTER COLUMN localid DROP DEFAULT;
       public               postgres    false    237    238    238            �           2604    17287    notificaciones notificacionid    DEFAULT     �   ALTER TABLE ONLY public.notificaciones ALTER COLUMN notificacionid SET DEFAULT nextval('public.notificaciones_notificacionid_seq'::regclass);
 L   ALTER TABLE public.notificaciones ALTER COLUMN notificacionid DROP DEFAULT;
       public               postgres    false    231    232    232            �           2604    17391    pagos pago_id    DEFAULT     n   ALTER TABLE ONLY public.pagos ALTER COLUMN pago_id SET DEFAULT nextval('public.pagos_pago_id_seq'::regclass);
 <   ALTER TABLE public.pagos ALTER COLUMN pago_id DROP DEFAULT;
       public               postgres    false    242    241    242            �           2604    17200    peluqueros peluqueroid    DEFAULT     �   ALTER TABLE ONLY public.peluqueros ALTER COLUMN peluqueroid SET DEFAULT nextval('public.peluqueros_peluqueroid_seq'::regclass);
 E   ALTER TABLE public.peluqueros ALTER COLUMN peluqueroid DROP DEFAULT;
       public               postgres    false    221    222    222            �           2604    17347    reportes reporteid    DEFAULT     x   ALTER TABLE ONLY public.reportes ALTER COLUMN reporteid SET DEFAULT nextval('public.reportes_reporteid_seq'::regclass);
 A   ALTER TABLE public.reportes ALTER COLUMN reporteid DROP DEFAULT;
       public               postgres    false    239    240    240            �           2604    17315    resenas resenaid    DEFAULT     t   ALTER TABLE ONLY public.resenas ALTER COLUMN resenaid SET DEFAULT nextval('public.resenas_resenaid_seq'::regclass);
 ?   ALTER TABLE public.resenas ALTER COLUMN resenaid DROP DEFAULT;
       public               postgres    false    236    235    236            �           2604    17216    servicios servicioid    DEFAULT     |   ALTER TABLE ONLY public.servicios ALTER COLUMN servicioid SET DEFAULT nextval('public.servicios_servicioid_seq'::regclass);
 C   ALTER TABLE public.servicios ALTER COLUMN servicioid DROP DEFAULT;
       public               postgres    false    224    223    224            �           2604    17172    usuarios usuarioid    DEFAULT     x   ALTER TABLE ONLY public.usuarios ALTER COLUMN usuarioid SET DEFAULT nextval('public.usuarios_usuarioid_seq'::regclass);
 A   ALTER TABLE public.usuarios ALTER COLUMN usuarioid DROP DEFAULT;
       public               postgres    false    217    218    218            �          0    17574    actividad_peluquero 
   TABLE DATA           g   COPY public.actividad_peluquero (actividadid, peluqueroid, tipo, citaid, clienteid, fecha) FROM stdin;
    public               postgres    false    250   ��       �          0    17235    bloqueoshorarios 
   TABLE DATA           f   COPY public.bloqueoshorarios (bloqueoid, peluqueroid, fecha, horainicio, horafin, motivo) FROM stdin;
    public               postgres    false    228   y�       �          0    17249    citas 
   TABLE DATA           s   COPY public.citas (citaid, clienteid, peluqueroid, servicioid, fechainicio, estado, localid, fechafin) FROM stdin;
    public               postgres    false    230   ��       �          0    17180    clientes 
   TABLE DATA           Y   COPY public.clientes (clienteid, usuarioid, nombre, telefono, fecharegistro) FROM stdin;
    public               postgres    false    220   �       �          0    17540    codigos_verificacion 
   TABLE DATA           S   COPY public.codigos_verificacion (id, email, codigo, expiracion, tipo) FROM stdin;
    public               postgres    false    248   ��       �          0    17301    configuracion 
   TABLE DATA           ?   COPY public.configuracion (configid, clave, valor) FROM stdin;
    public               postgres    false    234   C�       �          0    17467    favoritos_clientes 
   TABLE DATA           T   COPY public.favoritos_clientes (id, clienteid, localid, fecha_agregado) FROM stdin;
    public               postgres    false    246   `�       �          0    17418    historial_citas 
   TABLE DATA           c   COPY public.historial_citas (historial_id, citaid, usuarioid, accion, detalles, fecha) FROM stdin;
    public               postgres    false    244   ��       �          0    17222    horarios_peluqueros 
   TABLE DATA           e   COPY public.horarios_peluqueros (horarioid, peluqueroid, diasemana, horainicio, horafin) FROM stdin;
    public               postgres    false    226   ��       �          0    17333    local 
   TABLE DATA           �   COPY public.local (localid, nombre, direccion, telefono, horarioapertura, horariocierre, descripcion, localidad, imagen_url) FROM stdin;
    public               postgres    false    238   ��       �          0    17284    notificaciones 
   TABLE DATA           i   COPY public.notificaciones (notificacionid, clienteid, mensaje, fechaenvio, estado, localid) FROM stdin;
    public               postgres    false    232   ��       �          0    17388    pagos 
   TABLE DATA           ~   COPY public.pagos (pago_id, citaid, clienteid, peluqueroid, metodo_pago, monto, estado_pago, fecha_pago, localid) FROM stdin;
    public               postgres    false    242   ��       �          0    17197 
   peluqueros 
   TABLE DATA           �   COPY public.peluqueros (peluqueroid, usuarioid, nombre, telefono, especialidad, fechacontratacion, localid, activo) FROM stdin;
    public               postgres    false    222   ��       �          0    17344    reportes 
   TABLE DATA           �   COPY public.reportes (reporteid, fecha_inicio, total_clientes_atendidos, total_citas_realizadas, total_servicios_realizados, servicio_mas_solicitado, ingresos_totales, created_at, localid, fecha_fin, peluqueroid) FROM stdin;
    public               postgres    false    240   ��       �          0    17312    resenas 
   TABLE DATA           s   COPY public.resenas (resenaid, clienteid, peluqueroid, calificacion, comentario, fecharesena, localid) FROM stdin;
    public               postgres    false    236   �       �          0    17213 	   servicios 
   TABLE DATA           _   COPY public.servicios (servicioid, nombre, descripcion, duracion, precio, localid) FROM stdin;
    public               postgres    false    224   G�       �          0    17169    usuarios 
   TABLE DATA           `   COPY public.usuarios (usuarioid, nombreusuario, "contraseña", rol, localid, email) FROM stdin;
    public               postgres    false    218   [�       �           0    0 #   actividad_peluquero_actividadid_seq    SEQUENCE SET     Q   SELECT pg_catalog.setval('public.actividad_peluquero_actividadid_seq', 6, true);
          public               postgres    false    249            �           0    0    bloqueoshorarios_bloqueoid_seq    SEQUENCE SET     M   SELECT pg_catalog.setval('public.bloqueoshorarios_bloqueoid_seq', 1, false);
          public               postgres    false    227            �           0    0    citas_citaid_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.citas_citaid_seq', 474, true);
          public               postgres    false    229            �           0    0    clientes_clienteid_seq    SEQUENCE SET     E   SELECT pg_catalog.setval('public.clientes_clienteid_seq', 25, true);
          public               postgres    false    219            �           0    0 $   codigos_recuperar_contrasenas_id_seq    SEQUENCE SET     T   SELECT pg_catalog.setval('public.codigos_recuperar_contrasenas_id_seq', 156, true);
          public               postgres    false    247            �           0    0    configuracion_configid_seq    SEQUENCE SET     I   SELECT pg_catalog.setval('public.configuracion_configid_seq', 1, false);
          public               postgres    false    233            �           0    0    favoritos_clientes_id_seq    SEQUENCE SET     H   SELECT pg_catalog.setval('public.favoritos_clientes_id_seq', 66, true);
          public               postgres    false    245            �           0    0     historial_citas_historial_id_seq    SEQUENCE SET     O   SELECT pg_catalog.setval('public.historial_citas_historial_id_seq', 1, false);
          public               postgres    false    243            �           0    0    horarios_horarioid_seq    SEQUENCE SET     F   SELECT pg_catalog.setval('public.horarios_horarioid_seq', 110, true);
          public               postgres    false    225            �           0    0    local_localid_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.local_localid_seq', 16, true);
          public               postgres    false    237            �           0    0 !   notificaciones_notificacionid_seq    SEQUENCE SET     P   SELECT pg_catalog.setval('public.notificaciones_notificacionid_seq', 1, false);
          public               postgres    false    231            �           0    0    pagos_pago_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.pagos_pago_id_seq', 1, false);
          public               postgres    false    241            �           0    0    peluqueros_peluqueroid_seq    SEQUENCE SET     I   SELECT pg_catalog.setval('public.peluqueros_peluqueroid_seq', 12, true);
          public               postgres    false    221            �           0    0    reportes_reporteid_seq    SEQUENCE SET     E   SELECT pg_catalog.setval('public.reportes_reporteid_seq', 1, false);
          public               postgres    false    239            �           0    0    resenas_resenaid_seq    SEQUENCE SET     C   SELECT pg_catalog.setval('public.resenas_resenaid_seq', 47, true);
          public               postgres    false    235            �           0    0    servicios_servicioid_seq    SEQUENCE SET     G   SELECT pg_catalog.setval('public.servicios_servicioid_seq', 41, true);
          public               postgres    false    223            �           0    0    usuarios_usuarioid_seq    SEQUENCE SET     E   SELECT pg_catalog.setval('public.usuarios_usuarioid_seq', 39, true);
          public               postgres    false    217            	           2606    17582 ,   actividad_peluquero actividad_peluquero_pkey 
   CONSTRAINT     s   ALTER TABLE ONLY public.actividad_peluquero
    ADD CONSTRAINT actividad_peluquero_pkey PRIMARY KEY (actividadid);
 V   ALTER TABLE ONLY public.actividad_peluquero DROP CONSTRAINT actividad_peluquero_pkey;
       public                 postgres    false    250            �           2606    17242 &   bloqueoshorarios bloqueoshorarios_pkey 
   CONSTRAINT     k   ALTER TABLE ONLY public.bloqueoshorarios
    ADD CONSTRAINT bloqueoshorarios_pkey PRIMARY KEY (bloqueoid);
 P   ALTER TABLE ONLY public.bloqueoshorarios DROP CONSTRAINT bloqueoshorarios_pkey;
       public                 postgres    false    228            �           2606    17256    citas citas_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.citas
    ADD CONSTRAINT citas_pkey PRIMARY KEY (citaid);
 :   ALTER TABLE ONLY public.citas DROP CONSTRAINT citas_pkey;
       public                 postgres    false    230            �           2606    17186    clientes clientes_pkey 
   CONSTRAINT     [   ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT clientes_pkey PRIMARY KEY (clienteid);
 @   ALTER TABLE ONLY public.clientes DROP CONSTRAINT clientes_pkey;
       public                 postgres    false    220            �           2606    17190    clientes clientes_telefono_key 
   CONSTRAINT     ]   ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT clientes_telefono_key UNIQUE (telefono);
 H   ALTER TABLE ONLY public.clientes DROP CONSTRAINT clientes_telefono_key;
       public                 postgres    false    220            �           2606    17188    clientes clientes_usuarioid_key 
   CONSTRAINT     _   ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT clientes_usuarioid_key UNIQUE (usuarioid);
 I   ALTER TABLE ONLY public.clientes DROP CONSTRAINT clientes_usuarioid_key;
       public                 postgres    false    220                       2606    17545 7   codigos_verificacion codigos_recuperar_contrasenas_pkey 
   CONSTRAINT     u   ALTER TABLE ONLY public.codigos_verificacion
    ADD CONSTRAINT codigos_recuperar_contrasenas_pkey PRIMARY KEY (id);
 a   ALTER TABLE ONLY public.codigos_verificacion DROP CONSTRAINT codigos_recuperar_contrasenas_pkey;
       public                 postgres    false    248            �           2606    17310 %   configuracion configuracion_clave_key 
   CONSTRAINT     a   ALTER TABLE ONLY public.configuracion
    ADD CONSTRAINT configuracion_clave_key UNIQUE (clave);
 O   ALTER TABLE ONLY public.configuracion DROP CONSTRAINT configuracion_clave_key;
       public                 postgres    false    234            �           2606    17308     configuracion configuracion_pkey 
   CONSTRAINT     d   ALTER TABLE ONLY public.configuracion
    ADD CONSTRAINT configuracion_pkey PRIMARY KEY (configid);
 J   ALTER TABLE ONLY public.configuracion DROP CONSTRAINT configuracion_pkey;
       public                 postgres    false    234                       2606    17473 *   favoritos_clientes favoritos_clientes_pkey 
   CONSTRAINT     h   ALTER TABLE ONLY public.favoritos_clientes
    ADD CONSTRAINT favoritos_clientes_pkey PRIMARY KEY (id);
 T   ALTER TABLE ONLY public.favoritos_clientes DROP CONSTRAINT favoritos_clientes_pkey;
       public                 postgres    false    246                       2606    17427 $   historial_citas historial_citas_pkey 
   CONSTRAINT     l   ALTER TABLE ONLY public.historial_citas
    ADD CONSTRAINT historial_citas_pkey PRIMARY KEY (historial_id);
 N   ALTER TABLE ONLY public.historial_citas DROP CONSTRAINT historial_citas_pkey;
       public                 postgres    false    244            �           2606    17228 !   horarios_peluqueros horarios_pkey 
   CONSTRAINT     f   ALTER TABLE ONLY public.horarios_peluqueros
    ADD CONSTRAINT horarios_pkey PRIMARY KEY (horarioid);
 K   ALTER TABLE ONLY public.horarios_peluqueros DROP CONSTRAINT horarios_pkey;
       public                 postgres    false    226            �           2606    17340    local local_pkey 
   CONSTRAINT     S   ALTER TABLE ONLY public.local
    ADD CONSTRAINT local_pkey PRIMARY KEY (localid);
 :   ALTER TABLE ONLY public.local DROP CONSTRAINT local_pkey;
       public                 postgres    false    238            �           2606    17342    local local_telefono_key 
   CONSTRAINT     W   ALTER TABLE ONLY public.local
    ADD CONSTRAINT local_telefono_key UNIQUE (telefono);
 B   ALTER TABLE ONLY public.local DROP CONSTRAINT local_telefono_key;
       public                 postgres    false    238            �           2606    17294 "   notificaciones notificaciones_pkey 
   CONSTRAINT     l   ALTER TABLE ONLY public.notificaciones
    ADD CONSTRAINT notificaciones_pkey PRIMARY KEY (notificacionid);
 L   ALTER TABLE ONLY public.notificaciones DROP CONSTRAINT notificaciones_pkey;
       public                 postgres    false    232            �           2606    17398    pagos pagos_pkey 
   CONSTRAINT     S   ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_pkey PRIMARY KEY (pago_id);
 :   ALTER TABLE ONLY public.pagos DROP CONSTRAINT pagos_pkey;
       public                 postgres    false    242            �           2606    17202    peluqueros peluqueros_pkey 
   CONSTRAINT     a   ALTER TABLE ONLY public.peluqueros
    ADD CONSTRAINT peluqueros_pkey PRIMARY KEY (peluqueroid);
 D   ALTER TABLE ONLY public.peluqueros DROP CONSTRAINT peluqueros_pkey;
       public                 postgres    false    222            �           2606    17206 "   peluqueros peluqueros_telefono_key 
   CONSTRAINT     a   ALTER TABLE ONLY public.peluqueros
    ADD CONSTRAINT peluqueros_telefono_key UNIQUE (telefono);
 L   ALTER TABLE ONLY public.peluqueros DROP CONSTRAINT peluqueros_telefono_key;
       public                 postgres    false    222            �           2606    17204 #   peluqueros peluqueros_usuarioid_key 
   CONSTRAINT     c   ALTER TABLE ONLY public.peluqueros
    ADD CONSTRAINT peluqueros_usuarioid_key UNIQUE (usuarioid);
 M   ALTER TABLE ONLY public.peluqueros DROP CONSTRAINT peluqueros_usuarioid_key;
       public                 postgres    false    222            �           2606    17356    reportes reportes_fecha_key 
   CONSTRAINT     ^   ALTER TABLE ONLY public.reportes
    ADD CONSTRAINT reportes_fecha_key UNIQUE (fecha_inicio);
 E   ALTER TABLE ONLY public.reportes DROP CONSTRAINT reportes_fecha_key;
       public                 postgres    false    240            �           2606    17354    reportes reportes_pkey 
   CONSTRAINT     [   ALTER TABLE ONLY public.reportes
    ADD CONSTRAINT reportes_pkey PRIMARY KEY (reporteid);
 @   ALTER TABLE ONLY public.reportes DROP CONSTRAINT reportes_pkey;
       public                 postgres    false    240            �           2606    17321    resenas resenas_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.resenas
    ADD CONSTRAINT resenas_pkey PRIMARY KEY (resenaid);
 >   ALTER TABLE ONLY public.resenas DROP CONSTRAINT resenas_pkey;
       public                 postgres    false    236            �           2606    17220    servicios servicios_pkey 
   CONSTRAINT     ^   ALTER TABLE ONLY public.servicios
    ADD CONSTRAINT servicios_pkey PRIMARY KEY (servicioid);
 B   ALTER TABLE ONLY public.servicios DROP CONSTRAINT servicios_pkey;
       public                 postgres    false    224                       2606    17552 &   codigos_verificacion unique_email_tipo 
   CONSTRAINT     h   ALTER TABLE ONLY public.codigos_verificacion
    ADD CONSTRAINT unique_email_tipo UNIQUE (email, tipo);
 P   ALTER TABLE ONLY public.codigos_verificacion DROP CONSTRAINT unique_email_tipo;
       public                 postgres    false    248    248            �           2606    17538    usuarios usuarios_email_key 
   CONSTRAINT     W   ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);
 E   ALTER TABLE ONLY public.usuarios DROP CONSTRAINT usuarios_email_key;
       public                 postgres    false    218            �           2606    17178 #   usuarios usuarios_nombreusuario_key 
   CONSTRAINT     g   ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_nombreusuario_key UNIQUE (nombreusuario);
 M   ALTER TABLE ONLY public.usuarios DROP CONSTRAINT usuarios_nombreusuario_key;
       public                 postgres    false    218            �           2606    17176    usuarios usuarios_pkey 
   CONSTRAINT     [   ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (usuarioid);
 @   ALTER TABLE ONLY public.usuarios DROP CONSTRAINT usuarios_pkey;
       public                 postgres    false    218                       2606    17243 2   bloqueoshorarios bloqueoshorarios_peluqueroid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.bloqueoshorarios
    ADD CONSTRAINT bloqueoshorarios_peluqueroid_fkey FOREIGN KEY (peluqueroid) REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE;
 \   ALTER TABLE ONLY public.bloqueoshorarios DROP CONSTRAINT bloqueoshorarios_peluqueroid_fkey;
       public               postgres    false    222    228    4833                       2606    17257    citas citas_clienteid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.citas
    ADD CONSTRAINT citas_clienteid_fkey FOREIGN KEY (clienteid) REFERENCES public.clientes(clienteid) ON DELETE CASCADE;
 D   ALTER TABLE ONLY public.citas DROP CONSTRAINT citas_clienteid_fkey;
       public               postgres    false    220    230    4827                       2606    17262    citas citas_peluqueroid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.citas
    ADD CONSTRAINT citas_peluqueroid_fkey FOREIGN KEY (peluqueroid) REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE;
 F   ALTER TABLE ONLY public.citas DROP CONSTRAINT citas_peluqueroid_fkey;
       public               postgres    false    222    230    4833                       2606    17267    citas citas_servicioid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.citas
    ADD CONSTRAINT citas_servicioid_fkey FOREIGN KEY (servicioid) REFERENCES public.servicios(servicioid) ON DELETE CASCADE;
 E   ALTER TABLE ONLY public.citas DROP CONSTRAINT citas_servicioid_fkey;
       public               postgres    false    4839    230    224                       2606    17191     clientes clientes_usuarioid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT clientes_usuarioid_fkey FOREIGN KEY (usuarioid) REFERENCES public.usuarios(usuarioid) ON DELETE CASCADE;
 J   ALTER TABLE ONLY public.clientes DROP CONSTRAINT clientes_usuarioid_fkey;
       public               postgres    false    218    4825    220            #           2606    17474 4   favoritos_clientes favoritos_clientes_clienteid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.favoritos_clientes
    ADD CONSTRAINT favoritos_clientes_clienteid_fkey FOREIGN KEY (clienteid) REFERENCES public.clientes(clienteid) ON DELETE CASCADE;
 ^   ALTER TABLE ONLY public.favoritos_clientes DROP CONSTRAINT favoritos_clientes_clienteid_fkey;
       public               postgres    false    4827    220    246            $           2606    17479 2   favoritos_clientes favoritos_clientes_localid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.favoritos_clientes
    ADD CONSTRAINT favoritos_clientes_localid_fkey FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 \   ALTER TABLE ONLY public.favoritos_clientes DROP CONSTRAINT favoritos_clientes_localid_fkey;
       public               postgres    false    238    246    4855            
           2606    17486    usuarios fk_admin_local    FK CONSTRAINT     �   ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT fk_admin_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 A   ALTER TABLE ONLY public.usuarios DROP CONSTRAINT fk_admin_local;
       public               postgres    false    238    218    4855                       2606    17461    citas fk_cita_local    FK CONSTRAINT     �   ALTER TABLE ONLY public.citas
    ADD CONSTRAINT fk_cita_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 =   ALTER TABLE ONLY public.citas DROP CONSTRAINT fk_cita_local;
       public               postgres    false    4855    238    230                       2606    17506 $   notificaciones fk_notificacion_local    FK CONSTRAINT     �   ALTER TABLE ONLY public.notificaciones
    ADD CONSTRAINT fk_notificacion_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 N   ALTER TABLE ONLY public.notificaciones DROP CONSTRAINT fk_notificacion_local;
       public               postgres    false    232    4855    238                       2606    17491    pagos fk_pago_local    FK CONSTRAINT     �   ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT fk_pago_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 =   ALTER TABLE ONLY public.pagos DROP CONSTRAINT fk_pago_local;
       public               postgres    false    242    4855    238                       2606    17567    reportes fk_peluquero    FK CONSTRAINT     �   ALTER TABLE ONLY public.reportes
    ADD CONSTRAINT fk_peluquero FOREIGN KEY (peluqueroid) REFERENCES public.peluqueros(peluqueroid) ON UPDATE CASCADE ON DELETE SET NULL;
 ?   ALTER TABLE ONLY public.reportes DROP CONSTRAINT fk_peluquero;
       public               postgres    false    4833    222    240                       2606    17451    peluqueros fk_peluquero_local    FK CONSTRAINT     �   ALTER TABLE ONLY public.peluqueros
    ADD CONSTRAINT fk_peluquero_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 G   ALTER TABLE ONLY public.peluqueros DROP CONSTRAINT fk_peluquero_local;
       public               postgres    false    238    222    4855                       2606    17501    reportes fk_reporte_local    FK CONSTRAINT     �   ALTER TABLE ONLY public.reportes
    ADD CONSTRAINT fk_reporte_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 C   ALTER TABLE ONLY public.reportes DROP CONSTRAINT fk_reporte_local;
       public               postgres    false    240    238    4855                       2606    17496    resenas fk_resena_local    FK CONSTRAINT     �   ALTER TABLE ONLY public.resenas
    ADD CONSTRAINT fk_resena_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 A   ALTER TABLE ONLY public.resenas DROP CONSTRAINT fk_resena_local;
       public               postgres    false    4855    238    236                       2606    17456    servicios fk_servicio_local    FK CONSTRAINT     �   ALTER TABLE ONLY public.servicios
    ADD CONSTRAINT fk_servicio_local FOREIGN KEY (localid) REFERENCES public.local(localid) ON DELETE CASCADE;
 E   ALTER TABLE ONLY public.servicios DROP CONSTRAINT fk_servicio_local;
       public               postgres    false    238    224    4855            !           2606    17428 +   historial_citas historial_citas_citaid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.historial_citas
    ADD CONSTRAINT historial_citas_citaid_fkey FOREIGN KEY (citaid) REFERENCES public.citas(citaid) ON DELETE CASCADE;
 U   ALTER TABLE ONLY public.historial_citas DROP CONSTRAINT historial_citas_citaid_fkey;
       public               postgres    false    4845    244    230            "           2606    17433 .   historial_citas historial_citas_usuarioid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.historial_citas
    ADD CONSTRAINT historial_citas_usuarioid_fkey FOREIGN KEY (usuarioid) REFERENCES public.usuarios(usuarioid) ON DELETE SET NULL;
 X   ALTER TABLE ONLY public.historial_citas DROP CONSTRAINT historial_citas_usuarioid_fkey;
       public               postgres    false    244    218    4825                       2606    17229 -   horarios_peluqueros horarios_peluqueroid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.horarios_peluqueros
    ADD CONSTRAINT horarios_peluqueroid_fkey FOREIGN KEY (peluqueroid) REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE;
 W   ALTER TABLE ONLY public.horarios_peluqueros DROP CONSTRAINT horarios_peluqueroid_fkey;
       public               postgres    false    222    226    4833                       2606    17295 ,   notificaciones notificaciones_clienteid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.notificaciones
    ADD CONSTRAINT notificaciones_clienteid_fkey FOREIGN KEY (clienteid) REFERENCES public.clientes(clienteid) ON DELETE CASCADE;
 V   ALTER TABLE ONLY public.notificaciones DROP CONSTRAINT notificaciones_clienteid_fkey;
       public               postgres    false    232    220    4827                       2606    17399    pagos pagos_citaid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_citaid_fkey FOREIGN KEY (citaid) REFERENCES public.citas(citaid) ON DELETE CASCADE;
 A   ALTER TABLE ONLY public.pagos DROP CONSTRAINT pagos_citaid_fkey;
       public               postgres    false    242    4845    230                       2606    17404    pagos pagos_clienteid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_clienteid_fkey FOREIGN KEY (clienteid) REFERENCES public.clientes(clienteid) ON DELETE CASCADE;
 D   ALTER TABLE ONLY public.pagos DROP CONSTRAINT pagos_clienteid_fkey;
       public               postgres    false    220    4827    242                        2606    17409    pagos pagos_peluqueroid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_peluqueroid_fkey FOREIGN KEY (peluqueroid) REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE;
 F   ALTER TABLE ONLY public.pagos DROP CONSTRAINT pagos_peluqueroid_fkey;
       public               postgres    false    222    4833    242                       2606    17207 $   peluqueros peluqueros_usuarioid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.peluqueros
    ADD CONSTRAINT peluqueros_usuarioid_fkey FOREIGN KEY (usuarioid) REFERENCES public.usuarios(usuarioid) ON DELETE CASCADE;
 N   ALTER TABLE ONLY public.peluqueros DROP CONSTRAINT peluqueros_usuarioid_fkey;
       public               postgres    false    222    218    4825                       2606    17357 .   reportes reportes_servicio_mas_solicitado_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.reportes
    ADD CONSTRAINT reportes_servicio_mas_solicitado_fkey FOREIGN KEY (servicio_mas_solicitado) REFERENCES public.servicios(servicioid);
 X   ALTER TABLE ONLY public.reportes DROP CONSTRAINT reportes_servicio_mas_solicitado_fkey;
       public               postgres    false    224    4839    240                       2606    17322    resenas resenas_clienteid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.resenas
    ADD CONSTRAINT resenas_clienteid_fkey FOREIGN KEY (clienteid) REFERENCES public.clientes(clienteid) ON DELETE CASCADE;
 H   ALTER TABLE ONLY public.resenas DROP CONSTRAINT resenas_clienteid_fkey;
       public               postgres    false    236    220    4827                       2606    17327     resenas resenas_peluqueroid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.resenas
    ADD CONSTRAINT resenas_peluqueroid_fkey FOREIGN KEY (peluqueroid) REFERENCES public.peluqueros(peluqueroid) ON DELETE CASCADE;
 J   ALTER TABLE ONLY public.resenas DROP CONSTRAINT resenas_peluqueroid_fkey;
       public               postgres    false    236    222    4833            �   �   x�m�;�0E�zf�@��;��P�X�Q���IG��?��28\��>������L>s�X�Z�Pe������'�5݋.��Q7��>�HҤ��U��6p��!�w)Vu��'┖Ԋ/��Rl��R�e3�x+��v�;      �      x������ � �      �   t  x��XM�!]Ϝb.�	4�ֽ��|���J?]x��~Mg���$����������`�a��O��>�������ۏ���;�1�l���z<�ͦkxq�~���
���޿?��j���Y�9y��A��=���є����yX�rg�z��"�RpT�Hѱ�s���|zX�������7Lx4�9{�'��x���a��yv�����G�L�Q[_Z�b)߰Z�"������Gx�9e�n_�<�o?{��	؍��+:�N�H���������a�V��5{��W�7R�����~��yS<M@��9}��g�n`'<�N�����M�I��V8������eE�L���(�5�?����L8MOi����w�wh�%�P��Y��O�Z�����-wH�$����oW����|n��Y<���ݹ���Y7�<�̆"���3���hz���lя��J�SѶ���;ǆK�i��Ɓ��y\
�.t�AQ�	��š�CC���*��{e.S��(�ԯ���}h���܋C-<:��L�|�K�v�<?�[+n�-ފ�c$�i�~�_�١�Oӕ��������ΰ�si�u񫝣;��A�����:8�\��yD�*7V�ip+kɌZ���aZv}Q��ʷC�>�8_T/vA�;oN�'���ٓ��������j�#{h����]X]���h�s돓\B���������O	t���eїsi�R�p�ױ���$:m����59�����Qi��O�5��x�%3�w]c�&;�w��N�뢕];���f|ZW,���a�Sî���Vw���h��S�7O��k�Ӫ��*�"�d�M� ��E��qzg������M�2��xo�d���EY�����H���NU�W�+�R�1�4^�uЈ�-�3b�%�U�Vhn �p'x_�w��;�G���l�~#�O^�MrӺ*�s����]��Y���B��}߅2���
7��z�t ��Xзp�W���q���z���>s�,j8Y�v��\FǙ1�D^Nl_�LZ��E�C��Ӧb��,�z_:�6�pz\��6��C�T�4����ƨ��幅u��"~u��\�/MmYg��:,[Z(�hO�3��i�z���^d���>����5�Q�      �   �   x�M�A
�0�����@��$�]
ҢTq+t3b�@�@����sx1SZ��,>~� Ǒ�|��NP	�JWfX�.JY�$(hl
bxY�c~���#��~��}�B C(���˯4�_707H	�\�m���m�ҨZ�?̗�Y�!9;�=�n�xK��KQ�b��1���;�      �   W   x��A
� е��(L]u� D4�t@��W���A��#w����7a���S�
4�	DV;�� ��{<y�RS�+w�k)��Y�      �      x������ � �      �   N   x�E��� �7�"`��#�Z迎�%�<'�akD���>�f��S�/�C�
���_��)��w�$h1=2�^      �      x������ � �      �     x�}�=N�0�zr4cg�xk�THT4D($R`�%�؋���m����̓5n����н�3>r�w���v7}�ŧ�����������v�}��ca�}�MC�] w��-p)��8k�ۚ��Y��Zn�����ak�vm�ֳ�e���,�Ԛe8��_����k���b�h���MO�f�:=e���ؑ�̭�7����^�)�AX\�Y�[uH�A=���md���6��Kš�ՠ$.Q������~ ��I      �   �  x�uUIn#7]S���2kbUyg	�F �#X�^����-*,RM���W�F�9@A����$���� ZP����J2��άqd�������	�	��(�c��C�|8̍���g
�Bz�����T8/�!K���ɷϓ��/[A?	�h��������A)A?JW4�	OY�$i����$D������	=H��I�?VBZD�~��ւ�l5R� ��\Ѣ <cY�q�IoPK��(�?Y�5� J�J�1�8{	��Ƚ�5�u�����y^ +Q1?���~�c�<�i��!kq�J�(	���6|�D��ڽ����'d����,Y����G�<���D�T��7�[AS��3�6`:Ĕ|Ϡ�U����� ��Ѣg�Eq�e�C2�?�6��3x�� a��!dd���ދ�P� +p�C�\�Z�^�$�x�Q���\��$

M�����������d�����Ë�D:�ZP���X��Ly�,�{+�6G��0��[��,<���XFXF׾��r�؉f�7�F��N1p�I����u�BT�B�hL��z�-�⦘H�ep)��գI�h1�e}��;�X���+�<��0�p-�x�� ڭm�)?�z�y9d�����T�D��T�11$��W�6��os�����!C�P�����Q1+����4�?��޹j���&�ܶ/0&k��֨������ɋw2�:��ב����V��?�����-�\c����ֻ��Ҙb��!T���]��o��d}��e;�u
Twu:%(5V��}]���ie%v$�����_�@�X���_Q���S5��`Q�`b�%F�"0n�����������������2��~k��eQ�҂�<O�5�6U��eO�M'�ɿ�?      �      x������ � �      �      x������ � �      �     x�e�Mn�0��ϧ�R�'1dق�Z�
�[61UP�+;A�7b�Eϐ��	����b���f4T����|�|ӝ��
*,?��A	�%"M��ĎI=a�2��>K��*ǳu|iC(!oBÿT5����P�k�oͩ��E]������m����Ej�Z`I�t����zo�'������.��3(�9˂?TF_�yl��z욎��q��O-�|՝},�J���L���˽6B�4:�����`g��_�ƿ̡3,"4�EYU��f<�'{�ۦ�n[�1�~ ��y�      �      x������ � �      �     x�}�1n�0Eg��"�%���Ev��AlvR ��Yr�ʍ��K4h���" ؼ�����)8��k��g}���1|�a�}�lWZV����΋([�bP�`�a��ˡ�&<�1�i�Cj�aL�x.����˔��쯗	�q�z4A��VG���CҞH���jx�"XGl�x�vCv�Ƹw����=���*S�]h��n�>�gZ������ވr��(����=�{�]��搜g�e�K3c�%�rTܥ��1�&��QV�rs��*���2q{      �     x��V���6���`��`I��+� ) ^����>���!wse�+�ti�c�%)Y�K������,�O���8ّ�+�W��mT����:?��R��J�^�I=�j%���(����-��c�ړ1N<F��՗�-Ǐq���8/�$Cm|� 6+�%�R<�[s��u������t��U&�3���7�_>~?k��Q����Ӂ��մ��n�2p��|c�Q��������������f���5�*ƫ������i���59xw����d�qA5�Pk�5m�2��̤�f�E\3w�[0Eoė� /~%no�`p�4� �UwnGdcϣ�#�C:h� �m.�wy�,�¨M��H�����o �9��T��ZsruQ�rieG.��^hQ_�\��G�q��;���i�j�#QP��с�ɿ2���;e�����!��ʩ��BU�� �x\���3ÄP�7�j`O����3+l�]F���-�M~��'����,�hĭ���фW����,+K����'�"z-�ׁ z���ʛ�1Vz;k�(���k��BH��++/�v�*.휶R�֜�f��D확����AG�1g�`�b�%�5E5M,����	�<�~�$��^��][��� ��(����ro��Y{�n�c�y?����Z�`(Mn]��+��\$�n�f�NΖU��Aӻ�V����]�陽�ʚ F9�'��轧��x�C�lG}��|V@p�_,:Π�m����Qo�Hw3Ȳ;Z�2��g�x����2T���Ae�L� ��-��fPHߨk+�`�g�uZy�������N�3������?���f*�����S7�� ʠԖ�\�����ͭL'��0��	^�G�z��-����(�V��>�wtl���� �~�(����;*�Y��u�ƽ�:C=�$.��4�Ki��Yǳ�I�P˒���[���7=�w�y�p.^2�V�6٤w7BO0\�G�6�rL�,��43|m����][���.%U�oEQ�E^      �   l  x���˒�JE��5Ny�:+�*" �DGT$��!y�|}[޲J��g9 ��}�I("�aJ��F[o���Y-A��+��TGO���YP@�#dt�Î��V�I����	;PZ!����{GL�'�p*>��@GêC�ډ�p��G9ǃ���V�#O&uTϊp9�
�鄦�.*^4Kd(C6.*T>4�l��l}���1\*Q*���05��3lZ������E�v��7P\�5*0Aݡ��11K`�E�|X�{z�/n�]�R�{��}TSZ�w�eF���,!4T�c�!,�=���s�	,prS��)P��%0���1�Y�?l۪R٦y����^l:�uQHM���cؤ��r0�V���]�
��K���QϦS�QV��_N��| s� ���~[�b�N�e�*�6��2�4W�.b���'�{�[O������e��D�=�Q�>�b8"�����x��~?������K�yg�~/�4��}@��f���D/�6�jwp���_"}Ҋ3z��!���$��f�N=�J�Pg�h55}�oO�oI�����̸p�#��W�ߕ�*֧��7ݽ�mD���#�k���b;�֑��m󬖣@���4G8�	�+����K��I��ƾC_*i���s�/�؂Z�x�v|2�5��Ƀ��bK԰aI^^�ň����`6��=~���O�S"(��A�m2M���&������ԵfN���_���,E2Y�m.G�>?3��7�Nt`\�4��O$q{��`a�/l�g�4����F����$�j%�R�pT[��H�k�/D�~�w.��o3%�_P��DK�0�޷Sy%���1B���s�G2�$����t��DҎxX���S�_`0�`"�P     