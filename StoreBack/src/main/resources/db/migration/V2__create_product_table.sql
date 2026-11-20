-- Migration: V2__create_product_table.sql
-- Description: Create product table with UUID v7 primary key and foreign key to category

CREATE TABLE IF NOT EXISTS product (
    id UUID NOT NULL,
    category_id UUID NOT NULL,
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    size_label VARCHAR(50),
    selling_price NUMERIC(12, 2) NOT NULL,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT uq_product_sku UNIQUE (sku)
);

CREATE INDEX IF NOT EXISTS idx_product_category_id ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_sku ON product(sku);
