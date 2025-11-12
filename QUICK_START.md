# 🚀 Guía de Inicio Rápido - 5 Minutos

Los roles deben enviarse **SIN** el prefijo `ROLE_`:
- ✅ Correcto: `"arrendador"` o `"arrendatario"`
- ❌ Incorrecto: `"ROLE_ARRENDADOR"` o `"ROLE_ARRENDATARIO"`

El backend añade automáticamente el prefijo `ROLE_`.

---

## 📖 ¿Prefieres usar Swagger UI?

Si prefieres probar la API desde el navegador con una interfaz gráfica, consulta el tutorial completo:

📘 **[SWAGGER_TUTORIAL_COMPLETO.md](SWAGGER_TUTORIAL_COMPLETO.md)** - 22 pasos con todos los ejemplos corregidos

---

## 🎯 Prueba Rápida en 3 Pasos

### Paso 1: Registrar un Propietario

**Comando Windows (CMD):**
```cmd
curl -X POST http://localhost:8080/api/v1/authentication/sign-up -H "Content-Type: application/json" -d "{\"name\":\"Carlos Perez\",\"email\":\"carlos@test.com\",\"password\":\"Test123!\",\"role\":\"arrendador\"}"
```

**Comando PowerShell:**
```powershell
curl -Method POST -Uri "http://localhost:8080/api/v1/authentication/sign-up" -Headers @{"Content-Type"="application/json"} -Body '{"name":"Carlos Perez","email":"carlos@test.com","password":"Test123!","role":"arrendador"}'
```

**Respuesta Esperada:**
```json
{
  "id": 1,
  "name": "Carlos Perez",
  "email": "carlos@test.com",
  "roles": ["ROLE_ARRENDADOR"]
}
```

**📝 IMPORTANTE:** Copia el `id` y guarda tu email/password para el login

---
### Paso 2: Iniciar Sesión (Sign-In)

Ahora inicia sesión para obtener el token JWT:

**Comando Windows (CMD):**
```cmd
curl -X POST http://localhost:8080/api/v1/authentication/sign-in -H "Content-Type: application/json" -d "{\"email\":\"carlos@test.com\",\"password\":\"Test123!\"}"
```

