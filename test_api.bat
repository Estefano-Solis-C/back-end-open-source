@echo off
REM ========================================
REM Script de Pruebas Automatizado
REM CodexaTeam Backend Platform
REM ========================================

echo.
echo ========================================
echo   CodexaTeam Backend - Test Suite
echo ========================================
echo.

REM Configuración
set BASE_URL=http://localhost:8080
set CONTENT_TYPE=Content-Type: application/json

echo [INFO] Servidor: %BASE_URL%
echo.

REM ========================================
REM PARTE 1: HEALTH CHECK
REM ========================================
echo.
echo [1/16] Verificando Health Check...
curl -s %BASE_URL%/actuator/health
echo.
timeout /t 2 >nul

REM ========================================
REM PARTE 2: REGISTRAR USUARIOS
REM ========================================
echo.
echo [2/16] Registrando Propietario (ARRENDADOR)...
curl -s -X POST %BASE_URL%/api/v1/authentication/sign-up ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"username\":\"carlos.owner@email.com\",\"password\":\"Carlos123!\",\"roles\":[\"arrendador\"]}" > temp_owner.json

REM Extraer token (requiere jq o similar, aquí guardamos el JSON completo)
echo Token del propietario guardado en temp_owner.json
type temp_owner.json
echo.
timeout /t 2 >nul

echo [3/16] Registrando Arrendatario (ARRENDATARIO)...
curl -s -X POST %BASE_URL%/api/v1/authentication/sign-up ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"username\":\"maria.renter@email.com\",\"password\":\"Maria123!\",\"roles\":[\"arrendatario\"]}" > temp_renter.json

echo Token del arrendatario guardado en temp_renter.json
type temp_renter.json
echo.

echo Token del arrendatario guardado en temp_renter.json
echo.
timeout /t 1 >nul

echo.
echo ========================================
echo   INSTRUCCIONES MANUALES
echo ========================================
echo.
echo 1. Abre los archivos temp_owner.json y temp_renter.json
echo 2. Copia los tokens de cada uno
echo 3. Ejecuta manualmente los siguientes comandos con tus tokens:
echo.
echo    SET TOKEN_OWNER=tu_token_aqui
echo    SET TOKEN_RENTER=tu_token_aqui
echo.
echo 4. Luego ejecuta: test_part2.bat
echo.
pause

REM ========================================
REM Limpiar archivos temporales
REM ========================================
REM del temp_owner.json
REM del temp_renter.json

echo.
echo ========================================
echo   Parte 1 Completada
echo ========================================
echo.
echo Para continuar, necesitas configurar las variables TOKEN_OWNER y TOKEN_RENTER
echo Luego ejecuta los siguientes comandos manualmente o crea un segundo script.
echo.
pause

