-------------------------------------------------------------------------------
-- Migración V5: Agregar Campos de Perfil de Usuario
--
-- Descripción:
-- Agrega campos adicionales para el perfil del usuario:
-- - nombre_completo: Nombre completo del usuario
-- - telefono: Número de teléfono de contacto
-- - cargo: Puesto o cargo en la organización
-- - departamento: Departamento al que pertenece
-- - photo_url: URL de la foto de perfil
--
-- Autor: PagoDirecto Security Team
-- Fecha: 2025-10-13
-- Version: 5
-------------------------------------------------------------------------------

-- Agregar columnas a la tabla seguridad_usuarios
ALTER TABLE seguridad_usuarios
ADD COLUMN IF NOT EXISTS nombre_completo VARCHAR(100),
ADD COLUMN IF NOT EXISTS telefono VARCHAR(20),
ADD COLUMN IF NOT EXISTS cargo VARCHAR(100),
ADD COLUMN IF NOT EXISTS departamento VARCHAR(100),
ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

-- Crear índices para búsquedas por departamento
CREATE INDEX IF NOT EXISTS idx_seguridad_usuarios_departamento
    ON seguridad_usuarios(departamento)
    WHERE deleted_at IS NULL;

-- Agregar comentarios descriptivos
COMMENT ON COLUMN seguridad_usuarios.nombre_completo IS 'Nombre completo del usuario';
COMMENT ON COLUMN seguridad_usuarios.telefono IS 'Número de teléfono de contacto';
COMMENT ON COLUMN seguridad_usuarios.cargo IS 'Puesto o cargo en la organización';
COMMENT ON COLUMN seguridad_usuarios.departamento IS 'Departamento al que pertenece el usuario';
COMMENT ON COLUMN seguridad_usuarios.photo_url IS 'URL de la foto de perfil del usuario';

-- Opcional: Actualizar usuarios existentes con datos de ejemplo (solo desarrollo)
-- UPDATE seguridad_usuarios
-- SET
--   nombre_completo = CASE
--     WHEN username = 'admin' THEN 'Administrador del Sistema'
--     WHEN username = 'vendedor1' THEN 'Juan Pérez García'
--     ELSE username
--   END,
--   cargo = CASE
--     WHEN username = 'admin' THEN 'Administrador'
--     WHEN username = 'vendedor1' THEN 'Gerente de Ventas'
--     ELSE NULL
--   END,
--   departamento = CASE
--     WHEN username = 'vendedor1' THEN 'Ventas'
--     ELSE NULL
--   END
-- WHERE deleted_at IS NULL;

-------------------------------------------------------------------------------
-- Validaciones post-migración
-------------------------------------------------------------------------------

-- Verificar que las columnas se crearon correctamente
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguridad_usuarios'
        AND column_name = 'nombre_completo'
    ) THEN
        RAISE EXCEPTION 'Error: columna nombre_completo no fue creada';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguridad_usuarios'
        AND column_name = 'telefono'
    ) THEN
        RAISE EXCEPTION 'Error: columna telefono no fue creada';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguridad_usuarios'
        AND column_name = 'cargo'
    ) THEN
        RAISE EXCEPTION 'Error: columna cargo no fue creada';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguridad_usuarios'
        AND column_name = 'departamento'
    ) THEN
        RAISE EXCEPTION 'Error: columna departamento no fue creada';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguridad_usuarios'
        AND column_name = 'photo_url'
    ) THEN
        RAISE EXCEPTION 'Error: columna photo_url no fue creada';
    END IF;

    RAISE NOTICE 'Migración V5 completada exitosamente: Campos de perfil de usuario agregados';
END $$;
