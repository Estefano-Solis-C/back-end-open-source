# 🎯 Tutorial Completo de Swagger UI - 22 Pasos Funcionales

## 📋 Requisitos Previos

- ✅ Aplicación corriendo en `http://localhost:8080`
- ✅ Base de datos MySQL activa
- ✅ Base de datos limpia (reinicia la app si ya tienes datos)

---

## 🚀 Paso 1: Acceder a Swagger UI

Abre tu navegador y ve a:
```
http://localhost:8080/swagger-ui.html
```

---

## 👤 Paso 2: Registrar al Propietario (Carlos)

### 📍 Endpoint
**POST /api/v1/authentication/sign-up**

### 🔧 Pasos
1. Busca la sección **"authentication-controller"**
2. Click en **"POST /api/v1/authentication/sign-up"**
3. Click en **"Try it out"**
4. Copia y pega este JSON en el Request body:

```json
{
  "email": "carlos.owner@test.com",
## 📖 Paso 22: Ver Reseñas de un Vehículo
  "name": "Carlos Owner",
  "role": "arrendador"
}
```

5. Click en **"Execute"**

### ✅ Respuesta Esperada (201 Created)
```json
{
  "id": 1,
  "email": "carlos.owner@test.com",
  "name": "Carlos Owner",
  "roles": ["ROLE_ARRENDADOR"]
}
```

📝 **Importante:** Anota el `id: 1` de Carlos

---

## 🔐 Paso 3: Iniciar Sesión con Carlos

### 📍 Endpoint
**POST /api/v1/authentication/sign-in**

### 🔧 Pasos
1. En la misma sección **"authentication-controller"**
2. Click en **"POST /api/v1/authentication/sign-in"**
3. Click en **"Try it out"**
4. Copia y pega este JSON:

```json
{
  "email": "carlos.owner@test.com",
  "password": "Carlos123!"
}
```

5. Click en **"Execute"**

### ✅ Respuesta Esperada (200 OK)
```json
{
  "id": 1,
- ⚠️ **Nota:** No existe endpoint para confirmar/rechazar reservas (quedan en PENDING)
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20iLCJpYXQiOjE3MzE0MDMyNTgsImV4cCI6MTczMjAwODA1OH0.abc123..."
}
```

📝 **MUY IMPORTANTE:** Copia SOLO el valor del token (todo lo que está después de `"token": "`):
```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20iLCJpYXQiOjE3MzE0MDMyNTgsImV4cCI6MTczMjAwODA1OH0.abc123...
```

---

## 🔓 Paso 4: Autorizar en Swagger

