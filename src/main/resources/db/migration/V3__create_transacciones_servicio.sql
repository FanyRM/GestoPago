-- Migración V3: Esquema de Clientes, Cuentas, Domicilios y Transacciones de Servicios (GestoPago)

-- 1. Tabla de Clientes
CREATE TABLE IF NOT EXISTS clientes (
    id                   SERIAL PRIMARY KEY,
    nombre               VARCHAR(100)   NOT NULL,
    segundo_nombre       VARCHAR(100),
    apellido_paterno     VARCHAR(100)   NOT NULL,
    apellido_materno     VARCHAR(100)   NOT NULL,
    fecha_nacimiento     DATE           NOT NULL,
    curp                 VARCHAR(18)    NOT NULL,
    rfc                  VARCHAR(13)    NOT NULL,
    sexo                 VARCHAR(1)     NOT NULL,
    id_nacionalidad      INTEGER,
    id_estado_civil      INTEGER,
    id_ocupacion         INTEGER,
    empresa              VARCHAR(150),
    ingreso_mensual      NUMERIC(15, 2) NOT NULL,
    correo_electronico   VARCHAR(100)   NOT NULL,
    lada_movil           INTEGER        NOT NULL,
    telefono_movil       INTEGER        NOT NULL,
    lada_alternativo     INTEGER,
    telefono_alternativo INTEGER,
    activo               BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_clientes_curp UNIQUE (curp),
    CONSTRAINT uq_clientes_correo UNIQUE (correo_electronico),
    CONSTRAINT fk_cliente_nacionalidad FOREIGN KEY (id_nacionalidad) REFERENCES catalogo_nacionalidad (id),
    CONSTRAINT fk_cliente_estadocivil FOREIGN KEY (id_estado_civil) REFERENCES catalogo_estado_civil (id),
    CONSTRAINT fk_cliente_ocupacion FOREIGN KEY (id_ocupacion) REFERENCES catalogo_ocupacion (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_cliente_curp ON clientes (curp);
CREATE INDEX IF NOT EXISTS idx_cliente_rfc ON clientes (rfc);

-- 2. Tabla de Domicilios
CREATE TABLE IF NOT EXISTS domicilios (
    id                   SERIAL PRIMARY KEY,
    id_cliente           INTEGER        NOT NULL,
    calle                VARCHAR(150)   NOT NULL,
    numero_exterior      VARCHAR(20)    NOT NULL,
    numero_interior      VARCHAR(20),
    colonia              VARCHAR(150)   NOT NULL,
    municipio            VARCHAR(150)   NOT NULL,
    estado               VARCHAR(150)   NOT NULL,
    codigo_postal        VARCHAR(10)    NOT NULL,
    id_pais              INTEGER,
    CONSTRAINT uq_domicilios_cliente UNIQUE (id_cliente),
    CONSTRAINT fk_domicilio_cliente FOREIGN KEY (id_cliente) REFERENCES clientes (id) ON DELETE CASCADE,
    CONSTRAINT fk_domicilio_pais FOREIGN KEY (id_pais) REFERENCES catalogo_pais (id)
);

-- 3. Tabla de Cuentas
CREATE TABLE IF NOT EXISTS cuentas (
    id                   SERIAL PRIMARY KEY,
    id_cliente           INTEGER        NOT NULL,
    numero_cuenta        VARCHAR(20)    NOT NULL,
    saldo                NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    estatus              VARCHAR(20)    NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cuentas_numero UNIQUE (numero_cuenta),
    CONSTRAINT fk_cuenta_cliente FOREIGN KEY (id_cliente) REFERENCES clientes (id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_cuenta_numero ON cuentas (numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_cuentas_cliente ON cuentas (id_cliente);
CREATE INDEX IF NOT EXISTS idx_cuentas_estatus ON cuentas (estatus);

-- 4. Inserción inicial de cliente y cuenta demo para pruebas (si no existen)
INSERT INTO clientes (id, nombre, segundo_nombre, apellido_paterno, apellido_materno, fecha_nacimiento, curp, rfc, sexo, id_nacionalidad, id_estado_civil, id_ocupacion, empresa, ingreso_mensual, correo_electronico, lada_movil, telefono_movil, activo)
VALUES (1, 'Juan', 'Carlos', 'Pérez', 'López', '1990-01-15', 'PELJ900115HDFRRN01', 'PELJ900115ABC', 'M', 1, 1, 1, 'Empresa Demo S.A.', 25000.00, 'juan.perez@demo.com', 52, 1234567890, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO domicilios (id, id_cliente, calle, numero_exterior, numero_interior, colonia, municipio, estado, codigo_postal, id_pais)
VALUES (1, 1, 'Av. Reforma', '123', 'A', 'Juárez', 'Cuauhtémoc', 'Ciudad de México', '06600', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO cuentas (id, id_cliente, numero_cuenta, saldo, estatus)
VALUES (1, 1, '1234567890', 50000.00, 'ACTIVA')
ON CONFLICT (numero_cuenta) DO NOTHING;

-- Sincronizar secuencias seriales
SELECT setval(pg_get_serial_sequence('clientes', 'id'), COALESCE((SELECT MAX(id) FROM clientes), 1));
SELECT setval(pg_get_serial_sequence('domicilios', 'id'), COALESCE((SELECT MAX(id) FROM domicilios), 1));
SELECT setval(pg_get_serial_sequence('cuentas', 'id'), COALESCE((SELECT MAX(id) FROM cuentas), 1));

-- 5. Tabla de Transacciones de Servicios y Recargas (GestoPago)
CREATE TABLE IF NOT EXISTS transacciones_servicio (
    id                          BIGSERIAL PRIMARY KEY,
    cuenta_id                   INTEGER        NOT NULL,
    cliente_id                  INTEGER        NOT NULL,
    numero_cuenta               VARCHAR(20)    NOT NULL,
    clave_idempotencia          VARCHAR(100)   NOT NULL,
    upc                         VARCHAR(15)    NOT NULL,
    id_servicio                 INTEGER        NOT NULL,
    nombre_servicio             VARCHAR(150),
    id_producto                 INTEGER        NOT NULL,
    nombre_producto             VARCHAR(150),
    monto                       NUMERIC(15, 2) NOT NULL,
    saldo_anterior              NUMERIC(15, 2) NOT NULL,
    saldo_posterior             NUMERIC(15, 2) NOT NULL,
    telefono                    VARCHAR(20),
    referencia                  VARCHAR(100),
    estatus                     VARCHAR(30)    NOT NULL,
    codigo_respuesta_proveedor  VARCHAR(20),
    mensaje_proveedor           VARCHAR(500),
    folio_proveedor             VARCHAR(100),
    num_autorizacion            VARCHAR(100),
    hora_local                  VARCHAR(14)    NOT NULL,
    intentos_conciliacion       INTEGER        NOT NULL DEFAULT 0,
    fecha_creacion              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion         TIMESTAMP,
    CONSTRAINT uq_transacciones_idempotencia UNIQUE (clave_idempotencia),
    CONSTRAINT uq_transacciones_upc UNIQUE (upc),
    CONSTRAINT chk_transacciones_monto_positivo CHECK (monto > 0),
    CONSTRAINT fk_transacciones_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuentas (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacciones_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_tx_servicio_cliente_fecha ON transacciones_servicio (cliente_id, fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_tx_servicio_cuenta_fecha ON transacciones_servicio (cuenta_id, fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_tx_servicio_numero_cuenta ON transacciones_servicio (numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_tx_servicio_estatus ON transacciones_servicio (estatus);
