-- V6__create_cuentas.sql
-- Accounting module (Cuentas por cobrar/pagar)

CREATE TYPE tipo_cuenta AS ENUM ('COBRAR', 'PAGAR');
CREATE TYPE estado_cuenta AS ENUM ('PENDIENTE', 'PAGADO', 'VENCIDO', 'CANCELADO');

CREATE TABLE cuentas (
    id BIGSERIAL PRIMARY KEY,
    folio VARCHAR(50) UNIQUE NOT NULL,
    tipo tipo_cuenta NOT NULL,
    referencia_tipo VARCHAR(50),
    referencia_id BIGINT,
    cliente_id BIGINT,
    descripcion TEXT NOT NULL,
    monto DECIMAL(15, 2) NOT NULL,
    saldo DECIMAL(15, 2) NOT NULL,
    fecha_emision DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_vencimiento DATE NOT NULL,
    estado estado_cuenta NOT NULL DEFAULT 'PENDIENTE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by BIGINT NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_cuentas_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
    CONSTRAINT fk_cuentas_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
    CONSTRAINT chk_monto_positivo CHECK (monto > 0),
    CONSTRAINT chk_saldo_valido CHECK (saldo >= 0 AND saldo <= monto)
);

-- Indexes
CREATE INDEX idx_cuentas_folio ON cuentas(folio) WHERE deleted_at IS NULL;
CREATE INDEX idx_cuentas_tipo ON cuentas(tipo) WHERE deleted_at IS NULL;
CREATE INDEX idx_cuentas_estado ON cuentas(estado) WHERE deleted_at IS NULL;
CREATE INDEX idx_cuentas_cliente ON cuentas(cliente_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_cuentas_vencimiento ON cuentas(fecha_vencimiento) WHERE deleted_at IS NULL;
-- Composite index for overdue accounts query
CREATE INDEX idx_cuentas_estado_vencimiento ON cuentas(estado, fecha_vencimiento) WHERE deleted_at IS NULL;

-- Function to generate account folio
CREATE OR REPLACE FUNCTION generar_folio_cuenta()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.folio IS NULL OR NEW.folio = '' THEN
        IF NEW.tipo = 'COBRAR' THEN
            NEW.folio := 'CC-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(NEXTVAL('cuentas_id_seq')::TEXT, 6, '0');
        ELSE
            NEW.folio := 'CP-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(NEXTVAL('cuentas_id_seq')::TEXT, 6, '0');
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_generar_folio_cuenta
    BEFORE INSERT ON cuentas
    FOR EACH ROW
    EXECUTE FUNCTION generar_folio_cuenta();

-- Comments
COMMENT ON TABLE cuentas IS 'Accounts receivable and payable';
COMMENT ON COLUMN cuentas.tipo IS 'Account type: COBRAR (receivable) or PAGAR (payable)';
COMMENT ON COLUMN cuentas.referencia_tipo IS 'Reference type (e.g., VENTA, COMPRA)';
COMMENT ON COLUMN cuentas.referencia_id IS 'Reference ID to related entity';
COMMENT ON COLUMN cuentas.saldo IS 'Remaining balance (monto - payments made)';
