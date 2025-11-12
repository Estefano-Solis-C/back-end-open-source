# Ejemplos de Uso de la API - CodexaTeam Backend

Este documento contiene ejemplos prácticos de cómo usar la API del backend de CodexaTeam.

## 📋 Requisitos

- Backend ejecutándose en `http://localhost:8080`
- Base de datos MySQL configurada
- Cliente HTTP (curl, Postman, Thunder Client, etc.)

---

## 🔐 1. Autenticación

### 1.1. Registrar un Propietario (Arrendador)

```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "propietario@email.com",
    "password": "Password123!",
    "roles": ["ROLE_ARRENDADOR"]
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "username": "propietario@email.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwcm9waWV0YXJpb0BlbWFpbC5jb20iLCJpYXQiOjE2OTk4MDAwMDAsImV4cCI6MTcwMDQwNDgwMCwicm9sZXMiOlsiUk9MRV9BUlJFTkRBRE9SIl19.xyz..."
}
```

### 1.2. Registrar un Arrendatario

```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "arrendatario@email.com",
    "password": "Password123!",
    "roles": ["ROLE_ARRENDATARIO"]
  }'
```

**Respuesta:**
```json
{
  "id": 2,
  "username": "arrendatario@email.com",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcnJlbmRhdGFyaW9AZW1haWwuY29tIiwiaWF0IjoxNjk5ODAwMDAwLCJleHAiOjE3MDA0MDQ4MDAsInJvbGVzIjpbIlJPTEVfQVJSRU5EQVRBU klPIl19.abc..."
}
```

### 1.3. Iniciar Sesión

```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-in \
  -H "Content-Type: application/json" \
  -d '{
    "username": "propietario@email.com",
    "password": "Password123!"
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "username": "propietario@email.com",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 1.4. Obtener Información de Usuario

```bash
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 🚙 2. Gestión de Vehículos (Listings)

### 2.1. Crear un Vehículo (Requiere ROLE_ARRENDADOR)

```bash
curl -X POST http://localhost:8080/api/v1/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_PROPIETARIO>" \
  -d '{
    "brand": "Toyota",
    "model": "Corolla",
    "year": 2022,
    "pricePerDay": 50.00,
    "location": "Lima, Perú",
    "description": "Vehículo en excelente estado, económico y confiable.",
    "imageUrl": "https://example.com/images/toyota-corolla.jpg",
    "available": true
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2022,
  "pricePerDay": 50.00,
  "location": "Lima, Perú",
  "description": "Vehículo en excelente estado, económico y confiable.",
  "imageUrl": "https://example.com/images/toyota-corolla.jpg",
  "available": true,
  "ownerId": 1,
  "createdAt": "2025-11-12T10:30:00",
  "updatedAt": "2025-11-12T10:30:00"
}
```

### 2.2. Listar Todos los Vehículos (Público)

```bash
curl -X GET http://localhost:8080/api/v1/vehicles
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "brand": "Toyota",
    "model": "Corolla",
    "year": 2022,
    "pricePerDay": 50.00,
    "location": "Lima, Perú",
    "ownerId": 1,
    "available": true
  },
  {
    "id": 2,
    "brand": "Honda",
    "model": "Civic",
    "year": 2023,
    "pricePerDay": 60.00,
    "location": "Callao, Perú",
    "ownerId": 1,
    "available": true
  }
]
```

### 2.3. Ver Detalle de un Vehículo (Público)

```bash
curl -X GET http://localhost:8080/api/v1/vehicles/1
```

### 2.4. Ver Mis Vehículos Publicados (Requiere ROLE_ARRENDADOR)

```bash
curl -X GET http://localhost:8080/api/v1/vehicles/my-listings \
  -H "Authorization: Bearer <TOKEN_PROPIETARIO>"
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "brand": "Toyota",
    "model": "Corolla",
    "ownerId": 1,
    "pricePerDay": 50.00
  }
]
```

---

## 📅 3. Sistema de Reservas (Booking)

### 3.1. Crear una Reserva (Requiere ROLE_ARRENDATARIO)

```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_ARRENDATARIO>" \
  -d '{
    "vehicleId": 1,
    "startDate": "2025-12-01",
    "endDate": "2025-12-05"
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "vehicleId": 1,
  "renterId": 2,
  "ownerId": 1,
  "startDate": "2025-12-01",
  "endDate": "2025-12-05",
  "totalPrice": 200.00,
  "status": "PENDING",
  "createdAt": "2025-11-12T11:00:00"
}
```

### 3.2. Ver Mis Reservas (Requiere ROLE_ARRENDATARIO)

```bash
curl -X GET http://localhost:8080/api/v1/bookings/my-bookings \
  -H "Authorization: Bearer <TOKEN_ARRENDATARIO>"
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "ownerId": 1,
    "startDate": "2025-12-01",
    "endDate": "2025-12-05",
    "totalPrice": 200.00,
    "status": "PENDING"
  }
]
```

### 3.3. Ver Solicitudes de Reserva (Requiere ROLE_ARRENDADOR)

```bash
curl -X GET http://localhost:8080/api/v1/bookings/my-requests \
  -H "Authorization: Bearer <TOKEN_PROPIETARIO>"
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "ownerId": 1,
    "startDate": "2025-12-01",
    "endDate": "2025-12-05",
    "totalPrice": 200.00,
    "status": "PENDING"
  }
]
```

---

## ⭐ 4. Reseñas y Ratings

### 4.1. Crear una Reseña (Requiere ROLE_ARRENDATARIO)

