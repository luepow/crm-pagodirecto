-- V4__create_ventas.sql
-- Sales module

CREATE TYPE estado_venta AS ENUM ('BORRADOR', 'CONFIRMADA', 'ENVIADA', 'COMPLETADA', 'CANCELADA');

CREATE TABLE ventas (
    id BIGSERIAL PRIMARY KEY,
    folio VARCHAR(50) UNIQUE NOT NULL,
    cliente_id BIGINT NOT NULL,
    fecha_venta TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    estado estado_venta NOT NULL DEFAULT 'BORRADOR',
    subtotal DECIMAL(15, 2) NOT NULL DEFAULT 0,
    descuento DECIMAL(15, 2) NOT NULL DEFAULT 0,
    impuestos DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total DECIMAL(15, 2) NOT NULL DEFAULT 0,
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by BIGINT NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_ventas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_ventas_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
    CONSTRAINT fk_ventas_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
    CONSTRAINT chk_subtotal_positivo CHECK (subtotal >= 0),
    CONSTRAINT chk_total_positivo CHECK (total >= 0)
);

CREATE TABLE detalles_venta (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(15, 2) NOT NULL,
    descuento DECIMAL(15, 2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(15, 2) NOT NULL,
    CONSTRAINT fk_detalles_venta_venta FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalles_venta_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT chk_precio_positivo CHECK (precio_unitario >= 0)
);

-- Indexes
CREATE INDEX idx_ventas_folio ON ventas(folio) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_cliente ON ventas(cliente_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_fecha ON ventas(fecha_venta) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_estado ON ventas(estado) WHERE deleted_at IS NULL;
CREATE INDEX idx_ventas_created_by ON ventas(created_by);

CREATE INDEX idx_detalles_venta_venta ON detalles_venta(venta_id);
CREATE INDEX idx_detalles_venta_producto ON detalles_venta(producto_id);

-- Function to generate folio
CREATE OR REPLACE FUNCTION generar_folio_venta()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.folio IS NULL OR NEW.folio = '' THEN
        NEW.folio := 'VTA-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(NEXTVAL('ventas_id_seq')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_generar_folio_venta
    BEFORE INSERT ON ventas
    FOR EACH ROW
    EXECUTE FUNCTION generar_folio_venta();

-- Comments
COMMENT ON TABLE ventas IS 'Sales orders/invoices';
COMMENT ON TABLE detalles_venta IS 'Sales order line items';
COMMENT ON COLUMN ventas.folio IS 'Unique sales order number (auto-generated)';
COMMENT ON COLUMN ventas.estado IS 'Order status workflow';
