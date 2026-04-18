-- Script para crear la tabla de Usuario en SQL Server
-- Ejecutar esto en SQL Server Management Studio (SSMS)

CREATE TABLE usuario (
    id_usuario INT IDENTITY PRIMARY KEY,
    nombre VARCHAR(20),
    apellido VARCHAR(20),
    dni VARCHAR(8) NOT NULL UNIQUE,
    telefono VARCHAR(15),
    email VARCHAR(50),
    contraseña VARCHAR(200) NOT NULL,
    fecha_nacimiento DATE,
    fecha_creacion DATETIME NOT NULL,
    baja VARCHAR(2)
);

-- Ejemplo de inserción inicial
-- INSERT INTO usuario (nombre, apellido, dni, telefono, email, contraseña, fecha_nacimiento, fecha_creacion, baja)
-- VALUES ('Admin', 'Sisie', '00000000', '123456789', 'admin@sisie.com', 'admin123', '2000-01-01', GETDATE(), 'NO');
