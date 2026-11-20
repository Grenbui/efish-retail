-- Migration: V4__create_inventory_tables.sql
-- Description: Create inventory_stock and inventory_log tables

CREATE TABLE IF NOT EXISTS inventory_stock (
    product_id UUID NOT NULL,
    available_qty INTEGER NOT NULL DEFAULT 0,
    avg_cost NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_inventory_stock PRIMARY KEY (product_id),
    CONSTRAINT chk_inventory_stock_qty CHECK (available_qty >= 0)
);

CREATE TABLE IF NOT EXISTS inventory_log (
    id UUID NOT NULL,
    product_id UUID NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    qty_change INTEGER NOT NULL,
    unit_cost NUMERIC(12, 2),
    source_name VARCHAR(255),
    evidence_urls JSON,
    reference_id UUID,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_inventory_log PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_inventory_log_product_id ON inventory_log(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_log_reference_id ON inventory_log(reference_id);
CREATE INDEX IF NOT EXISTS idx_inventory_log_created_at ON inventory_log(created_at);
