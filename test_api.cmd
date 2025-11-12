@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM =============================================================
REM CodexaTeam Backend - Script de pruebas end-to-end (Windows .cmd)
REM Requisitos:
REM  - Backend levantado en http://localhost:8080 (o cambia BASE_URL)
REM  - MySQL accesible como en application.properties
REM  - Windows 10/11 con curl y PowerShell disponibles
REM Uso:
REM  - Abre un CMD, navega al repo y ejecuta: test_api.cmd
REM =============================================================

REM ---------- Configuración ----------
set "BASE_URL=http://localhost:8080"
set "PASSWORD=Passw0rd123"

REM Random para emails únicos
set /a RAND=%RANDOM% + 1000
set "OWNER_EMAIL=owner%RAND%@test.local"
set "RENTER_EMAIL=renter%RAND%@test.local"

call :banner "0) Swagger y Health"
call :check_get "%BASE_URL%/" 200
call :check_get "%BASE_URL%/v3/api-docs" 200
call :check_get "%BASE_URL%/swagger-ui.html" 302

call :banner "1) Registro (sign-up) de usuarios"
echo Registrando ARRRENDADOR: %OWNER_EMAIL%
call :signup "%OWNER_EMAIL%" "arrendador" "Owner One"

echo Registrando ARRENDATARIO: %RENTER_EMAIL%
call :signup "%RENTER_EMAIL%" "arrendatario" "Renter One"

call :banner "2) Login (sign-in) y captura de tokens"
call :signin "%OWNER_EMAIL%" "%PASSWORD%" OWNER_TOKEN OWNER_ID
call :signin "%RENTER_EMAIL%" "%PASSWORD%" RENTER_TOKEN RENTER_ID

if not defined OWNER_TOKEN (
  echo [FAIL] No se obtuvo token para ARRRENDADOR.
  goto :end
)
if not defined RENTER_TOKEN (
  echo [FAIL] No se obtuvo token para ARRENDATARIO.
  goto :end
)

echo Owner ID: !OWNER_ID!  Token (inicio): !OWNER_TOKEN:~0,16!...
echo Renter ID: !RENTER_ID! Token (inicio): !RENTER_TOKEN:~0,16!...

call :banner "3) GET publico de vehiculos (deberia estar vacio al inicio)"
call :get "%BASE_URL%/api/v1/vehicles"

call :banner "4) Arrendador crea un vehiculo"
call :create_vehicle "!OWNER_TOKEN!" VEHICLE_ID
if not defined VEHICLE_ID (
  echo [FAIL] No se obtuvo VEHICLE_ID tras crear vehiculo.
  goto :end
)
echo VEHICLE_ID creado: !VEHICLE_ID!

call :banner "5) Arrendatario crea una reserva para el vehiculo"
call :create_booking "!RENTER_TOKEN!" "!VEHICLE_ID!" BOOKING_ID
if not defined BOOKING_ID (
  echo [FAIL] No se obtuvo BOOKING_ID tras crear reserva.
  goto :end
)
echo BOOKING_ID creado: !BOOKING_ID!

call :banner "6) Arrendador confirma la reserva"
call :put_auth "!OWNER_TOKEN!" "%BASE_URL%/api/v1/bookings/!BOOKING_ID!/confirm" 200

call :banner "7) Arrendador registra telemetria para el vehiculo"
call :record_telemetry "!OWNER_TOKEN!" "!VEHICLE_ID!"

call :banner "8) Arrendatario publica una reseña del vehiculo"
call :post_review "!RENTER_TOKEN!" "!VEHICLE_ID!"

call :banner "9) Endpoints 'my-*' protegidos"
call :get_auth "!OWNER_TOKEN!" "%BASE_URL%/api/v1/vehicles/my-listings"
call :get_auth "!RENTER_TOKEN!" "%BASE_URL%/api/v1/bookings/my-bookings"
call :get_auth "!OWNER_TOKEN!" "%BASE_URL%/api/v1/bookings/my-requests"
call :get_auth "!RENTER_TOKEN!" "%BASE_URL%/api/v1/reviews/my-reviews"

call :banner "10) Reviews publicas por vehiculo"
call :get "%BASE_URL%/api/v1/reviews/vehicle/!VEHICLE_ID!"

echo.
echo =============================================================
echo Pruebas ejecutadas. Revisa los codigos y respuestas mostradas arriba.
echo =============================================================

goto :end

REM ===================== Helpers =====================
:banner
  echo.
  echo ---------------- %~1 ----------------
  exit /b 0

:check_get
  setlocal EnableDelayedExpansion
  set "URL=%~1"
  set "EXPECT=%~2"
  for /f "usebackq delims=" %%s in (`curl -s -o NUL -w "%%{http_code}" "%URL%"`) do set "CODE=%%s"
  if "!CODE!"=="%EXPECT%" (
    echo [PASS] GET %URL% ^(HTTP !CODE!^)
  ) else (
    echo [FAIL] GET %URL% esperado %EXPECT% pero obtuvo !CODE!
  )
  endlocal & exit /b 0

