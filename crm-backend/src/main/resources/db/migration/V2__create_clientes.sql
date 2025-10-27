-- V2__create_clientes.sql
-- Clientes/Customers module

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    direccion TEXT,
    ciudad VARCHAR(100),
    pais VARCHAR(100),
    codigo_postal VARCHAR(20),
    rfc VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by BIGINT NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_clientes_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
    CONSTRAINT fk_clientes_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id)
);

-- Indexes
CREATE INDEX idx_clientes_email ON clientes(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_nombre ON clientes(nombre) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_activo ON clientes(activo) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_created_by ON clientes(created_by);

-- Comments
COMMENT ON TABLE clientes IS 'Customer/client management';
COMMENT ON COLUMN clientes.activo IS 'Business active status';
COMMENT ON COLUMN clientes.rfc IS 'Registro Federal de Contribuyentes (Mexico Tax ID)';
