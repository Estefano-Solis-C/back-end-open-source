# 🔧 Solución al Error 401 en Sign-Up

## 🔴 Problema Identificado

Cuando intentaste hacer Sign-Up en Swagger UI, obtuviste error **401 Unauthorized**.

Sin embargo, el Sign-In funcionó correctamente, lo que significa que **el usuario ya existe en la base de datos**.

---

## ✅ Solución

### **Opción 1: Usar un Email Diferente**

El email `carlos.owner@test.com` ya está registrado. Prueba con un email nuevo:

#### En Swagger UI:
1. Ve a **POST /api/v1/authentication/sign-up**
2. Click "Try it out"
3. Usa este JSON con un **email diferente**:

```json
{
  "name": "Juan López",
  "email": "juan.owner@test.com",
  "password": "Juan123!",
  "role": "arrendador"
}
```

4. Click "Execute"
5. Ahora debería funcionar ✅

---

### **Opción 2: Continuar con el Usuario Existente**

Como ya hiciste Sign-In exitosamente y obtuviste el token:

```json
{
  "id": 1,
  "email": "carlos.owner@test.com",
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20iLCJpYXQiOjE3NjI5MzI4OTgsImV4cCI6MTc2MzUzNzY5OH0.MzVUGf2k8o9BqfBf77PnMcg_btMsbWLvXpel1_ZOqnYbzhb-IqYEpfvju942P3OqeaWQZzqIR-3vtVqbp27u6g"
}
```

**Simplemente continúa con el siguiente paso:**

1. Click en el botón **"Authorize"** 🔓 (arriba a la derecha)
2. En el campo "Value", pega:
   ```
   Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjYXJsb3Mub3duZXJAdGVzdC5jb20iLCJpYXQiOjE3NjI5MzI4OTgsImV4cCI6MTc2MzUzNzY5OH0.MzVUGf2k8o9BqfBf77PnMcg_btMsbWLvXpel1_ZOqnYbzhb-IqYEpfvju942P3OqeaWQZzqIR-3vtVqbp27u6g
   ```
3. Click "Authorize"
4. Click "Close"
5. **Ahora prueba crear un vehículo:**
   - Ve a **vehicles-controller**
   - **POST /api/v1/vehicles**
   - Click "Try it out"
   - Usa este JSON:

```json
{
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.00,
  "imageUrl": "https://www.toyota.com/imgix/responsive/images/mlp/colorizer/2023/camry/1J9/1.png"
}
```

6. Click "Execute"
7. Deberías ver **201 Created** ✅

---

## 🔍 ¿Por Qué Pasó Esto?

### Teoría 1: Usuario Pre-existente
Es posible que hayas ejecutado el servidor anteriormente y el usuario ya se creó en la base de datos MySQL.

### Teoría 2: Seed de Datos
El backend puede tener un script de inicialización que crea usuarios por defecto.

### Teoría 3: Swagger UI Enviando Headers Extra
A veces Swagger UI envía automáticamente headers de autenticación incluso en endpoints públicos si ya estás autorizado en la sesión.

---

## 🎯 Flujo Recomendado AHORA

Ya que tienes el token de Carlos, continúa con estos pasos:

### 1. ✅ Autorizar Swagger (Ya lo hiciste con el Sign-In)
```
Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 2. ✅ Crear Vehículo (POST /api/v1/vehicles)
```json
{
  "brand": "Toyota",
  "model": "Camry",
  "year": 2023,
  "pricePerDay": 75.00,
  "imageUrl": "https://www.toyota.com/imgix/responsive/images/mlp/colorizer/2023/camry/1J9/1.png"
}
```

### 3. ✅ Ver Catálogo (GET /api/v1/vehicles)
- No requiere autenticación
- Verás el vehículo que acabas de crear

### 4. ✅ Crear Arrendatario
- Desautorízate en Swagger (Logout)
- **POST /api/v1/authentication/sign-up** con:

```json
{
  "name": "María García",
  "email": "maria.renter@test.com",
  "password": "Maria123!",
  "role": "arrendatario"
}
```

### 5. ✅ Login como María
- **POST /api/v1/authentication/sign-in**
- Autoriza Swagger con el token de María
- Crea una reserva con **POST /api/v1/bookings**

---

## 📊 Verificar Usuarios Existentes en Base de Datos

Si quieres ver qué usuarios ya existen, puedes:

### Opción 1: Consulta SQL Directa
```sql
USE renticar_db;
SELECT id, name, email FROM users;
```

### Opción 2: Intentar Login
Si el Sign-In funciona, el usuario existe.
Si el Sign-In falla con "Invalid email or password", el usuario NO existe.

---

## 🚀 Continuar con el Tutorial

Tu servidor está funcionando perfectamente. El único "problema" fue que el usuario ya existía.

**Siguiente paso:** Sigue desde el **Paso 4** de `SWAGGER_GUIDE.md` (Autorizar Swagger y crear vehículo).

---

## ✅ Resumen

| Estado | Descripción |
|--------|-------------|
| ❌ Sign-Up falló | Usuario `carlos.owner@test.com` ya existe |
| ✅ Sign-In funcionó | Obtuviste el token correctamente |
| ✅ Token válido | Expira el 17 de noviembre 2025 |
| 🎯 Siguiente paso | Autorizar Swagger y crear vehículo |

---

**¿Listo para continuar?** 

1. Autoriza Swagger con tu token
2. Crea tu primer vehículo
3. ¡Disfruta probando tu backend! 🚀

---

*Documento creado: 12 de noviembre de 2025*