### 🔧 Pasos
1. Busca el botón **"Authorize"** 🔓 (verde, arriba a la derecha)
2. Click en **"Authorize"**
3. Aparecerá un modal con un campo **"Value"**
4. **⚠️ IMPORTANTE:** Pega SOLO el token (SIN escribir "Bearer"):

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20iLCJpYXQiOjE3MzE0MDMyNTgsImV4cCI6MTczMjAwODA1OH0.abc123...
```

**⛔ NO escribas esto:**
```
Bearer eyJhbGciOiJIUzUxMiJ9...
```

**✅ Solo esto:**
```
eyJhbGciOiJIUzUxMiJ9...
```

5. Click en **"Authorize"** (botón del modal)
6. Click en **"Close"**

✅ **El candado 🔓 ahora debe estar cerrado 🔒**

---

## 🚗 Paso 5: Crear un Vehículo (Carlos)

### 📍 Endpoint
**POST /api/v1/vehicles**

### 🔧 Pasos
1. Busca la sección **"vehicles-controller"**
2. Click en **"POST /api/v1/vehicles"**
3. Click en **"Try it out"**
4. Copia y pega este JSON:

```json
{
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.00,
  "imageUrl": "https://ejemplo.com/toyota-camry.jpg"
}
```

5. Click en **"Execute"**

### ✅ Respuesta Esperada (201 Created)
```json
{
  "id": 1,
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.0,
  "status": "AVAILABLE",
  "imageUrl": "https://ejemplo.com/toyota-camry.jpg",
  "ownerId": 1,
  "createdAt": "2025-11-12T08:30:00.000+00:00"
}
```

📝 **Importante:** Anota el `id: 1` del vehículo

---

## 🚙 Paso 6: Crear Otro Vehículo (Carlos)

### 🔧 Pasos
1. En el mismo endpoint **"POST /api/v1/vehicles"**
2. Ya debe estar en "Try it out"
3. Copia y pega este JSON:

```json
{
  "brand": "Honda",
  "model": "Civic",
  "year": 2024,
  "pricePerDay": 65.00,
  "imageUrl": "https://ejemplo.com/honda-civic.jpg"
}
```

4. Click en **"Execute"**

### ✅ Respuesta Esperada (201 Created)
```json
{
  "id": 2,
  "brand": "Honda",
  "model": "Civic",
  "year": 2024,
  "pricePerDay": 65.0,
  "status": "AVAILABLE",
  "imageUrl": "https://ejemplo.com/honda-civic.jpg",
  "ownerId": 1,
  "createdAt": "2025-11-12T08:31:00.000+00:00"
}
```

---

## 👥 Paso 7: Registrar a la Arrendataria (María)

### 📍 Endpoint
**POST /api/v1/authentication/sign-up**

### 🔧 Pasos
1. Vuelve a **"authentication-controller"**
2. Click en **"POST /api/v1/authentication/sign-up"**
3. Click en **"Try it out"**
4. Copia y pega este JSON:

```json
{
  "email": "maria.renter@test.com",
  "password": "Maria123!",
  "name": "Maria Renter",
  "role": "arrendatario"
}
```

5. Click en **"Execute"**

### ✅ Respuesta Esperada (201 Created)
```json
{
  "id": 2,
  "email": "maria.renter@test.com",
  "name": "Maria Renter",
  "roles": ["ROLE_ARRENDATARIO"]
}
```

📝 **Importante:** Anota el `id: 2` de María

---

## 🔐 Paso 8: Iniciar Sesión con María

### 🔧 Pasos
1. Click en **"POST /api/v1/authentication/sign-in"**
2. Click en **"Try it out"**
3. Copia y pega este JSON:

```json
{
  "email": "maria.renter@test.com",
  "password": "Maria123!"
}
```

4. Click en **"Execute"**

### ✅ Respuesta Esperada (200 OK)
```json
{
  "id": 2,
  "email": "maria.renter@test.com",
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYXJpYS5yZW50ZXJAZGV0ZXN0LmNvbSIsImlhdCI6MTczMTQwMzM1OCwiZXhwIjoxNzMyMDA4MTU4fQ.xyz789..."
}
```

📝 **Importante:** Copia el token de María

---

## 🔄 Paso 9: Cambiar Autorización a María

### 🔧 Pasos
1. Click en **"Authorize"** 🔒
2. Click en **"Logout"**
3. Pega SOLO el token de María (sin "Bearer"):
```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYXJpYS5yZW50ZXJAZGV0ZXN0LmNvbSIsImlhdCI6MTczMTQwMzM1OCwiZXhwIjoxNzMyMDA4MTU4fQ.xyz789...
```
4. Click en **"Authorize"**
5. Click en **"Close"**

✅ **Ahora estás autenticado como María**

---

## 🔍 Paso 10: Buscar Vehículos Disponibles (María)

### 📍 Endpoint
**GET /api/v1/vehicles**

### 🔧 Pasos
1. Busca la sección **"vehicles-controller"**
2. Click en **"GET /api/v1/vehicles"**
3. Click en **"Try it out"**
4. Click en **"Execute"** (sin modificar nada)

### ✅ Respuesta Esperada (200 OK)
```json
[
  {
    "id": 1,
    "brand": "Toyota",
    "model": "Camry",
    "year": 2023,
    "pricePerDay": 75.0,
    "status": "AVAILABLE",
    "imageUrl": "https://ejemplo.com/toyota-camry.jpg",
    "ownerId": 1,
    "createdAt": "2025-11-12T08:30:00.000+00:00"
  },
  {
    "id": 2,
    "brand": "Honda",
    "model": "Civic",
    "year": 2024,
    "pricePerDay": 65.0,
    "status": "AVAILABLE",
    "imageUrl": "https://ejemplo.com/honda-civic.jpg",
    "ownerId": 1,
    "createdAt": "2025-11-12T08:31:00.000+00:00"
  }
]
```

---

## 📅 Paso 11: Crear una Reserva (María)

### 📍 Endpoint
**POST /api/v1/bookings**

### 🔧 Pasos
1. Busca la sección **"bookings-controller"**
2. Click en **"POST /api/v1/bookings"**
3. Click en **"Try it out"**
4. **⚠️ IMPORTANTE:** Usa fechas que incluyan HOY (2025-11-12):

```json
{
  "vehicleId": 1,
  "startDate": "2025-11-10",
  "endDate": "2025-11-15"
}
```

**Nota:** La fecha actual (2025-11-12) está entre el 10 y el 15, así que la reserva está ACTIVA.

5. Click en **"Execute"**

### ✅ Respuesta Esperada (201 Created)
```json
{
  "id": 1,
  "vehicleId": 1,
  "renterId": 2,
  "ownerId": 1,
  "startDate": "2025-11-10",
  "endDate": "2025-11-15",
  "totalPrice": 450.0,
  "status": "PENDING",
  "createdAt": "2025-11-12T08:35:00.000+00:00"
}
```

📝 **Importante:** 
- Anota el `id: 1` de la reserva
- Cálculo: 6 días × $75 = $450
- El status es "PENDING" porque Carlos aún no la aprueba

---

## 📋 Paso 12: Ver Mis Reservas (María)

### 📍 Endpoint
**GET /api/v1/bookings/my-bookings**

### 🔧 Pasos
1. En la sección **"bookings-controller"**
2. Click en **"GET /api/v1/bookings/my-bookings"**
3. Click en **"Try it out"**
4. Click en **"Execute"**

### ✅ Respuesta Esperada (200 OK)
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "ownerId": 1,
    "startDate": "2025-11-10",
    "endDate": "2025-11-15",
    "totalPrice": 450.0,
    "status": "PENDING",
    "createdAt": "2025-11-12T08:35:00.000+00:00"
  }
]
```

