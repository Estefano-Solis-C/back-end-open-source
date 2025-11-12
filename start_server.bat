@echo off
echo ========================================
echo   CodexaTeam Backend - Starting Server
echo ========================================
echo.
echo Server will start on http://localhost:8080
echo Swagger UI: http://localhost:8080/swagger-ui/index.html
echo.
echo Logs configurados para produccion (limpios)
echo - SQL queries: Ocultas
echo - Security warnings: Suprimidos
echo - JVM warnings: Suprimidos
echo.
echo Consulta LOGGING_CONFIGURATION_GUIDE.md para mas info
echo.
echo Press Ctrl+C to stop the server
echo.

REM Suprimir warnings de sun.misc.Unsafe
set MAVEN_OPTS=--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED

mvnw.cmd spring-boot:run -DskipTests

