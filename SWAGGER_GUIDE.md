# 📘 Guía Completa de Swagger UI - CodexaTeam Backend

## 🌐 Acceso a Swagger UI

Una vez que el servidor esté corriendo, abre tu navegador:

```
http://localhost:8080/swagger-ui.html
```

---

## ⚠️ IMPORTANTE: Formato del Token en Swagger

**Cuando autorices en Swagger UI:**
- ✅ **CORRECTO:** Pega SOLO el token → `eyJhbGciOiJIUzUxMiJ9...`
- ❌ **INCORRECTO:** NO escribas "Bearer" → `Bearer eyJhbGciOiJIUzUxMiJ9...`

**Swagger agrega automáticamente el prefijo "Bearer"**. Si lo escribes tú también, quedará duplicado (`Bearer Bearer ...`) y causará errores 401 Unauthorized.

---

## 🎯 Tutorial Paso a Paso

### **1. Registrar un Propietario (ARRENDADOR)**

#### 📍 Ubicación en Swagger
- Sección: **authentication-controller**
- Endpoint: **POST /api/v1/authentication/sign-up**

#### 🔧 Pasos
1. Click en el endpoint para expandirlo
2. Click en **"Try it out"** (botón azul a la derecha)
3. En el cuadro de texto "Request body", **borra** todo el contenido
4. **Copia y pega** exactamente este JSON:

```json
{
  "name": "Carlos Pérez",
  "email": "carlos.owner@test.com",
  "password": "Carlos123!",
  "role": "arrendador"
}
```

5. Click en **"Execute"** (botón azul grande abajo)
6. Verás la respuesta en la sección **"Response body"**:

```json
{
  "id": 1,
  "name": "Carlos Pérez",
  "email": "carlos.owner@test.com",
  "roles": [
    "ROLE_ARRENDADOR"
  ]
}
```

✅ **¡Usuario propietario creado exitosamente!**

---

### **2. Iniciar Sesión como Propietario**

#### 📍 Ubicación en Swagger
- Sección: **authentication-controller**
- Endpoint: **POST /api/v1/authentication/sign-in**

#### 🔧 Pasos
1. Click en el endpoint **POST /api/v1/authentication/sign-in**
2. Click en **"Try it out"**
3. Copia y pega este JSON:

```json
{
  "email": "carlos.owner@test.com",
  "password": "Carlos123!"
}
```

4. Click en **"Execute"**
5. En la respuesta, verás algo como:

```json
{
  "id": 1,
  "email": "carlos.owner@test.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20iLCJpYXQiOjE2OTk4MDAwMDAsImV4cCI6MTcwMDQwNDgwMH0.xyz123abc456..."
}
```

6. **📝 IMPORTANTE:** Selecciona y copia TODO el valor de `"token"` (es muy largo, empieza con `eyJ...`)

---

### **3. Autorizar Swagger con el Token**

Ahora que tienes el token, debes autenticarte en Swagger para poder usar los endpoints protegidos.

#### 🔧 Pasos
1. Busca el botón **"Authorize"** 🔓 en la parte superior derecha de Swagger UI (es verde)
2. Click en **"Authorize"**
3. Aparecerá un modal con un campo de texto que dice **"Value"**
4. **⚠️ IMPORTANTE:** En el campo **"Value"**, pega SOLO el token (SIN escribir "Bearer"):
   ```
   eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20iLCJpYXQiOjE2OTk4MDAwMDAsImV4cCI6MTcwMDQwNDgwMH0.xyz123abc456...
   ```
   
   ⛔ **NO escribas "Bearer"** - Swagger lo agrega automáticamente
   
   ✅ **CORRECTO:** `eyJhbGciOiJIUzUxMiJ9...`
   
   ❌ **INCORRECTO:** `Bearer eyJhbGciOiJIUzUxMiJ9...`

5. Click en **"Authorize"** (el botón del modal)
6. Click en **"Close"**

✅ **Ahora el botón 🔓 debería cambiar a un candado cerrado 🔒**

✅ **¡Ahora puedes acceder a todos los endpoints protegidos!**

---

### **4. Crear un Vehículo**

Ahora que estás autenticado, vamos a crear un vehículo.

#### 📍 Ubicación en Swagger
- Sección: **vehicles-controller**
- Endpoint: **POST /api/v1/vehicles**

#### 🔧 Pasos
1. Baja hasta **vehicles-controller**
2. Click en **POST /api/v1/vehicles**
3. Click en **"Try it out"**
4. Copia y pega este JSON:

```json
{
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.00,
  "location": "Lima, Miraflores",
  "description": "Auto ejecutivo en excelente estado, ideal para viajes de negocios. Incluye GPS y seguro completo.",
  "imageUrl": "https://www.toyota.com/imgix/responsive/images/mlp/colorizer/2023/camry/1J9/1.png",
  "available": true
}
```

5. Click en **"Execute"**
6. Verás una respuesta **201 Created** con el vehículo creado:

```json
{
  "id": 1,
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.0,
  "status": "available",
  "imageUrl": "https://www.toyota.com/imgix/responsive/images/mlp/colorizer/2023/camry/1J9/1.png",
  "ownerId": 1,
  "createdAt": "2025-11-12T..."
}
```

✅ **¡Vehículo creado exitosamente!**

---

### **5. Ver el Catálogo de Vehículos (Público)**

Este endpoint NO requiere autenticación.

#### 📍 Ubicación en Swagger
- Sección: **vehicles-controller**
- Endpoint: **GET /api/v1/vehicles**

#### 🔧 Pasos
1. Click en **GET /api/v1/vehicles**
2. Click en **"Try it out"**
3. Click en **"Execute"** (no necesitas enviar nada)
4. Verás una lista con todos los vehículos disponibles:

```json
[
  {
    "id": 1,
    "brand": "Toyota",
    "model": "Camry",
    "year": 2023,
    "pricePerDay": 75.0,
    "status": "available",
    "ownerId": 1
  }
]
```

---

### **6. Ver Mis Vehículos Publicados**

#### 📍 Ubicación en Swagger
- Sección: **vehicles-controller**
- Endpoint: **GET /api/v1/vehicles/my-listings**

#### 🔧 Pasos
1. Click en **GET /api/v1/vehicles/my-listings**
2. Click en **"Try it out"**
3. Click en **"Execute"**
4. Verás solo TUS vehículos (los que creaste con tu cuenta)

---

### **7. Registrar un Arrendatario**

Ahora vamos a crear un segundo usuario para probar el flujo de reservas.

#### 🔧 Pasos
1. Ve a **authentication-controller**
2. **POST /api/v1/authentication/sign-up**
3. Click en **"Try it out"**
4. Copia y pega este JSON:

```json
{
  "name": "María García",
  "email": "maria.renter@test.com",
  "password": "Maria123!",
  "role": "arrendatario"
}
```

5. Click en **"Execute"**
6. Usuario arrendatario creado ✅

---

### **8. Iniciar Sesión como Arrendatario**

#### 🔧 Pasos
1. **POST /api/v1/authentication/sign-in**
2. Click en **"Try it out"**
3. Usa este JSON:

```json
{
  "email": "maria.renter@test.com",
  "password": "Maria123!"
}
```

4. Click en **"Execute"**
5. **Copia el nuevo token** de María

---

### **9. Cambiar la Autorización a María**

Para usar los endpoints como María, necesitas cambiar el token:

#### 🔧 Pasos
1. Click en **"Authorize"** 🔒 (ahora está cerrado)
2. Click en **"Logout"**
3. Pega SOLO el token de María (sin escribir "Bearer"): `eyJhbGciOiJIUzUxMiJ9...`
4. Click en **"Authorize"**
5. Click en **"Close"**

✅ **Ahora estás autenticado como María**

---

### **10. Crear una Reserva**

María va a reservar el Toyota Camry de Carlos.

#### 📍 Ubicación en Swagger
- Sección: **bookings-controller**
- Endpoint: **POST /api/v1/bookings**

#### 🔧 Pasos
1. Click en **POST /api/v1/bookings**
2. Click en **"Try it out"**
3. **⚠️ IMPORTANTE:** Usa fechas que incluyan el día de hoy para que puedas ver la telemetría. Copia y pega este JSON:

```json
{
  "vehicleId": 1,
  "startDate": "2025-11-10",
  "endDate": "2025-11-15"
}
```

4. Click en **"Execute"**
5. Verás la respuesta **201 Created**:

```json
{
  "id": 1,
  "vehicleId": 1,
  "renterId": 2,
  "ownerId": 1,
  "startDate": "2025-11-10",
  "endDate": "2025-11-15",
  "totalPrice": 375.0,
  "status": "PENDING",
  "createdAt": "2025-11-12T..."
}
```

✅ **Reserva creada por 5 días: 5 × $75 = $375**

📝 **Nota:** La reserva incluye la fecha actual (2025-11-12), por lo que María podrá ver la telemetría del vehículo en tiempo real.

---

### **11. Ver Mis Reservas (María)**

#### 📍 Ubicación en Swagger
- Sección: **bookings-controller**
- Endpoint: **GET /api/v1/bookings/my-bookings**