---

## 🔄 Paso 13: Volver a Autenticarse como Carlos

### 🔧 Pasos
1. Click en **"Authorize"** 🔒
2. Click en **"Logout"**
3. Ve a **"POST /api/v1/authentication/sign-in"**
4. Inicia sesión con Carlos:

```json
{
  "email": "carlos.owner@test.com",
  "password": "Carlos123!"
}
```

5. Copia el token de Carlos
6. Click en **"Authorize"** 🔓
7. Pega SOLO el token (sin "Bearer")
8. Click en **"Authorize"** y **"Close"**

---

## ✅ Paso 14: Ver Solicitudes de Reserva (Carlos)

### 📍 Endpoint
**GET /api/v1/bookings/my-requests**

### 🔧 Pasos
1. En la sección **"bookings-controller"**
2. Click en **"GET /api/v1/bookings/my-requests"**
3. Click en **"Try it out"**
4. Click en **"Execute"**

### ✅ Respuesta Esperada (200 OK)
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "ownerId": 1,
    "startDate": "2025-11-10",
    "endDate": "2025-11-15",
    "totalPrice": 450.0,
    "status": "PENDING",
    "createdAt": "2025-11-12T08:35:00.000+00:00"
  }
]
```

---

## ✅ Paso 15: Aprobar la Reserva (Carlos)

### 📍 Endpoint
**PUT /api/v1/bookings/{bookingId}/confirm**

### 🔧 Pasos
1. En la sección **"bookings-controller"**
2. Click en **"PUT /api/v1/bookings/{bookingId}/confirm"**
3. Click en **"Try it out"**
4. En el campo **bookingId**, escribe: `1`
5. Click en **"Execute"**

### ✅ Respuesta Esperada (200 OK)
```json
{
  "id": 1,
  "vehicleId": 1,
  "renterId": 2,
  "ownerId": 1,
  "startDate": "2025-11-10",
  "endDate": "2025-11-15",
  "totalPrice": 450.0,
  "status": "CONFIRMED",
  "createdAt": "2025-11-12T08:35:00.000+00:00"
}
```

📝 **Nota:** El status cambió de "PENDING" a "CONFIRMED"

### 💡 Endpoint Adicional

También puedes **rechazar** una reserva con:
**PUT /api/v1/bookings/{bookingId}/reject**

Esto cambiará el status a "REJECTED".

---

## 📍 Paso 16: Registrar Telemetría (Carlos)

### 📍 Endpoint
**POST /api/v1/telemetry**

### 🔧 Pasos
1. Busca la sección **"telemetry-controller"**
2. Click en **"POST /api/v1/telemetry"**
3. Click en **"Try it out"**
4. Copia y pega este JSON:

```json
{
  "vehicleId": 1,
  "latitude": -12.0464,
  "longitude": -77.0428,
  "speed": 65.5,
  "fuelLevel": 80.0
}
```

5. Click en **"Execute"**

### ✅ Respuesta Esperada (201 Created)
```json
{
  "id": 1,
  "vehicleId": 1,
  "latitude": -12.0464,
  "longitude": -77.0428,
  "speed": 65.5,
  "fuelLevel": 80.0,
  "timestamp": "2025-11-12T08:40:00.000+00:00"
}
```

---

## 🗺️ Paso 17: Registrar Más Telemetría (Carlos)

### 🔧 Pasos
Repite el paso anterior con estos datos:

**Telemetría 2:**
```json
{
  "vehicleId": 1,
  "latitude": -12.0500,
  "longitude": -77.0450,
  "speed": 72.0,
  "fuelLevel": 78.5
}
```

**Telemetría 3:**
```json
{
  "vehicleId": 1,
  "latitude": -12.0550,
  "longitude": -77.0480,
  "speed": 68.3,
  "fuelLevel": 76.0
}
```

---

## 👁️ Paso 18: Ver Telemetría del Vehículo (Carlos)

### 📍 Endpoint
**GET /api/v1/telemetry/vehicle/{vehicleId}**

### 🔧 Pasos
1. En la sección **"telemetry-controller"**
2. Click en **"GET /api/v1/telemetry/vehicle/{vehicleId}"**
3. Click en **"Try it out"**
4. En el campo **vehicleId**, escribe: `1`
5. Click en **"Execute"**

### ✅ Respuesta Esperada (200 OK)
```json
[
  {
    "id": 3,
    "vehicleId": 1,
    "latitude": -12.0550,
    "longitude": -77.0480,
    "speed": 68.3,
    "fuelLevel": 76.0,
    "timestamp": "2025-11-12T08:42:00.000+00:00"
  },
  {
    "id": 2,
    "vehicleId": 1,
    "latitude": -12.0500,
    "longitude": -77.0450,
    "speed": 72.0,
    "fuelLevel": 78.5,
    "timestamp": "2025-11-12T08:41:00.000+00:00"
  },
  {
    "id": 1,
    "vehicleId": 1,
    "latitude": -12.0464,
    "longitude": -77.0428,
    "speed": 65.5,
    "fuelLevel": 80.0,
    "timestamp": "2025-11-12T08:40:00.000+00:00"
  }
]
```

📝 **Nota:** Los datos están ordenados del más reciente al más antiguo

---

## 🔄 Paso 19: Cambiar a María para Ver Telemetría

### 🔧 Pasos
1. Click en **"Authorize"** 🔒
2. Click en **"Logout"**
3. Inicia sesión con María:

```json
{
  "email": "maria.renter@test.com",
  "password": "Maria123!"
}
```

4. Copia el token de María
5. Click en **"Authorize"** 🔓
6. Pega SOLO el token (sin "Bearer")
7. Click en **"Authorize"** y **"Close"**

---

## 👁️ Paso 20: Ver Telemetría del Vehículo Reservado (María)

### 📍 Endpoint
**GET /api/v1/telemetry/vehicle/{vehicleId}**

### 🔧 Pasos
1. En la sección **"telemetry-controller"**
2. Click en **"GET /api/v1/telemetry/vehicle/{vehicleId}"**
3. Click en **"Try it out"**
4. **⚠️ IMPORTANTE:** En el campo **vehicleId**, escribe: `1` (el vehículo que reservó)
5. Click en **"Execute"**

### ✅ Respuesta Esperada (200 OK)
```json
[
  {
    "id": 3,
    "vehicleId": 1,
    "latitude": -12.0550,
    "longitude": -77.0480,
    "speed": 68.3,
    "fuelLevel": 76.0,
    "timestamp": "2025-11-12T08:42:00.000+00:00"
  },
  {
    "id": 2,
    "vehicleId": 1,
    "latitude": -12.0500,
    "longitude": -77.0450,
    "speed": 72.0,
    "fuelLevel": 78.5,
    "timestamp": "2025-11-12T08:41:00.000+00:00"
  },
  {
    "id": 1,
    "vehicleId": 1,
    "latitude": -12.0464,
    "longitude": -77.0428,
    "speed": 65.5,
    "fuelLevel": 80.0,
    "timestamp": "2025-11-12T08:40:00.000+00:00"
  }
]
```

✅ **¡María puede ver la telemetría porque tiene una reserva ACTIVA para este vehículo!**

---

## ⭐ Paso 21: Crear una Reseña (María)

### 📍 Endpoint
**POST /api/v1/reviews**

### 🔧 Pasos
1. Busca la sección **"reviews-controller"**
2. Click en **"POST /api/v1/reviews"**
3. Click en **"Try it out"**
4. Copia y pega este JSON:

```json
{
  "vehicleId": 1,
  "rating": 5,
  "comment": "Excelente vehículo, muy cómodo y en perfectas condiciones. Carlos fue muy amable y puntual."
}
```

5. Click en **"Execute"**

### ✅ Respuesta Esperada (201 Created)
```json
{
  "id": 1,
  "vehicleId": 1,
  "userId": 2,
  "userName": "Maria Renter",
  "rating": 5,
  "comment": "Excelente vehículo, muy cómodo y en perfectas condiciones. Carlos fue muy amable y puntual.",
  "createdAt": "2025-11-12T08:45:00.000+00:00"
}
```

---

## 📖 Paso 22: Ver Reseñas de un Vehículo

### 📍 Endpoint
**GET /api/v1/reviews/vehicle/{vehicleId}**

### 🔧 Pasos
1. En la sección **"reviews-controller"**
2. Click en **"GET /api/v1/reviews/vehicle/{vehicleId}"**
3. Click en **"Try it out"**
4. En el campo **vehicleId**, escribe: `1`
5. Click en **"Execute"**

### ✅ Respuesta Esperada (200 OK)
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "userId": 2,
    "userName": "Maria Renter",
    "rating": 5,
    "comment": "Excelente vehículo, muy cómodo y en perfectas condiciones. Carlos fue muy amable y puntual.",
    "createdAt": "2025-11-12T08:45:00.000+00:00"
  }
]
```

