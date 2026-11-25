-- Migración V6: Onboarding con cuenta única de ahorro y estatus de revisión
ALTER TABLE cuentas
    ADD COLUMN IF NOT EXISTS tipo_cuenta VARCHAR(20) NOT NULL DEFAULT 'AHORRO';

ALTER TABLE cuentas
    ALTER COLUMN estatus TYPE VARCHAR(30);

ALTER TABLE cuentas
    ADD COLUMN IF NOT EXISTS revisado_por VARCHAR(100);

ALTER TABLE cuentas
    ADD COLUMN IF NOT EXISTS observacion_revision VARCHAR(255);

ALTER TABLE cuentas
    ADD COLUMN IF NOT EXISTS fecha_revision TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_cuentas_tipo_cuenta ON cuentas (tipo_cuenta);