#### 🔧 Pasos
1. Click en **GET /api/v1/bookings/my-bookings**
2. Click en **"Try it out"**
3. Click en **"Execute"**
4. Verás la lista de reservas de María

---

### **12. Ver Solicitudes de Reserva (Carlos)**

Ahora vamos a volver a Carlos para que vea las solicitudes.

#### 🔧 Pasos
1. Click en **"Authorize"** 🔒
2. Click en **"Logout"**
3. Pega SOLO el token de Carlos (sin escribir "Bearer"): `eyJhbGciOiJIUzUxMiJ9...`
4. Click en **"Authorize"** y **"Close"**
5. Ve a **bookings-controller**
6. **GET /api/v1/bookings/my-requests**
7. Click en **"Try it out"** y **"Execute"**
8. Verás las solicitudes de reserva para los vehículos de Carlos

---

### **13. María Deja una Reseña**

Vuelve a autenticarte como María (repite pasos de cambio de token).

#### 📍 Ubicación en Swagger
- Sección: **reviews-controller**
- Endpoint: **POST /api/v1/reviews**

#### 🔧 Pasos
1. Click en **POST /api/v1/reviews**
2. Click en **"Try it out"**
3. Copia y pega este JSON:

```json
{
  "vehicleId": 1,
  "rating": 5,
  "comment": "Excelente vehículo! Muy cómodo y en perfecto estado. Carlos fue muy amable y todo el proceso fue súper fácil. Lo recomiendo 100%."
}
```

4. Click en **"Execute"**
5. Reseña creada ✅

---

### **14. Ver Reseñas del Vehículo (Público)**

#### 📍 Ubicación en Swagger
- Sección: **reviews-controller**
- Endpoint: **GET /api/v1/reviews/vehicle/{vehicleId}**

#### 🔧 Pasos
1. Click en **GET /api/v1/reviews/vehicle/{vehicleId}**
2. Click en **"Try it out"**
3. En el campo **vehicleId**, escribe: `1`
4. Click en **"Execute"**
5. Verás todas las reseñas del vehículo

---

### **15. Carlos Registra Telemetría**

Vuelve a autenticarte como Carlos.

#### 📍 Ubicación en Swagger
- Sección: **telemetry-controller**
- Endpoint: **POST /api/v1/telemetry**

#### 🔧 Pasos
1. Click en **POST /api/v1/telemetry**
2. Click en **"Try it out"**
3. Copia y pega este JSON:

```json
{
  "vehicleId": 1,
  "latitude": -12.0464,
  "longitude": -77.0428,
  "speed": 65.5,
  "fuelLevel": 80.0
}
```

4. Click en **"Execute"**
5. Telemetría registrada ✅

---

### **16. María Ve el Tracking del Vehículo**

Vuelve a autenticarte como María. Ella puede ver el tracking porque tiene una reserva activa.

#### 📍 Ubicación en Swagger
- Sección: **telemetry-controller**
- Endpoint: **GET /api/v1/telemetry/vehicle/{vehicleId}**

#### 🔧 Pasos
1. Click en **GET /api/v1/telemetry/vehicle/{vehicleId}**
2. Click en **"Try it out"**
3. **⚠️ MUY IMPORTANTE:** En el campo **vehicleId**, escribe: `1` (el mismo ID del vehículo que reservaste)
4. Click en **"Execute"**
5. Verás los datos de telemetría del vehículo

#### ⚠️ Nota de Seguridad

María solo puede ver telemetría si cumple **TODAS** estas condiciones:
- Tiene una reserva para ese vehículo específico
- La reserva está en estado PENDING o CONFIRMED
- **La fecha actual está dentro del período de la reserva** (entre startDate y endDate)

Si ves un error **"You are not authorized to view tracking data for this vehicle"**, verifica:
- ✅ Estás usando el `vehicleId` correcto (el de tu reserva)
- ✅ La fecha actual está entre las fechas de tu reserva
- ✅ Estás autenticado como María (no como Carlos)

---

## 📊 Resumen de Datos Creados

| Entidad | ID | Descripción |
|---------|----|-|
| **Usuario 1** | 1 | Carlos (ARRENDADOR) |
| **Usuario 2** | 2 | María (ARRENDATARIO) |
| **Vehículo 1** | 1 | Toyota Camry 2023 ($75/día) |
| **Reserva 1** | 1 | María reserva el Camry (5 días = $375) |
| **Reseña 1** | 1 | María da 5 estrellas al Camry |
| **Telemetría 1** | 1 | GPS: Lima (-12.0464, -77.0428) |

---

## 🎯 Pruebas de Seguridad

### Probar Acceso No Autorizado

