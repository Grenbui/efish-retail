-- Migration: V1__create_category_table.sql
-- Description: Create category table with UUID v7 primary key

CREATE TABLE IF NOT EXISTS category (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_category_name ON category(name);
