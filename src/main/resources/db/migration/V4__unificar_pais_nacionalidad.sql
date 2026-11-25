-- Migración V4: Unificación de catálogos de país y nacionalidad con lada

-- 1. Agregar columnas nacionalidad y lada a catalogo_pais si no existen
ALTER TABLE catalogo_pais ADD COLUMN IF NOT EXISTS nacionalidad VARCHAR(100);
ALTER TABLE catalogo_pais ADD COLUMN IF NOT EXISTS lada VARCHAR(10);

-- 2. Poblar/actualizar los registros existentes en catalogo_pais con nacionalidad, ISO y lada
UPDATE catalogo_pais SET nacionalidad = 'Mexicana', codigo_iso = 'MEX', lada = '52' WHERE id = 1 OR descripcion ILIKE 'M%xico%';
UPDATE catalogo_pais SET nacionalidad = 'Estadounidense', codigo_iso = 'USA', lada = '1' WHERE id = 2 OR descripcion ILIKE 'Estados Unidos%';
UPDATE catalogo_pais SET nacionalidad = 'Canadiense', codigo_iso = 'CAN', lada = '1' WHERE id = 3 OR descripcion ILIKE 'Canad%';
UPDATE catalogo_pais SET nacionalidad = 'Española', codigo_iso = 'ESP', lada = '34' WHERE id = 4 OR descripcion ILIKE 'Espa%a%';
UPDATE catalogo_pais SET nacionalidad = 'Colombiana', codigo_iso = 'COL', lada = '57' WHERE id = 5 OR descripcion ILIKE 'Colombia%';

-- 3. Si hay otros registros en catalogo_nacionalidad que coincidan por ID y falte nacionalidad, sincronizarlos
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = 'catalogo_nacionalidad' AND table_type = 'BASE TABLE'
    ) THEN
        UPDATE catalogo_pais p
        SET nacionalidad = n.descripcion
        FROM catalogo_nacionalidad n
        WHERE p.id = n.id AND (p.nacionalidad IS NULL OR p.nacionalidad = '');
    END IF;
END $$;

-- 4. Modificar la clave foránea en la tabla clientes para que id_nacionalidad apunte a catalogo_pais (id)
ALTER TABLE clientes DROP CONSTRAINT IF EXISTS fk_cliente_nacionalidad;
ALTER TABLE clientes ADD CONSTRAINT fk_cliente_nacionalidad FOREIGN KEY (id_nacionalidad) REFERENCES catalogo_pais (id);

-- 5. Eliminar la tabla separada catalogo_nacionalidad y reemplazarla por una vista para retrocompatibilidad
DROP TABLE IF EXISTS catalogo_nacionalidad CASCADE;

CREATE OR REPLACE VIEW catalogo_nacionalidad AS
SELECT id, nacionalidad AS descripcion, activo
FROM catalogo_pais;