:get
  setlocal EnableDelayedExpansion
  set "URL=%~1"
  echo [GET] %URL%
  curl -s -H "Accept: application/json" "%URL%"
  echo.
  endlocal & exit /b 0

:get_auth
  setlocal EnableDelayedExpansion
  set "TOKEN=%~1"
  set "URL=%~2"
  echo [GET] %URL%
  curl -s -f -H "Accept: application/json" -H "Authorization: Bearer %TOKEN%" "%URL%"
  if errorlevel 1 (
    echo.
    echo [FAIL] Error en GET autenticado.
  )
  echo.
  endlocal & exit /b 0

:signup
  setlocal EnableDelayedExpansion
  set "EMAIL=%~1"
  set "ROLE=%~2"
  set "NAME=%~3"
  set "TMPCODE=%TEMP%\signup_code_%RANDOM%.txt"
  set "TMPOUT=%TEMP%\signup_out_%RANDOM%.json"

  set "BODY={\"name\":\"%NAME%\",\"email\":\"%EMAIL%\",\"password\":\"%PASSWORD%\",\"role\":\"%ROLE%\"}"
  curl -s -o "%TMPOUT%" -w "%%{http_code}" -H "Content-Type: application/json" -d "%BODY%" "%BASE_URL%/api/v1/authentication/sign-up" > "%TMPCODE%"
  set /p CODE=<"%TMPCODE%"
  if "!CODE!"=="201" (
    echo [PASS] sign-up %ROLE% ^(HTTP 201^)
  ) else if "!CODE!"=="200" (
    echo [PASS] sign-up %ROLE% ^(HTTP 200^)
  ) else if "!CODE!"=="409" (
    echo [INFO] sign-up %ROLE% fallo, probablemente el email ya existe ^(HTTP 409^)
  ) else (
    echo [FAIL] sign-up %ROLE% fallo con codigo HTTP !CODE!
    type "%TMPOUT%"
  )
  del /q "%TMPCODE%" 2>nul
  del /q "%TMPOUT%" 2>nul
  endlocal & exit /b 0

:post_json
  setlocal EnableDelayedExpansion
  set "URL=%~1"
  set "BODY=%~2"
  for /f "usebackq delims=" %%s in (`curl -s -o NUL -w "%%{http_code}" -H "Content-Type: application/json" -d "%BODY%" "%URL%"`) do set "CODE=%%s"
  if "!CODE!"=="200" (
    echo [PASS] POST %URL% ^(HTTP 200^)
    goto post_json_end
  )
  if "!CODE!"=="201" (
    echo [PASS] POST %URL% ^(HTTP 201^)
    goto post_json_end
  )
  echo [WARN] POST %URL% devolvio HTTP !CODE! ^(revisa logs/respuesta^)
  echo BODY:
  curl -s -H "Content-Type: application/json" -d "%BODY%" "%URL%"
  echo.
:post_json_end
  endlocal & exit /b 0

:signin
  setlocal EnableDelayedExpansion
  set "EMAIL=%~1"
  set "PASS=%~2"
  set "OUT_TOKEN=%~3"
  set "OUT_ID=%~4"
  set "TMPCODE=%TEMP%\signin_code_%RANDOM%.txt"
  set "TMPJSON=%TEMP%\signin_json_%RANDOM%.json"
  echo [POST] sign-in %EMAIL%

  set "BODY={\"email\":\"%EMAIL%\",\"password\":\"%PASS%\"}"
  curl -s -o "%TMPJSON%" -w "%%{http_code}" -H "Content-Type: application/json" -d "%BODY%" "%BASE_URL%/api/v1/authentication/sign-in" > "%TMPCODE%"
  set /p CODE=<"%TMPCODE%"
  set "TOKEN="
  set "UID="
  if "!CODE!"=="200" (
    for /f "usebackq delims=" %%t in (`powershell.exe -NoProfile -Command "$j = Get-Content -Raw '%TMPJSON%' ^| ConvertFrom-Json; $j.token"`) do set "TOKEN=%%t"
    for /f "usebackq delims=" %%i in (`powershell.exe -NoProfile -Command "$j = Get-Content -Raw '%TMPJSON%' ^| ConvertFrom-Json; $j.id"`) do set "UID=%%i"
    if defined TOKEN (
      echo [PASS] sign-in OK para %EMAIL%
    ) else (
      echo [FAIL] sign-in devolvio 200 pero no se pudo leer token
      type "%TMPJSON%"
    )
  ) else (
    echo [FAIL] sign-in fallo para %EMAIL% con HTTP !CODE!
    type "%TMPJSON%"
  )
  del /q "%TMPCODE%" 2>nul
  del /q "%TMPJSON%" 2>nul
  endlocal & set "%~3=%TOKEN%" & set "%~4=%UID%" & exit /b 0

