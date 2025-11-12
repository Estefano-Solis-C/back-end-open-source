# 📋 Referencia Rápida: Sign-Up API

## ✅ Formato Correcto para Sign-Up

### Request Body (lo que envías)

```json
{
  "email": "usuario@test.com",
  "password": "Password123!",
  "name": "Nombre del Usuario",
  "role": "arrendador"
}
```

**⚠️ Importante:**
- Usa `"role"` (singular, sin 's')
- Es un string, NO un array
- Usa minúsculas: `"arrendador"` o `"arrendatario"`
- NO uses prefijo `ROLE_`

---

## ✅ Respuesta del Servidor

```json
{
  "id": 1,
  "email": "usuario@test.com",
  "name": "Nombre del Usuario",
  "roles": ["ROLE_ARRENDADOR"]
}
```

**Nota:** El servidor devuelve `"roles"` (plural) como array con el prefijo `ROLE_`

---

## 🎯 Valores Válidos

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| `"arrendador"` | Propietario | Crear vehículos, ver solicitudes, registrar telemetría |
| `"arrendatario"` | Arrendatario | Buscar vehículos, crear reservas, ver telemetría de reservados |

---

## ✅ Ejemplos Completos

### Ejemplo 1: Propietario (Carlos)

**Request:**
```json
{
  "email": "carlos.owner@test.com",
  "password": "Carlos123!",
  "name": "Carlos Owner",
  "role": "arrendador"
}
```

**Response:**
```json
{
  "id": 1,
  "email": "carlos.owner@test.com",
  "name": "Carlos Owner",
  "roles": ["ROLE_ARRENDADOR"]
}
```

### Ejemplo 2: Arrendatario (María)

**Request:**
```json
{
  "email": "maria.renter@test.com",
  "password": "Maria123!",
  "name": "Maria Renter",
  "role": "arrendatario"
}
```

**Response:**
```json
{
  "id": 2,
  "email": "maria.renter@test.com",
  "name": "Maria Renter",
  "roles": ["ROLE_ARRENDATARIO"]
}
```

---

## ❌ Errores Comunes

### Error 1: Usar "roles" (plural)
```json
{
  "roles": ["arrendador"]  ❌ INCORRECTO
}
```
**Corrección:**
```json
{
  "role": "arrendador"     ✅ CORRECTO
}
```

### Error 2: Usar array
```json
{
  "role": ["arrendador"]   ❌ INCORRECTO
}
```
**Corrección:**
```json
{
  "role": "arrendador"     ✅ CORRECTO
}
```

### Error 3: Usar prefijo ROLE_
```json
{
  "role": "ROLE_ARRENDADOR"  ❌ INCORRECTO
}
```
**Corrección:**
```json
{
  "role": "arrendador"       ✅ CORRECTO
}
```

### Error 4: Usar mayúsculas
```json
{
  "role": "ARRENDADOR"     ❌ INCORRECTO
}
```
**Corrección:**
```json
{
  "role": "arrendador"     ✅ CORRECTO
}
```

---

## 🔍 Schema del API

### POST /api/v1/authentication/sign-up

**Request Body:**
```
{
  "name": "string",       // Nombre completo
  "email": "string",      // Email válido
  "password": "string",   // Mínimo 8 caracteres, mayúscula, minúscula, número, símbolo
  "role": "string"        // "arrendador" o "arrendatario"
}
```

**Response (201 Created):**
```
{
  "id": integer,          // ID del usuario
  "name": "string",       // Nombre completo
  "email": "string",      // Email
  "roles": [string]       // Array con roles (ej: ["ROLE_ARRENDADOR"])
}
```

---

## 📝 Validaciones

### Email
- ✅ Formato válido: `usuario@dominio.com`
- ❌ Debe ser único (no duplicado)

### Password
- ✅ Mínimo 8 caracteres
- ✅ Al menos 1 mayúscula
- ✅ Al menos 1 minúscula
- ✅ Al menos 1 número
- ✅ Al menos 1 símbolo especial (!@#$%^&*)

### Role
- ✅ Solo acepta: `"arrendador"` o `"arrendatario"`
- ❌ Case-sensitive (minúsculas obligatorias)

---

## 🧪 Probar con Swagger UI

1. Abre: `http://localhost:8080/swagger-ui.html`
2. Busca: **authentication-controller**
3. Endpoint: **POST /api/v1/authentication/sign-up**
4. Click: **"Try it out"**
5. Pega el JSON de ejemplo
6. Click: **"Execute"**

---

## 🧪 Probar con cURL (Windows CMD)

```cmd
curl -X POST http://localhost:8080/api/v1/authentication/sign-up -H "Content-Type: application/json" -d "{\"email\":\"carlos@test.com\",\"password\":\"Carlos123!\",\"name\":\"Carlos\",\"role\":\"arrendador\"}"
```

## 🧪 Probar con PowerShell

```powershell
$body = @{
    email = "carlos@test.com"
    password = "Carlos123!"
    name = "Carlos"
    role = "arrendador"
} | ConvertTo-Json

Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/authentication/sign-up" -ContentType "application/json" -Body $body
```

---

## 📚 Referencias

- **Tutorial completo:** `SWAGGER_TUTORIAL_COMPLETO.md`
- **Guía Swagger:** `SWAGGER_GUIDE.md`
- **Inicio rápido:** `QUICK_START.md`

---

## 🎯 Resumen Ultra-Rápido

```json
// ✅ Envías (Request):
{
  "role": "arrendador"
}

// ✅ Recibes (Response):
{
  "roles": ["ROLE_ARRENDADOR"]
}
```

**¡Recuerda: Request = singular, Response = plural!** 🚀

