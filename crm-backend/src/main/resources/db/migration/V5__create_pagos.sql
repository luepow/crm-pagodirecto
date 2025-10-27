-- V5__create_pagos.sql
-- Payments module

CREATE TYPE metodo_pago AS ENUM ('EFECTIVO', 'TARJETA_CREDITO', 'TARJETA_DEBITO', 'TRANSFERENCIA', 'CHEQUE', 'OTRO');
CREATE TYPE estado_pago AS ENUM ('PENDIENTE', 'COMPLETADO', 'FALLIDO', 'REEMBOLSADO');

CREATE TABLE pagos (
    id BIGSERIAL PRIMARY KEY,
    folio VARCHAR(50) UNIQUE NOT NULL,
    venta_id BIGINT NOT NULL,
    metodo_pago metodo_pago NOT NULL,
    monto DECIMAL(15, 2) NOT NULL,
    fecha_pago TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    estado estado_pago NOT NULL DEFAULT 'PENDIENTE',
    referencia VARCHAR(100),
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by BIGINT NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_pagos_venta FOREIGN KEY (venta_id) REFERENCES ventas(id),
    CONSTRAINT fk_pagos_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
    CONSTRAINT fk_pagos_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
    CONSTRAINT chk_monto_positivo CHECK (monto > 0)
);

-- Indexes
CREATE INDEX idx_pagos_folio ON pagos(folio) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagos_venta ON pagos(venta_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagos_fecha ON pagos(fecha_pago) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagos_estado ON pagos(estado) WHERE deleted_at IS NULL;
CREATE INDEX idx_pagos_metodo ON pagos(metodo_pago) WHERE deleted_at IS NULL;

-- Function to generate payment folio
CREATE OR REPLACE FUNCTION generar_folio_pago()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.folio IS NULL OR NEW.folio = '' THEN
        NEW.folio := 'PAG-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(NEXTVAL('pagos_id_seq')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_generar_folio_pago
    BEFORE INSERT ON pagos
    FOR EACH ROW
    EXECUTE FUNCTION generar_folio_pago();

-- Comments
COMMENT ON TABLE pagos IS 'Payment transactions';
COMMENT ON COLUMN pagos.folio IS 'Unique payment number (auto-generated)';
COMMENT ON COLUMN pagos.referencia IS 'External reference (transaction ID, check number, etc)';
