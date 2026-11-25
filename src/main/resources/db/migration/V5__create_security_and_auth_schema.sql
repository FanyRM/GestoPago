-- Migración V5: Esquema de Seguridad, Usuarios, Refresh Tokens, MFA TOTP y Auditoría de Login

-- 1. Tabla de Usuarios del Sistema
CREATE TABLE IF NOT EXISTS usuarios (
    id                   BIGSERIAL PRIMARY KEY,
    cliente_id           INTEGER,
    correo               VARCHAR(100) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    rol                  VARCHAR(30)  NOT NULL DEFAULT 'ROLE_CLIENTE',
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos    INTEGER      NOT NULL DEFAULT 0,
    bloqueado_hasta      TIMESTAMP,
    ultimo_login         TIMESTAMP,
    token_version        BIGINT       NOT NULL DEFAULT 0,
    totp_obligatorio     BOOLEAN      NOT NULL DEFAULT TRUE,
    totp_habilitado      BOOLEAN      NOT NULL DEFAULT FALSE,
    totp_secret_enc      VARCHAR(255),
    totp_configurado_en  TIMESTAMP,
    totp_ultimo_paso     BIGINT,
    fecha_creacion       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion  TIMESTAMP,
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios (correo);
CREATE INDEX IF NOT EXISTS idx_usuarios_activo ON usuarios (activo);
CREATE INDEX IF NOT EXISTS idx_usuarios_cliente_id ON usuarios (cliente_id);

-- 2. Tabla de Refresh Tokens opacos con rotación por familia
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL,
    familia_id      VARCHAR(64)  NOT NULL,
    token_hash      VARCHAR(64)  NOT NULL,
    expira_en       TIMESTAMP    NOT NULL,
    revocado        BOOLEAN      NOT NULL DEFAULT FALSE,
    reemplazado_por VARCHAR(64),
    ip_origen       VARCHAR(64),
    user_agent      VARCHAR(255),
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_usuario_id ON refresh_tokens (usuario_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_familia_id ON refresh_tokens (familia_id);

-- 3. Tabla de Desafíos MFA Temporales (Enrolamiento y Verificación TOTP)
CREATE TABLE IF NOT EXISTS mfa_challenges (
    id              BIGSERIAL PRIMARY KEY,
    challenge_hash  VARCHAR(64)  NOT NULL,
    usuario_id      BIGINT       NOT NULL,
    tipo            VARCHAR(20)  NOT NULL,
    intentos        INTEGER      NOT NULL DEFAULT 0,
    expira_en       TIMESTAMP    NOT NULL,
    consumido       BOOLEAN      NOT NULL DEFAULT FALSE,
    ip_origen       VARCHAR(64),
    user_agent      VARCHAR(255),
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_mfa_challenges_challenge_hash UNIQUE (challenge_hash),
    CONSTRAINT fk_mfa_challenges_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_mfa_challenges_usuario_id ON mfa_challenges (usuario_id);

-- 4. Tabla de Auditoría de Eventos de Autenticación
CREATE TABLE IF NOT EXISTS auditoria_login (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT,
    correo          VARCHAR(100) NOT NULL,
    evento          VARCHAR(40)  NOT NULL,
    exitoso         BOOLEAN      NOT NULL,
    ip_origen       VARCHAR(64),
    user_agent      VARCHAR(255),
    detalle         VARCHAR(255),
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_login_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_auditoria_login_correo_fecha ON auditoria_login (correo, fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_login_usuario_fecha ON auditoria_login (usuario_id, fecha_creacion DESC);