#### ❌ Intentar Crear Vehículo con Rol Incorrecto

1. Autentícate como **María** (ARRENDATARIO)
2. Intenta **POST /api/v1/vehicles**
3. Verás **403 Forbidden**
4. ✅ La seguridad funciona correctamente

---

#### ❌ Intentar Acceder sin Token

1. Click en **"Authorize"** 🔒
2. Click en **"Logout"**
3. Click en **"Close"**
4. Intenta **GET /api/v1/vehicles/my-listings**
5. Verás **401 Unauthorized**
6. ✅ La seguridad funciona correctamente

---

## 💡 Tips y Trucos

### 🔄 Cambiar Rápido Entre Usuarios

Guarda los tokens en un archivo de texto (sin escribir "Bearer"):
```
Token Carlos: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20i...
Token María: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtYXJpYS5yZW50ZXJAZGV0ZXN0LmNvbS4...
```

Así puedes copiarlos rápidamente cuando necesites cambiar de usuario en el botón **"Authorize"** de Swagger.

---

### 📋 Copiar Respuestas

Click en el botón **"Download"** debajo de cualquier respuesta para guardarla como archivo.

---

### 🔍 Ver el Request Completo

En cada respuesta, expande **"Request headers"** y **"Request body"** para ver exactamente qué se envió.

---

### 🎨 Cambiar Tema

Swagger UI se adapta al tema oscuro/claro de tu navegador automáticamente.

---

## ❓ Preguntas Frecuentes

### ¿Por qué dice "unauthorized" si me autentiqué?

- ⚠️ **Causa más común:** Escribiste "Bearer" en el campo de autorización de Swagger
  - ❌ INCORRECTO: `Bearer eyJhbGci...`
  - ✅ CORRECTO: `eyJhbGci...` (Swagger agrega "Bearer" automáticamente)
- Asegúrate de no haber copiado espacios extra al inicio o final del token
- Los tokens expiran en 7 días, haz sign-in de nuevo si pasó mucho tiempo
- Si copiaste desde el curl de Swagger y ves "Bearer Bearer" en el header, ese es el problema

---

### ¿Por qué no puedo ver la telemetría de un vehículo?

Si ves el error **"You are not authorized to view tracking data for this vehicle"**, verifica:

1. **¿Usaste el vehicleId correcto?**
   - ❌ Si María reservó el vehículo 1, NO puede ver telemetría del vehículo 2
   - ✅ Usa el mismo ID del vehículo que reservaste

2. **¿La fecha actual está dentro del período de reserva?**
   - ❌ Si la reserva es del 15 al 20 de diciembre y hoy es 12 de noviembre, NO funcionará
   - ✅ Crea una reserva que incluya la fecha de hoy (ej: del 10 al 15 de noviembre)

3. **¿La reserva está en estado correcto?**
   - ✅ Estados válidos: PENDING o CONFIRMED
   - ❌ Estados inválidos: CANCELLED, REJECTED

4. **¿Estás autenticado con el usuario correcto?**
   - Si eres ARRENDADOR (Carlos), solo puedes ver telemetría de TUS vehículos
   - Si eres ARRENDATARIO (María), solo puedes ver telemetría de vehículos que RESERVASTE y cuya reserva esté ACTIVA HOY

**Ejemplo de reserva correcta para ver telemetría hoy:**
```json
{
  "vehicleId": 1,
  "startDate": "2025-11-10",
  "endDate": "2025-11-15"
}
```
(La fecha actual 2025-11-12 está entre el 10 y el 15, por lo que la reserva está activa)

---

### ¿Puedo tener múltiples usuarios con el mismo email?

No, el email debe ser único. Si intentas registrar un email duplicado, verás un error.

---

### ¿Qué pasa si uso el rol incorrecto?

Verás el error: `"Invalid role provided. Must be 'arrendador' or 'arrendatario'."`

---

### ¿Puedo cambiar el rol de un usuario después de crearlo?

No, el rol es inmutable una vez creado el usuario. Necesitarías crear un nuevo usuario.

---

## 🎉 ¡Felicitaciones!

Has completado el tutorial completo de Swagger UI. Ahora sabes:

- ✅ Registrar usuarios con diferentes roles
- ✅ Iniciar sesión y obtener tokens JWT
- ✅ Autenticar Swagger con tokens
- ✅ Crear y gestionar vehículos
- ✅ Crear y ver reservas
- ✅ Dejar y ver reseñas
- ✅ Registrar y consultar telemetría
- ✅ Probar la seguridad del sistema

**¡Tu backend está 100% funcional y listo para integrarse con el frontend!** 🚀

---

*Última actualización: 12 de noviembre de 2025*

