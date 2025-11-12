# Guía Rápida de Pruebas - CodexaTeam Backend

## 🚀 Inicio Rápido

### 1. Verificar Compilación
```bash
mvnw.cmd clean compile
```
**Resultado esperado:** `BUILD SUCCESS`

### 2. Iniciar el Servidor
```bash
mvnw.cmd spring-boot:run
```
**Resultado esperado:** Servidor corriendo en `http://localhost:8080`

### 3. Verificar Salud del Servidor
```bash
curl http://localhost:8080/actuator/health
```
**Resultado esperado:** `{"status":"UP"}`

---

## 🧪 Pruebas de Funcionalidad

### Escenario 1: Ciclo Completo de Usuario Propietario

#### Paso 1: Registrar Propietario
```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "owner@test.com",
    "password": "Owner123!",
    "roles": ["ROLE_ARRENDADOR"]
  }'
```
**Guardar el token de la respuesta**

#### Paso 2: Publicar Vehículo
```bash
curl -X POST http://localhost:8080/api/v1/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_OWNER>" \
  -d '{
    "brand": "Toyota",
    "model": "Camry",
    "year": 2023,
    "pricePerDay": 75.00,
    "location": "Lima, Perú",
    "description": "Vehículo ejecutivo en excelente estado",
    "imageUrl": "https://example.com/camry.jpg",
    "available": true
  }'
```
**Guardar el vehicleId de la respuesta**

#### Paso 3: Ver Mis Vehículos
```bash
curl -X GET http://localhost:8080/api/v1/vehicles/my-listings \
  -H "Authorization: Bearer <TOKEN_OWNER>"
```
**Debe mostrar el vehículo creado**

#### Paso 4: Registrar Telemetría del Vehículo
```bash
curl -X POST http://localhost:8080/api/v1/telemetry \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_OWNER>" \
  -d '{
    "vehicleId": <VEHICLE_ID>,
    "latitude": -12.0464,
    "longitude": -77.0428,
    "speed": 65.0,
    "fuelLevel": 85.0
  }'
```
**Debe registrarse exitosamente (solo si eres el propietario)**

---

### Escenario 2: Ciclo Completo de Usuario Arrendatario

#### Paso 1: Registrar Arrendatario
```bash
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "renter@test.com",
    "password": "Renter123!",
    "roles": ["ROLE_ARRENDATARIO"]
  }'
```
**Guardar el token de la respuesta**

#### Paso 2: Ver Catálogo de Vehículos
```bash
curl -X GET http://localhost:8080/api/v1/vehicles
```
**No requiere autenticación, debe mostrar todos los vehículos**

#### Paso 3: Ver Detalle de un Vehículo
```bash
curl -X GET http://localhost:8080/api/v1/vehicles/<VEHICLE_ID>
```

#### Paso 4: Crear Reserva
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_RENTER>" \
  -d '{
    "vehicleId": <VEHICLE_ID>,
    "startDate": "2025-12-15",
    "endDate": "2025-12-20"
  }'
```
**Debe calcular automáticamente el precio total (5 días * 75.00 = 375.00)**

#### Paso 5: Ver Mis Reservas
```bash
curl -X GET http://localhost:8080/api/v1/bookings/my-bookings \
  -H "Authorization: Bearer <TOKEN_RENTER>"
```

#### Paso 6: Ver Tracking del Vehículo Alquilado
```bash
curl -X GET http://localhost:8080/api/v1/telemetry/vehicle/<VEHICLE_ID> \
  -H "Authorization: Bearer <TOKEN_RENTER>"
```
**Debe permitir acceso si hay una reserva activa**

#### Paso 7: Dejar una Reseña
```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_RENTER>" \
  -d '{
    "vehicleId": <VEHICLE_ID>,
    "rating": 5,
    "comment": "Excelente vehículo, muy cómodo y económico"
  }'
```

#### Paso 8: Ver Mis Reseñas
```bash
curl -X GET http://localhost:8080/api/v1/reviews/my-reviews \
  -H "Authorization: Bearer <TOKEN_RENTER>"
```

---

### Escenario 3: Propietario ve Solicitudes de Reserva

```bash
curl -X GET http://localhost:8080/api/v1/bookings/my-requests \
  -H "Authorization: Bearer <TOKEN_OWNER>"
```
**Debe mostrar las reservas de sus vehículos**

---

## 🛡️ Pruebas de Seguridad

### Prueba 1: Acceso No Autorizado a Telemetría

#### Intentar registrar telemetría de vehículo ajeno
```bash
# Login como owner2
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "owner2@test.com",
    "password": "Owner123!",
    "roles": ["ROLE_ARRENDADOR"]
  }'

# Intentar registrar telemetría del vehículo de owner1
curl -X POST http://localhost:8080/api/v1/telemetry \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_OWNER2>" \
  -d '{
    "vehicleId": <VEHICLE_ID_OF_OWNER1>,
    "latitude": -12.0464,
    "longitude": -77.0428,
    "speed": 65.0,
    "fuelLevel": 85.0
  }'
