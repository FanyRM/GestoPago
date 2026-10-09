-- Migración para poblar catálogos iniciales
-- Asegúrate de que los nombres de las tablas y campos coincidan con los de tus entidades

-- Insertar Estados Civiles
INSERT INTO catalogo_estado_civil (descripcion, activo) VALUES 
('Soltero(a)', true),
('Casado(a)', true),
('Divorciado(a)', true),
('Viudo(a)', true),
('Unión Libre', true)
ON CONFLICT DO NOTHING;

-- Insertar Nacionalidades
INSERT INTO catalogo_nacionalidad (descripcion, activo) VALUES 
('Mexicana', true),
('Estadounidense', true),
('Canadiense', true),
('Española', true),
('Colombiana', true)
ON CONFLICT DO NOTHING;

-- Insertar Ocupaciones
INSERT INTO catalogo_ocupacion (nombre, descripcion) VALUES 
('Estudiante', 'Estudiante'),
('Empleado', 'Empleado'),
('Independiente', 'Independiente'),
('Empresario', 'Empresario'),
('Hogar', 'Hogar'),
('Jubilado/Pensionado', 'Jubilado/Pensionado')
ON CONFLICT DO NOTHING;

-- Insertar Países
INSERT INTO catalogo_pais (descripcion, activo) VALUES 
('México', true),
('Estados Unidos', true),
('Canadá', true),
('España', true),
('Colombia', true)
ON CONFLICT DO NOTHING;