:create_vehicle
  setlocal EnableDelayedExpansion
  set "TOKEN=%~1"
  set "OUT_ID=%~2"
  echo [POST] Crear vehiculo

  set "VID="
  for /f "usebackq delims=" %%i in (`curl -s -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"brand\":\"Toyota\",\"model\":\"Corolla\",\"year\":2020,\"pricePerDay\":45.0,\"imageUrl\":\"https://example.com/t.jpg\"}" "%BASE_URL%/api/v1/vehicles" ^| powershell.exe -NoProfile -Command "$input ^| ConvertFrom-Json ^| Select-Object -Expand id"`) do set "VID=%%i"

  if defined VID (
    echo [PASS] Vehiculo creado con ID !VID!
  ) else (
    echo [FAIL] No se pudo crear vehiculo.
  )
  endlocal & set "%~2=%VID%" & exit /b 0

:create_booking
  setlocal EnableDelayedExpansion
  set "TOKEN=%~1"
  set "VEH_ID=%~2"
  set "OUT_ID=%~3"
  echo [POST] Crear reserva para vehicleId=%VEH_ID%

  set "BID="
  for /f "usebackq delims=" %%i in (`powershell.exe -NoProfile -Command "$ErrorActionPreference='Stop'; try { $h=@{'Authorization'='Bearer %TOKEN%'}; $start=(Get-Date).AddDays(1).ToString('yyyy-MM-ddTHH:mm:ssZ'); $end=(Get-Date).AddDays(5).ToString('yyyy-MM-ddTHH:mm:ssZ'); $body=@{vehicleId=%VEH_ID%; startDate=$start; endDate=$end} ^| ConvertTo-Json; $resp=Invoke-RestMethod -Method Post -Uri '%BASE_URL%/api/v1/bookings' -ContentType 'application/json' -Headers $h -Body $body; $resp.id; } catch {}"`) do set "BID=%%i"

  if defined BID (
    echo [PASS] Reserva creada con ID !BID!
  ) else (
    echo [FAIL] No se pudo crear reserva ^(verifica fechas y disponibilidad^)
  )
  endlocal & set "%~3=%BID%" & exit /b 0

:put_auth
  setlocal EnableDelayedExpansion
  set "TOKEN=%~1"
  set "URL=%~2"
  set "EXPECT=%~3"
  for /f "usebackq delims=" %%s in (`curl -s -o NUL -w "%%{http_code}" -X PUT -H "Authorization: Bearer %TOKEN%" "%URL%"`) do set "CODE=%%s"
  if "!CODE!"=="%EXPECT%" (
    echo [PASS] PUT %URL% ^(HTTP !CODE!^)
  ) else (
    echo [FAIL] PUT %URL% esperado %EXPECT% pero obtuvo !CODE!
  )
  endlocal & exit /b 0

:record_telemetry
  setlocal EnableDelayedExpansion
  set "TOKEN=%~1"
  set "VEH_ID=%~2"
  echo [POST] Registrar telemetria para vehicleId=%VEH_ID%
  for /f "usebackq delims=" %%s in (`powershell.exe -NoProfile -Command "$ErrorActionPreference='Stop'; $h=@{'Authorization'='Bearer %TOKEN%'}; $r1=Get-Random -Minimum -50 -Maximum 50; $r2=Get-Random -Minimum -50 -Maximum 50; $lat = -12.05 + ($r1/1000.0); $lon = -77.05 + ($r2/1000.0); $speed = Get-Random -Minimum 0 -Maximum 100; $fuel = Get-Random -Minimum 10 -Maximum 100; $body=@{vehicleId=%VEH_ID%; latitude=[double]$lat; longitude=[double]$lon; speed=[double]$speed; fuelLevel=[double]$fuel} ^| ConvertTo-Json; $null = Invoke-RestMethod -Method Post -Uri '%BASE_URL%/api/v1/telemetry' -ContentType 'application/json' -Headers $h -Body $body; 'OK'"`) do set "OK=%%s"
  if /i "!OK!"=="OK" (
    echo [PASS] Telemetria registrada
  ) else (
    echo [FAIL] No se pudo registrar telemetria
  )
  endlocal & exit /b 0

:post_review
  setlocal EnableDelayedExpansion
  set "TOKEN=%~1"
  set "VEH_ID=%~2"
  echo [POST] Crear review para vehicleId=%VEH_ID%
  for /f "usebackq delims=" %%s in (`curl -s -o NUL -w "%%{http_code}" -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"vehicleId\":%VEH_ID%,\"rating\":5,\"comment\":\"Excelente experiencia\"}" "%BASE_URL%/api/v1/reviews"`) do set "CODE=%%s"
  if "!CODE!"=="201" (
    echo [PASS] Review creada ^(HTTP 201^)
  ) else (
    echo [FAIL] Review no creada, HTTP !CODE!
  )
  endlocal & exit /b 0

:end
endlocal
pause
exit /b 0
