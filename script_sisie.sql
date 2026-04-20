-- SCRIPT DE SISTEMA SISIE --

-- Creacion de base --
CREATE DATABASE sisie;
GO

USE sisie;
GO

-- 1. Tabla: provincia
CREATE TABLE provincia (
    id_provincia INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL
);

-- 2. Tabla: ciudad
CREATE TABLE ciudad (
    id_ciudad INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL,
    id_provincia INT NOT NULL,
    CONSTRAINT FK_ciudad_provincia FOREIGN KEY (id_provincia) REFERENCES provincia(id_provincia)
);

-- 3. Tabla: cliente
CREATE TABLE cliente (
    id_cliente INT IDENTITY(1,1) PRIMARY KEY,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombre VARCHAR(20) NOT NULL,
    apellido VARCHAR(20) NOT NULL
);

-- 4. Tabla: transporte
-- Nota: Para "nombre", el diccionario indicaba int(11), pero se asume VARCHAR(50) por tratarse de un nombre.
CREATE TABLE transporte (
    id_transporte INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL, 
    telefono VARCHAR(15)
);

-- 5. Tabla: estado
CREATE TABLE estado (
    id_estado INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL,
    descripcion VARCHAR(30)
);

-- 6. Tabla: usuario
CREATE TABLE usuario (
    id_usuario INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL,
    apellido VARCHAR(20) NOT NULL,
    dni VARCHAR(8) NOT NULL UNIQUE,
    telefono VARCHAR(15),
    email VARCHAR(50) NOT NULL,
    contraseña VARCHAR(200) NOT NULL,
    fecha_nacimiento DATE,
    fecha_creacion DATETIME NOT NULL,
    baja VARCHAR(2)
);

-- 7. Tabla: direccion
CREATE TABLE direccion (
    id_direccion INT IDENTITY(1,1) PRIMARY KEY,
    calle VARCHAR(50) NOT NULL,
    altura VARCHAR(11) NOT NULL,
    codigo_postal VARCHAR(11) NOT NULL,
    id_cliente INT NOT NULL,
    id_ciudad INT NOT NULL,
    CONSTRAINT FK_direccion_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT FK_direccion_ciudad FOREIGN KEY (id_ciudad) REFERENCES ciudad(id_ciudad)
);

-- 8. Tabla: venta
CREATE TABLE venta (
    id_venta INT IDENTITY(1,1) PRIMARY KEY,
    fecha_creacion DATETIME NOT NULL,
    id_cliente INT NOT NULL,
    CONSTRAINT FK_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

-- 9. Tabla: envios
CREATE TABLE envios (
    id_envio INT IDENTITY(1,1) PRIMARY KEY,
    cod_seguimiento VARCHAR(50) UNIQUE,
    costo DECIMAL(10,2),
    id_transporte INT NOT NULL,
    id_venta INT NOT NULL,
    id_direccion INT NOT NULL,
    id_estado INT NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    CONSTRAINT FK_envios_transporte FOREIGN KEY (id_transporte) REFERENCES transporte(id_transporte),
    CONSTRAINT FK_envios_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
    CONSTRAINT FK_envios_direccion FOREIGN KEY (id_direccion) REFERENCES direccion(id_direccion),
    CONSTRAINT FK_envios_estado FOREIGN KEY (id_estado) REFERENCES estado(id_estado)
);

-- 10. Tabla: historial_envio
CREATE TABLE historial_envio (
    id_historial_envio INT IDENTITY(1,1) PRIMARY KEY,
    fecha_movimiento DATETIME NOT NULL,
    motivo VARCHAR(30) NOT NULL,
    id_envio INT NOT NULL,
    id_usuario INT NOT NULL,
    id_estado INT NOT NULL,
    CONSTRAINT FK_historial_envios_envio FOREIGN KEY (id_envio) REFERENCES envios(id_envio),
    CONSTRAINT FK_historial_envios_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT FK_historial_envios_estado FOREIGN KEY (id_estado) REFERENCES estado(id_estado));



-- Dar permisos al usuario sobre la nueva base de datos 
USE sisie;
GO
-- Si el usuario "appuser" ya existe en el servidor:
CREATE USER appuser FOR LOGIN appuser;
ALTER ROLE db_owner ADD MEMBER appuser;
GO