---

## 🎉 ¡TUTORIAL COMPLETADO!

Has probado exitosamente:

✅ **Autenticación:**
- Registro de usuarios (ARRENDADOR y ARRENDATARIO)
- Inicio de sesión y obtención de tokens JWT
- Autorización en Swagger UI

✅ **Gestión de Vehículos:**
- Creación de vehículos (solo propietarios)
- Listado de vehículos disponibles

✅ **Sistema de Reservas:**
- Creación de reservas (arrendatarios)
- Visualización de reservas propias
- Visualización de solicitudes (propietarios)
- Confirmación de reservas (propietarios)

✅ **Telemetría IoT:**
- Registro de datos GPS (propietarios)
- Visualización de telemetría (propietarios y arrendatarios con reserva activa)

✅ **Sistema de Reseñas:**
- Creación de reseñas (arrendatarios)
- Visualización de reseñas de vehículos

---

## 📝 Notas Importantes

### ⚠️ Errores Comunes y Soluciones

#### 1. Error 401 Unauthorized
**Causa:** Token mal configurado
**Solución:** 
- NO escribas "Bearer" en el campo de autorización
- Pega SOLO el token: `eyJhbGci...`

#### 2. Error "You are not authorized to view tracking data"
**Causa:** Intentas ver telemetría de un vehículo que no te corresponde
**Solución:**
- Verifica que usas el **vehicleId correcto** (el que reservaste)
- Verifica que la **fecha actual esté dentro** del período de reserva
- Verifica que la reserva esté en estado **PENDING** o **CONFIRMED**

