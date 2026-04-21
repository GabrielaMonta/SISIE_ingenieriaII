# SISIE_ingenieriaII - Módulo de Envíos

Este proyecto corresponde al desarrollo del módulo de envíos del sistema SISIE para la cátedra de Ingeniería del Software II.

## Arquitectura
El sistema utiliza una arquitectura **Cliente-Servidor** implementada con **Spring Boot 3** y organizada internamente en capas:

1.  **capadepresentacion**: Controladores REST (API).
2.  **capadelogica**: Servicios y lógica de negocio.
3.  **capadedatos**: Repositorios de persistencia (Spring Data JPA).
4.  **capadedominio**: Entidades y modelos de datos.

## Tecnologías
- **Lenguaje**: Java 21
- **Framework**: Spring Boot
- **Gestión de dependencias**: Maven
- **Base de Datos**: SQL Server

## Requisitos
- Java JDK 22.
- Maven instalado (o uso de un IDE compatible).
- SQL Server Management Studio para la ejecución de scripts.

## Configuración Inicial
1. Ejecutar el archivo `script_sisie.sql` en tu servidor SQL Server.
2. Configurar las credenciales de acceso en `src/main/resources/application.properties`.
