# 🧪 Guía de Pruebas Práctica - Ejemplo Completo

## 🎯 Escenario de Prueba: Alquiler de un Toyota Camry

Este ejemplo simula el flujo completo de la plataforma:
1. Un propietario registra su vehículo
2. Un arrendatario lo alquila
3. El arrendatario deja una reseña
4. Se registra telemetría del vehículo

---

## 📋 Requisitos Previos

- ✅ Servidor corriendo en `http://localhost:8080`
- ✅ MySQL activo con base de datos `renticar_db`
- ✅ Herramienta para hacer peticiones HTTP (cURL, Postman, Thunder Client, etc.)

---

## 🚀 PARTE 1: Configuración Inicial

### Paso 1: Verificar que el Servidor Está Activo

**Comando:**
```bash
curl http://localhost:8080/actuator/health
```

**Respuesta Esperada:**
```json
{
  "status": "UP"
}
```

✅ Si ves esto, ¡continúa con el siguiente paso!

---

## 👤 PARTE 2: Crear Usuarios

### Paso 2: Registrar un Propietario (ARRENDADOR)

**Comando:**
```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-up ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"carlos.owner@email.com\",\"password\":\"Carlos123!\",\"roles\":[\"ROLE_ARRENDADOR\"]}"
```

**Respuesta Esperada:**
```json
{
  "id": 1,
  "username": "carlos.owner@email.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAZW1haWwuY29tIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDA2MDQ4MDB9.xyz..."
}
```

**📝 IMPORTANTE: Guarda el token en una variable:**
```bash
# Windows CMD
set TOKEN_OWNER=eyJhbGciOiJIUzI1NiJ9...

# PowerShell
$TOKEN_OWNER = "eyJhbGciOiJIUzI1NiJ9..."
```

---

### Paso 3: Registrar un Arrendatario (ARRENDATARIO)

**Comando:**
```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-up ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"maria.renter@email.com\",\"password\":\"Maria123!\",\"roles\":[\"ROLE_ARRENDATARIO\"]}"
```

**Respuesta Esperada:**
```json
{
  "id": 2,
  "username": "maria.renter@email.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJpYS5yZW50ZXJAZW1haWwuY29tIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDA2MDQ4MDB9.abc..."
}
```

**📝 Guarda este token también:**
```bash
# Windows CMD
set TOKEN_RENTER=eyJhbGciOiJIUzI1NiJ9...

# PowerShell
$TOKEN_RENTER = "eyJhbGciOiJIUzI1NiJ9..."
```

---

## 🚗 PARTE 3: Publicar un Vehículo

### Paso 4: Carlos Publica su Toyota Camry

**Comando:**
```bash
curl -X POST http://localhost:8080/api/v1/vehicles ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN_OWNER%" ^
  -d "{\"brand\":\"Toyota\",\"model\":\"Camry\",\"year\":2023,\"pricePerDay\":75.00,\"location\":\"Lima, Miraflores\",\"description\":\"Auto ejecutivo en excelente estado, ideal para viajes de negocios. Incluye GPS y seguro completo.\",\"imageUrl\":\"https://www.toyota.com/camry.jpg\",\"available\":true}"
```

**Respuesta Esperada:**
```json
{
  "id": 1,
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.0,
  "status": "available",
  "imageUrl": "https://www.toyota.com/camry.jpg",
  "ownerId": 1,
  "createdAt": "2025-11-12T02:30:00"
}
```

**📝 Guarda el ID del vehículo:**
```bash
# Windows CMD
set VEHICLE_ID=1

# PowerShell
$VEHICLE_ID = 1
```

---

### Paso 5: Ver el Catálogo de Vehículos (Público)

**Comando:**
```bash
curl http://localhost:8080/api/v1/vehicles
```

**Respuesta Esperada:**
```json
[
  {
    "id": 1,
    "brand": "Toyota",
    "model": "Camry",
    "year": 2023,
    "pricePerDay": 75.0,
    "status": "available",
    "imageUrl": "https://www.toyota.com/camry.jpg",
    "ownerId": 1,
    "createdAt": "2025-11-12T02:30:00"
  }
]
```

✅ Este endpoint es público, no requiere autenticación.

---

### Paso 6: Carlos Ve Sus Vehículos Publicados

**Comando:**
```bash
curl -X GET http://localhost:8080/api/v1/vehicles/my-listings ^
  -H "Authorization: Bearer %TOKEN_OWNER%"
```

**Respuesta Esperada:**
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

## 📅 PARTE 4: Crear una Reserva

### Paso 7: María Reserva el Toyota Camry

**Comando:**
```bash
curl -X POST http://localhost:8080/api/v1/bookings ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN_RENTER%" ^
  -d "{\"vehicleId\":1,\"startDate\":\"2025-12-15\",\"endDate\":\"2025-12-20\"}"
```