#### 3. Error "Email already exists"
**Causa:** Los datos de prueba ya existen
**Solución:**
- Reinicia la aplicación (Ctrl+C y luego `mvnw.cmd spring-boot:run`)
- O ejecuta `reset_database.bat`

#### 4. Error "Vehicle is not available"
**Causa:** El vehículo ya tiene una reserva activa
**Solución:**
- Usa otro vehículo
- O espera a que termine la reserva actual

---

## 💡 Tips Adicionales

### 🔄 Cambio Rápido de Usuarios

Guarda los tokens en un archivo de texto:
```
Token Carlos: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20i...
Token María: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYXJpYS5yZW50ZXJAZGV0ZXN0LmNvbS4...
```

### 📅 Fechas de Reserva

Para poder ver telemetría, la reserva debe:
- Estar en estado **PENDING** o **CONFIRMED**
- La fecha actual debe estar **entre startDate y endDate**

**Ejemplo válido para hoy (2025-11-12):**
```json
{
  "startDate": "2025-11-10",
  "endDate": "2025-11-15"
}
```

### 🔍 Filtros de Búsqueda

El endpoint `GET /api/v1/vehicles` acepta parámetros opcionales:
- `brand`: Filtrar por marca
- `minPrice`: Precio mínimo
- `maxPrice`: Precio máximo
- `status`: Estado del vehículo

**Ejemplo:**
```
GET /api/v1/vehicles?brand=Toyota&maxPrice=80
```

---

## 📚 Referencias

- 📘 **Guía completa de Swagger:** `SWAGGER_GUIDE.md`
- 📗 **Inicio rápido:** `QUICK_START.md`
- 📙 **Reset de base de datos:** `DATABASE_RESET_GUIDE.md`
- 📕 **Documentación principal:** `README.md`

---

## 🎯 Próximos Pasos

Ahora que conoces toda la funcionalidad, puedes:

1. **Probar más escenarios:**
   - Crear múltiples reservas
   - Agregar más reseñas
   - Registrar más datos de telemetría

2. **Explorar otros endpoints:**
   - Actualizar vehículos
   - Eliminar vehículos
   - Ver perfil de usuario

3. **Desarrollar tu frontend:**
   - Usa estos mismos endpoints
   - Implementa la lógica de autorización
   - Muestra la telemetría en un mapa

4. **Implementar funcionalidad faltante:**
   - Agregar endpoints para confirmar/rechazar reservas
   - Agregar endpoints para cancelar reservas
   - Agregar notificaciones

¡Feliz desarrollo! 🚀

