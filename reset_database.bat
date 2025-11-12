@echo off
REM Script para resetear la base de datos manualmente
REM Autor: CodexaTeam
REM Fecha: 2025-11-12

echo ================================================
echo   RESET DE BASE DE DATOS - CODEXATEAM BACKEND
echo ================================================
echo.

echo [ADVERTENCIA] Este script borrara TODOS los datos de la base de datos.
echo.
set /p confirm="¿Estas seguro? (S/N): "

if /i "%confirm%" neq "S" (
    echo.
    echo [CANCELADO] Operacion cancelada por el usuario.
    pause
    exit /b 0
)

echo.
echo [INFO] Conectando a MySQL...
echo.

REM Crear archivo temporal con comandos SQL
echo USE renticar_db; > temp_reset.sql
echo DROP TABLE IF EXISTS user_roles; >> temp_reset.sql
echo DROP TABLE IF EXISTS telemetry; >> temp_reset.sql
echo DROP TABLE IF EXISTS reviews; >> temp_reset.sql
echo DROP TABLE IF EXISTS bookings; >> temp_reset.sql
echo DROP TABLE IF EXISTS vehicles; >> temp_reset.sql
echo DROP TABLE IF EXISTS users; >> temp_reset.sql
echo DROP TABLE IF EXISTS roles; >> temp_reset.sql
echo SHOW TABLES; >> temp_reset.sql

REM Ejecutar comandos SQL
mysql -u root -padmin < temp_reset.sql

REM Borrar archivo temporal
del temp_reset.sql

echo.
echo ================================================
echo [EXITO] Base de datos reseteada correctamente
echo ================================================
echo.
echo Las tablas se recrearan automaticamente al iniciar la aplicacion.
echo.
echo Proximos pasos:
echo   1. Reinicia la aplicacion Spring Boot
echo   2. Las tablas se crearan automaticamente
echo   3. Puedes empezar a usar Swagger desde cero
echo.

pause