**Respuesta Esperada:**
```json
{
  "id": 1,
  "vehicleId": 1,
  "renterId": 2,
  "ownerId": 1,
  "startDate": "2025-12-15",
  "endDate": "2025-12-20",
  "totalPrice": 375.0,
  "status": "PENDING",
  "createdAt": "2025-11-12T02:35:00"
}
```

✅ **Cálculo automático:** 5 días × $75/día = $375

**📝 Guarda el ID de la reserva:**
```bash
# Windows CMD
set BOOKING_ID=1

# PowerShell
$BOOKING_ID = 1
```

---

### Paso 8: María Ve Sus Reservas

**Comando:**
```bash
curl -X GET http://localhost:8080/api/v1/bookings/my-bookings ^
  -H "Authorization: Bearer %TOKEN_RENTER%"
```

**Respuesta Esperada:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "ownerId": 1,
    "startDate": "2025-12-15",
    "endDate": "2025-12-20",
    "totalPrice": 375.0,
    "status": "PENDING",
    "createdAt": "2025-11-12T02:35:00"
  }
]
```

---

### Paso 9: Carlos Ve las Solicitudes de Reserva

**Comando:**
```bash
curl -X GET http://localhost:8080/api/v1/bookings/my-requests ^
  -H "Authorization: Bearer %TOKEN_OWNER%"
```

**Respuesta Esperada:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "ownerId": 1,
    "startDate": "2025-12-15",
    "endDate": "2025-12-20",
    "totalPrice": 375.0,
    "status": "PENDING",
    "createdAt": "2025-11-12T02:35:00"
  }
]
```

---

## ⭐ PARTE 5: Dejar una Reseña

### Paso 10: María Deja una Reseña del Vehículo

**Comando:**
```bash
curl -X POST http://localhost:8080/api/v1/reviews ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN_RENTER%" ^
  -d "{\"vehicleId\":1,\"rating\":5,\"comment\":\"Excelente vehículo! Muy cómodo y en perfecto estado. Carlos fue muy amable y todo el proceso fue súper fácil. Lo recomiendo 100%.\"}"
```

**Respuesta Esperada:**
```json
{
  "id": 1,
  "vehicleId": 1,
  "renterId": 2,
  "rating": 5,
  "comment": "Excelente vehículo! Muy cómodo y en perfecto estado. Carlos fue muy amable y todo el proceso fue súper fácil. Lo recomiendo 100%.",
  "createdAt": "2025-11-12T02:40:00"
}
```

---

### Paso 11: Ver Reseñas del Vehículo (Público)

**Comando:**
```bash
curl http://localhost:8080/api/v1/reviews/vehicle/1
```

**Respuesta Esperada:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "rating": 5,
    "comment": "Excelente vehículo! Muy cómodo y en perfecto estado...",
    "createdAt": "2025-11-12T02:40:00"
  }
]
```

---

### Paso 12: María Ve Sus Reseñas

**Comando:**
```bash
curl -X GET http://localhost:8080/api/v1/reviews/my-reviews ^
  -H "Authorization: Bearer %TOKEN_RENTER%"
```

**Respuesta Esperada:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "rating": 5,
    "comment": "Excelente vehículo! Muy cómodo y en perfecto estado...",
    "createdAt": "2025-11-12T02:40:00"
  }
]
```

---

## 📍 PARTE 6: Telemetría IoT

### Paso 13: Carlos Registra Telemetría de su Vehículo

**Comando:**
```bash
curl -X POST http://localhost:8080/api/v1/telemetry ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN_OWNER%" ^
  -d "{\"vehicleId\":1,\"latitude\":-12.0464,\"longitude\":-77.0428,\"speed\":65.5,\"fuelLevel\":80.0}"
```

**Respuesta Esperada:**
```json
{
  "id": 1,
  "vehicleId": 1,
  "latitude": -12.0464,
  "longitude": -77.0428,
  "speed": 65.5,
  "fuelLevel": 80.0,
  "timestamp": "2025-11-12T02:45:00"
}
```

✅ **Validación de seguridad:** Solo el propietario puede registrar telemetría

---

### Paso 14: María Ve el Tracking del Vehículo Alquilado

**Comando:**
```bash
curl -X GET http://localhost:8080/api/v1/telemetry/vehicle/1 ^
  -H "Authorization: Bearer %TOKEN_RENTER%"
```

**Respuesta Esperada:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "latitude": -12.0464,
    "longitude": -77.0428,
    "speed": 65.5,
    "fuelLevel": 80.0,
    "timestamp": "2025-11-12T02:45:00"
  }
]
```

✅ **Validación de seguridad:** María puede ver el tracking porque tiene una reserva activa

---

## 🛡️ PARTE 7: Pruebas de Seguridad

### Paso 15: Intentar Crear Vehículo con Rol Incorrecto (Debe Fallar)

**Comando:**
```bash
curl -X POST http://localhost:8080/api/v1/vehicles ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN_RENTER%" ^
  -d "{\"brand\":\"Honda\",\"model\":\"Civic\",\"year\":2023,\"pricePerDay\":60.0,\"location\":\"Lima\",\"imageUrl\":\"https://example.com/civic.jpg\",\"available\":true}"