```
**Resultado esperado:** `403 Forbidden` o `SecurityException: You are not authorized to record telemetry for this vehicle.`

---

### Prueba 2: Acceso No Autorizado a Tracking

```bash
# Login como renter2 (sin reserva)
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "renter2@test.com",
    "password": "Renter123!",
    "roles": ["ROLE_ARRENDATARIO"]
  }'

# Intentar ver tracking sin tener reserva
curl -X GET http://localhost:8080/api/v1/telemetry/vehicle/<VEHICLE_ID> \
  -H "Authorization: Bearer <TOKEN_RENTER2>"
```
**Resultado esperado:** `403 Forbidden` o `SecurityException: You are not authorized to view tracking data for this vehicle.`

---

### Prueba 3: Intentar Crear Vehículo sin Rol ARRENDADOR

```bash
# Intentar crear vehículo con token de ARRENDATARIO
curl -X POST http://localhost:8080/api/v1/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_RENTER>" \
  -d '{
    "brand": "Honda",
    "model": "Civic",
    "year": 2023,
    "pricePerDay": 60.00,
    "location": "Lima",
    "imageUrl": "https://example.com/civic.jpg",
    "available": true
  }'
```
**Resultado esperado:** `403 Forbidden`

---

### Prueba 4: Intentar Reservar Vehículo No Disponible

```bash
# Primero, cambiar el status del vehículo a "rented" (esto requeriría un endpoint adicional)
# Por ahora, solo validar que la lógica funciona correctamente

curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN_RENTER>" \
  -d '{
    "vehicleId": <VEHICLE_ID_NOT_AVAILABLE>,
    "startDate": "2025-12-15",
    "endDate": "2025-12-20"
  }'
```
**Resultado esperado:** `400 Bad Request` con mensaje "Vehicle is not available for booking"

---

## 📊 Checklist de Validación

### Funcionalidades Core
- [ ] Sign-up de usuarios (ARRENDADOR y ARRENDATARIO)
- [ ] Sign-in y generación de JWT
- [ ] Crear vehículo (solo ARRENDADOR)
- [ ] Ver catálogo de vehículos (público)
- [ ] Ver mis vehículos (ARRENDADOR)
- [ ] Crear reserva (ARRENDATARIO)
- [ ] Ver mis reservas (ARRENDATARIO)
- [ ] Ver solicitudes de reserva (ARRENDADOR)
- [ ] Crear reseña (ARRENDATARIO)
- [ ] Ver reseñas de un vehículo (público)
- [ ] Registrar telemetría (ARRENDADOR, solo su vehículo)
- [ ] Ver tracking (ARRENDADOR del vehículo O ARRENDATARIO con reserva activa)

### Validaciones de Seguridad
- [ ] No se puede crear vehículo sin rol ARRENDADOR
- [ ] No se puede crear reserva sin rol ARRENDATARIO
- [ ] No se puede registrar telemetría de vehículo ajeno
- [ ] No se puede ver tracking sin permisos
- [ ] No se puede reservar vehículo no disponible
- [ ] Todos los endpoints protegidos requieren JWT válido

### Validaciones de Negocio
- [ ] Precio total se calcula correctamente (días * pricePerDay)
- [ ] No se puede crear reserva con startDate > endDate
- [ ] Solo el propietario puede registrar telemetría de su vehículo
- [ ] Solo propietario o arrendatario con reserva activa puede ver tracking
- [ ] Validación de propiedad funciona correctamente

---

## 🐛 Troubleshooting

### Error: "Cannot connect to database"
**Solución:**
1. Verificar que MySQL esté corriendo
2. Verificar credenciales en `application.properties`
3. Crear la base de datos si no existe:
```sql
CREATE DATABASE renticar_db;
```

### Error: "JWT token is invalid"
**Solución:**
1. Verificar que el token no haya expirado (7 días por defecto)
2. Hacer sign-in nuevamente para obtener un nuevo token

### Error: "User not authenticated"
**Solución:**
1. Asegurarse de incluir el header `Authorization: Bearer <token>` en todas las peticiones protegidas
2. Verificar que el token esté correctamente copiado (sin espacios extra)

### Error: "Vehicle not found"
**Solución:**
1. Verificar que el vehicleId exista en la base de datos
2. Usar el ID correcto devuelto al crear el vehículo

---

## 📝 Notas Importantes

1. **Fechas de Reserva:** Deben ser en formato ISO 8601: `YYYY-MM-DD`
2. **Tokens JWT:** Expiran en 7 días por defecto
3. **Roles:** Un usuario puede tener múltiples roles, separados por comas en el array
4. **IDs:** Son autogenerados por la base de datos (autoincrement)
5. **Status de Vehículos:** Valores posibles: `"available"`, `"rented"`, `"maintenance"`
6. **Status de Reservas:** Valores posibles: `"PENDING"`, `"CONFIRMED"`, `"COMPLETED"`, `"CANCELLED"`

---

## 🎯 Swagger UI

Para una experiencia de testing más visual, accede a:
```
http://localhost:8080/swagger-ui.html
```

Ahí puedes:
- Ver todos los endpoints disponibles
- Probar cada endpoint directamente desde el navegador
- Ver la estructura de request/response
- Autenticarte con el token JWT

---

**¡Listo para probar! 🚀**

*Última actualización: 12 de noviembre de 2025*