```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_ARRENDATARIO>" \
  -d '{
    "vehicleId": 1,
    "rating": 5,
    "comment": "Excelente vehículo, muy cómodo y económico. El propietario fue muy amable."
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "vehicleId": 1,
  "renterId": 2,
  "rating": 5,
  "comment": "Excelente vehículo, muy cómodo y económico. El propietario fue muy amable.",
  "createdAt": "2025-11-12T12:00:00"
}
```

### 4.2. Ver Reseñas de un Vehículo (Público)

```bash
curl -X GET http://localhost:8080/api/v1/reviews/vehicle/1
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "renterId": 2,
    "rating": 5,
    "comment": "Excelente vehículo, muy cómodo y económico.",
    "createdAt": "2025-11-12T12:00:00"
  },
  {
    "id": 2,
    "vehicleId": 1,
    "renterId": 3,
    "rating": 4,
    "comment": "Muy buen auto, recomendado.",
    "createdAt": "2025-11-10T10:30:00"
  }
]
```

### 4.3. Ver Mis Reseñas (Requiere ROLE_ARRENDATARIO)

```bash
curl -X GET http://localhost:8080/api/v1/reviews/my-reviews \
  -H "Authorization: Bearer <TOKEN_ARRENDATARIO>"
```

---

## 📍 5. Telemetría IoT

### 5.1. Registrar Datos de Telemetría (Requiere ROLE_ARRENDADOR)

```bash
curl -X POST http://localhost:8080/api/v1/telemetry \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_PROPIETARIO>" \
  -d '{
    "vehicleId": 1,
    "latitude": -12.0464,
    "longitude": -77.0428,
    "speed": 45.5,
    "fuelLevel": 75.0
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "vehicleId": 1,
  "latitude": -12.0464,
  "longitude": -77.0428,
  "speed": 45.5,
  "fuelLevel": 75.0,
  "timestamp": "2025-11-12T14:30:00"
}
```

### 5.2. Ver Telemetría de un Vehículo (Requiere Autenticación)

```bash
curl -X GET http://localhost:8080/api/v1/telemetry/vehicle/1 \
  -H "Authorization: Bearer <TOKEN>"
```

**Respuesta:**
```json
[
  {
    "id": 3,
    "vehicleId": 1,
    "latitude": -12.0464,
    "longitude": -77.0428,
    "speed": 45.5,
    "fuelLevel": 75.0,
    "timestamp": "2025-11-12T14:30:00"
  },
  {
    "id": 2,
    "vehicleId": 1,
    "latitude": -12.0450,
    "longitude": -77.0420,
    "speed": 40.0,
    "fuelLevel": 76.0,
    "timestamp": "2025-11-12T14:20:00"
  }
]
```

---

## 🧪 Escenario Completo de Prueba

### Paso 1: Crear Usuarios

```bash
# Propietario
TOKEN_OWNER=$(curl -s -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{"username":"owner@test.com","password":"Pass123!","roles":["ROLE_ARRENDADOR"]}' \
  | jq -r '.token')

# Arrendatario
TOKEN_RENTER=$(curl -s -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{"username":"renter@test.com","password":"Pass123!","roles":["ROLE_ARRENDATARIO"]}' \
  | jq -r '.token')
```

### Paso 2: Propietario Crea un Vehículo

```bash
VEHICLE_ID=$(curl -s -X POST http://localhost:8080/api/v1/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_OWNER" \
  -d '{
    "brand":"Toyota",
    "model":"Camry",
    "year":2023,
    "pricePerDay":80.0,
    "location":"Lima",
    "description":"Auto ejecutivo",
    "imageUrl":"https://example.com/camry.jpg",
    "available":true
  }' | jq -r '.id')
```

### Paso 3: Arrendatario Crea una Reserva

```bash
BOOKING_ID=$(curl -s -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_RENTER" \
  -d "{
    \"vehicleId\":$VEHICLE_ID,
    \"startDate\":\"2025-12-15\",
    \"endDate\":\"2025-12-20\"
  }" | jq -r '.id')
```

### Paso 4: Arrendatario Crea una Reseña

```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_RENTER" \
  -d "{
    \"vehicleId\":$VEHICLE_ID,
    \"rating\":5,
    \"comment\":\"Excelente experiencia\"
  }"
```

### Paso 5: Propietario Ve sus Solicitudes

```bash
curl -X GET http://localhost:8080/api/v1/bookings/my-requests \
  -H "Authorization: Bearer $TOKEN_OWNER"
```

---

## ❌ Errores Comunes y Soluciones

### Error 401 Unauthorized
**Causa:** Token JWT inválido o expirado
**Solución:** Volver a hacer sign-in para obtener un nuevo token

### Error 403 Forbidden
**Causa:** Usuario no tiene el rol necesario
**Solución:** Verificar que el usuario tenga el rol correcto (ARRENDADOR o ARRENDATARIO)

### Error 404 Not Found
**Causa:** Recurso no existe
**Solución:** Verificar que el ID del recurso sea correcto

### Error 500 Internal Server Error
**Causa:** Error en el servidor o base de datos
**Solución:** Verificar logs del servidor y configuración de base de datos

---

## 📊 Postman Collection

Para facilitar las pruebas, puedes importar esta colección en Postman:

```json
{
  "info": {
    "name": "CodexaTeam Backend API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Sign Up",
          "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "url": "http://localhost:8080/api/v1/authentication/sign-up",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"user@example.com\",\n  \"password\": \"Password123!\",\n  \"roles\": [\"ROLE_ARRENDADOR\"]\n}"
            }
          }
        }
      ]
    }
  ]
}
```

---

## 🎯 Próximos Pasos

1. Explorar la documentación Swagger: `http://localhost:8080/swagger-ui.html`
2. Probar todos los endpoints con diferentes roles
3. Verificar las validaciones de seguridad
4. Integrar con el frontend

---

*Última actualización: 12 de noviembre de 2025*

