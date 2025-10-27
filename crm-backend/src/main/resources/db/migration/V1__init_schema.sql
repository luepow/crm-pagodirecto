-- V1__init_schema.sql
-- Initial database schema for CRM/ERP system

-- Create usuarios table
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    apellido VARCHAR(200) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create usuarios_roles junction table
CREATE TABLE usuarios_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuarios_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_roles_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Insert default roles
INSERT INTO roles (nombre, descripcion) VALUES
    ('ROLE_ADMIN', 'Administrator with full access'),
    ('ROLE_MANAGER', 'Manager with CRUD access to most resources'),
    ('ROLE_USER', 'Regular user with read access'),
    ('ROLE_FINANCE', 'Financial department with access to payments and reports');

-- Insert default admin user (password: admin123)
INSERT INTO usuarios (username, email, password, nombre, apellido, activo)
VALUES ('admin', 'admin@empresa.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8ssO.wkZaK1VKzYSS6', 'Admin', 'User', true);

-- Assign ROLE_ADMIN to admin user
INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u, roles r
WHERE u.username = 'admin' AND r.nombre = 'ROLE_ADMIN';

-- Indexes for usuarios
CREATE INDEX idx_usuarios_username ON usuarios(username) WHERE deleted_at IS NULL;
CREATE INDEX idx_usuarios_email ON usuarios(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_usuarios_activo ON usuarios(activo) WHERE deleted_at IS NULL;

-- Indexes for usuarios_roles
CREATE INDEX idx_usuarios_roles_usuario ON usuarios_roles(usuario_id);
CREATE INDEX idx_usuarios_roles_rol ON usuarios_roles(rol_id);

-- Comments
COMMENT ON TABLE usuarios IS 'System users with authentication credentials';
COMMENT ON TABLE roles IS 'User roles for authorization';
COMMENT ON TABLE usuarios_roles IS 'Many-to-many relationship between users and roles';
