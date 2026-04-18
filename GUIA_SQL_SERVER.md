# Guía de Configuración de Base de Datos para el Equipo

Esta guía contiene los pasos necesarios para que cualquier integrante del equipo pueda configurar su instancia local de SQL Server y ejecutar el proyecto sin errores de conexión.

## 1. Configuración de Red (TCP/IP)
Es necesario habilitar las conexiones por red en SQL Server Express para que Java pueda conectarse.

1. Abre el **SQL Server Configuration Manager**.
2. Ve a **SQL Server Network Configuration** > **Protocols for SQLEXPRESS**.
3. Haz clic derecho en **TCP/IP** y selecciona **Habilitar**.
4. Haz doble clic en **TCP/IP** y ve a la pestaña **IP Addresses**.
5. Baja hasta el final a la sección **IPAll**:
   - Deja **TCP Dynamic Ports** vacío (borra cualquier número).
   - En **TCP Port** escribe `1433`.
6. Ve a **SQL Server Services** y selecciona **Reiniciar** en el servicio de *SQL Server (SQLEXPRESS)*.

## 2. Modo de Autenticación
SQL Server debe permitir usuarios con contraseña (no solo de Windows).

1. Abre **SQL Server Management Studio (SSMS)**.
2. Clic derecho en el servidor (arriba del todo) > **Properties** > **Security**.
3. Selecciona **"SQL Server and Windows Authentication mode"**.
4. Reinicia el servicio de SQL Server desde el Configuration Manager.

## 3. Crear Usuario para la Aplicación
Ejecuta el siguiente script en una nueva consulta (New Query) en SSMS:

```sql
-- Crear el login en el servidor
-- Nota: Reemplaza 'TU_PASSWORD_AQUI' por la contraseña acordada internamente
CREATE LOGIN appuser WITH PASSWORD = 'TU_PASSWORD_AQUI';

-- Dar permisos sobre la base de datos del proyecto
USE [sisie]; -- Asegúrate de que tu DB se llame sisie
CREATE USER appuser FOR LOGIN appuser;
ALTER ROLE db_owner ADD MEMBER appuser;
```

## 4. Archivo application.properties
Asegúrate de que tu archivo `src/main/resources/application.properties` (que no se sube a Git por seguridad) tenga los siguientes datos:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=sisie;encrypt=true;trustServerCertificate=true
spring.datasource.username=appuser
spring.datasource.password=TU_PASSWORD_ACORDADO
```

---
*Cualquier duda, consulta con el encargado del módulo de Envíos.*