```

**Respuesta Esperada:**
```json
{
  "error": "Forbidden",
  "message": "Access Denied",
  "status": 403
}
```

❌ María no puede crear vehículos porque tiene rol ARRENDATARIO

---

### Paso 16: Crear Otro Usuario para Probar Telemetría No Autorizada

**Registrar otro propietario:**
```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-up ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"juan.owner@email.com\",\"password\":\"Juan123!\",\"roles\":[\"ROLE_ARRENDADOR\"]}"
```

**Guardar el token:**
```bash
set TOKEN_OWNER2=<token_recibido>
```

**Intentar registrar telemetría del vehículo de Carlos:**
```bash
curl -X POST http://localhost:8080/api/v1/telemetry ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN_OWNER2%" ^
  -d "{\"vehicleId\":1,\"latitude\":-12.0500,\"longitude\":-77.0500,\"speed\":70.0,\"fuelLevel\":75.0}"
```

**Respuesta Esperada:**
```json
{
  "error": "Security Exception",
  "message": "You are not authorized to record telemetry for this vehicle.",
  "status": 403
}
```

❌ Juan no puede registrar telemetría del vehículo de Carlos

---

## ✅ PARTE 8: Verificación Final

### Checklist de Funcionalidades Probadas

- [x] Sign-up de ARRENDADOR
- [x] Sign-up de ARRENDATARIO
- [x] Crear vehículo (ARRENDADOR)
- [x] Ver catálogo de vehículos (público)
- [x] Ver mis vehículos (ARRENDADOR)
- [x] Crear reserva (ARRENDATARIO)
- [x] Ver mis reservas (ARRENDATARIO)
- [x] Ver solicitudes de reserva (ARRENDADOR)
- [x] Crear reseña (ARRENDATARIO)
- [x] Ver reseñas de vehículo (público)
- [x] Ver mis reseñas (ARRENDATARIO)
- [x] Registrar telemetría (ARRENDADOR, solo su vehículo)
- [x] Ver tracking (ARRENDADOR/ARRENDATARIO con reserva)
- [x] Validación de seguridad: No crear vehículo sin rol
- [x] Validación de seguridad: No registrar telemetría de vehículo ajeno

---

## 📊 Resumen de Datos Creados

| Entidad | ID | Descripción |
|---------|----|-|
| Usuario 1 | 1 | carlos.owner@email.com (ARRENDADOR) |
| Usuario 2 | 2 | maria.renter@email.com (ARRENDATARIO) |
| Vehículo 1 | 1 | Toyota Camry 2023 ($75/día) |
| Reserva 1 | 1 | 5 días (15-20 Dic) - $375 total |
| Reseña 1 | 1 | 5 estrellas - "Excelente vehículo..." |
| Telemetría 1 | 1 | Lima, Perú (-12.0464, -77.0428) |

---

## 🎯 Próximos Pasos

### 1. Explorar Swagger UI
```
http://localhost:8080/swagger-ui.html
```
Ahí puedes probar todos los endpoints de forma visual.

### 2. Verificar la Base de Datos
```sql
USE renticar_db;
SELECT * FROM users;
SELECT * FROM vehicles;
SELECT * FROM bookings;
SELECT * FROM reviews;
SELECT * FROM telemetries;
```

### 3. Probar Más Escenarios
- Crear múltiples vehículos
- Crear múltiples reservas
- Probar validaciones de fechas (startDate > endDate)
- Probar reservas de vehículos no disponibles

---

## 🆘 Solución de Problemas

### Error: "JWT token is invalid"
**Solución:** Los tokens expiran en 7 días. Haz sign-in nuevamente:
```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-in ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"carlos.owner@email.com\",\"password\":\"Carlos123!\"}"
```

### Error: "Vehicle not found"
**Solución:** Verifica que el vehicleId exista:
```bash
curl http://localhost:8080/api/v1/vehicles
```

### Error: "User not authenticated"
**Solución:** Verifica que el header Authorization esté correcto:
```
Authorization: Bearer <tu_token_aqui>
```

---

## 🎉 ¡Felicitaciones!

Has probado exitosamente:
- ✅ Autenticación JWT
- ✅ Autorización por roles
- ✅ CRUD de vehículos
- ✅ Sistema de reservas
- ✅ Sistema de reseñas
- ✅ Telemetría IoT
- ✅ Validaciones de seguridad
- ✅ ACLs entre dominios

**Tu backend está 100% funcional y listo para integrarse con el frontend!** 🚀

---

*Guía generada el 12 de noviembre de 2025*

