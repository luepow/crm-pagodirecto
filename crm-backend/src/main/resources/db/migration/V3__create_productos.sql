-- V3__create_productos.sql
-- Products/Catalog module

CREATE TABLE categorias_producto (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE productos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    categoria_id BIGINT,
    precio DECIMAL(15, 2) NOT NULL,
    costo DECIMAL(15, 2),
    stock INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER DEFAULT 0,
    unidad_medida VARCHAR(20) NOT NULL DEFAULT 'PZA',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by BIGINT NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias_producto(id),
    CONSTRAINT fk_productos_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
    CONSTRAINT fk_productos_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
    CONSTRAINT chk_precio_positivo CHECK (precio >= 0),
    CONSTRAINT chk_stock_no_negativo CHECK (stock >= 0)
);

-- Indexes
CREATE INDEX idx_productos_codigo ON productos(codigo) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_nombre ON productos(nombre) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_categoria ON productos(categoria_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_activo ON productos(activo) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_stock_bajo ON productos(stock) WHERE stock <= stock_minimo AND deleted_at IS NULL;

CREATE INDEX idx_categorias_nombre ON categorias_producto(nombre) WHERE deleted_at IS NULL;

-- Insert default categories
INSERT INTO categorias_producto (nombre, descripcion) VALUES
    ('General', 'Productos generales sin categoría específica'),
    ('Electrónica', 'Dispositivos y equipos electrónicos'),
    ('Servicios', 'Servicios profesionales'),
    ('Consumibles', 'Productos de consumo regular');

-- Comments
COMMENT ON TABLE productos IS 'Product/service catalog';
COMMENT ON TABLE categorias_producto IS 'Product categories';
COMMENT ON COLUMN productos.unidad_medida IS 'Unit of measure (PZA, KG, M, L, etc)';
COMMENT ON COLUMN productos.stock_minimo IS 'Minimum stock level for alerts';