**Respuesta Esperada:**
```json
{
  "id": 1,
  "email": "carlos@test.com",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**📝 IMPORTANTE:** Copia el `token` completo

---

### Paso 3: Crear un Vehículo
### Paso 2: Crear un Vehículo
### Paso 4: Ver el Catálogo
### Paso 4: Ver el Catálogo

**Comando Windows (CMD):**
```cmd
curl -X POST http://localhost:8080/api/v1/vehicles -H "Content-Type: application/json" -H "Authorization: Bearer <TU_TOKEN>" -d "{\"brand\":\"Toyota\",\"model\":\"Camry\",\"year\":2023,\"pricePerDay\":75.0,\"location\":\"Lima\",\"description\":\"Auto ejecutivo\",\"imageUrl\":\"https://example.com/car.jpg\",\"available\":true}"
```

**Respuesta Esperada:**
```json
{
  "id": 1,
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.0,
  "ownerId": 1,
  "createdAt": "2025-11-12T..."
}
```

---

### Paso 3: Ver el Catálogo

**Comando (no requiere autenticación):**
```cmd
curl http://localhost:8080/api/v1/vehicles
```

**Respuesta Esperada:**
```json
[
  {
    "id": 1,
    "brand": "Toyota",
    "model": "Camry",
    ...
  }
]
```

---

## ✅ ¡Listo! Tu Backend Funciona

Ahora puedes:
1. Ir a Swagger UI: http://localhost:8080/swagger-ui.html
2. Probar todos los endpoints
3. Ver la documentación completa en `EXAMPLE_TEST_GUIDE.md`

---

## 🔥 Ejemplo Completo con Swagger UI

### 1. Abre Swagger
```
http://localhost:8080/swagger-ui.html
```

### 2. Registrar Usuario
- Ve a **authentication-controller**
- POST `/api/v1/authentication/sign-up`
- Click "Try it out"
- Usa este JSON:
```json
{
  "name": "Carlos Pérez",
  "email": "carlos@test.com",
  "password": "Test123!",
  "role": "arrendador"
}
```
- Click "Execute"
- Guarda el `id` del usuario

### 3. Iniciar Sesión
- POST `/api/v1/authentication/sign-in`
- Click "Try it out"
- Usa este JSON:
```json
{
### 5. Crear Vehículo
  "password": "Test123!"
}
```
- Click "Execute"
- Copia el `token` de la respuesta

### 4. Autorizar Swagger
- Click en el botón verde "Authorize" 🔓 (arriba a la derecha)
- Escribe: `Bearer <pega_tu_token_aqui>`
- Click "Authorize"
- Click "Close"

### 4. Crear Vehículo
- Ve a **vehicles-controller**
- POST `/api/v1/vehicles`
- Click "Try it out"
- Usa este JSON:
```json
{
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.0,
  "location": "Lima",
  "description": "Auto ejecutivo",
  "imageUrl": "https://example.com/car.jpg",
  "available": true
}
```
- Click "Execute"
- Deberías ver Status 201 Created

---

## 📊 Campos de la API

### Sign-Up (/api/v1/authentication/sign-up)

| Campo | Tipo | Requerido | Ejemplo | Descripción |
|-------|------|-----------|---------|-------------|
| `name` | String | ✅ | "Carlos Pérez" | Nombre completo del usuario |
| `email` | String | ✅ | "carlos@test.com" | Correo electrónico (usado para login) |

| Rol | Valor para API | Prefijo Automático | Permisos |
|-----|----------------|-------------------|----------|
| Propietario | `"arrendador"` | → `ROLE_ARRENDADOR` | Crear vehículos, ver solicitudes |
| Arrendatario | `"arrendatario"` | → `ROLE_ARRENDATARIO` | Crear reservas, dejar reseñas |

---

## ❌ Errores Comunes

### Error: "Invalid role provided"
```json
{
  "error": "Invalid role provided. Must be 'arrendador' or 'arrendatario'."
}
```

**Causa:** Usaste `"ROLE_ARRENDADOR"` en lugar de `"arrendador"`

**Solución:** Usa solo `"arrendador"` o `"arrendatario"`

---

### Error: "Unauthorized request"
```
Full authentication is required to access this resource
```

**Causa:** Falta el token JWT o el header Authorization

**Solución:** 
1. Asegúrate de incluir: `Authorization: Bearer <token>`
2. Verifica que el token no haya expirado (7 días)

---

### Error: "Access Denied" o 403 Forbidden
```
{
  "error": "Forbidden"
}
```

**Causa:** El usuario no tiene el rol necesario para ese endpoint

**Solución:**
- Para crear vehículos: usa rol `"arrendador"`
- Para crear reservas: usa rol `"arrendatario"`

---

## 🎓 Próximos Pasos

1. ✅ Registrar un arrendatario con rol `"arrendatario"`
2. ✅ Crear una reserva para el vehículo
3. ✅ Dejar una reseña
4. ✅ Registrar telemetría

Ver guía completa en: `EXAMPLE_TEST_GUIDE.md`

---

## 📞 Ayuda Rápida

| Problema | Solución |
|----------|----------|
| Rol inválido | Usa `"arrendador"` o `"arrendatario"` (sin ROLE_) |
| Sin autorización | Añade header `Authorization: Bearer <token>` |
| Token expirado | Haz sign-in de nuevo |
| 403 Forbidden | Verifica que el usuario tenga el rol correcto |

---

**¿Listo para probar?** Ejecuta el Paso 1 y empieza a explorar tu backend 🚀

*Última actualización: 12 de noviembre de 2025*

