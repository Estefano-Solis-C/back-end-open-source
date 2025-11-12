# 🗑️ Guía para Resetear la Base de Datos

## 📋 Opciones Disponibles

### ✅ **Opción 1: Auto-Reset al Reiniciar (RECOMENDADO - YA CONFIGURADO)**

**Estado:** ✅ **YA ACTIVADO** - He cambiado `spring.jpa.hibernate.ddl-auto=create-drop` en `application.properties`

**¿Qué hace?**
- Borra TODAS las tablas cuando la aplicación se cierra
- Recrea TODAS las tablas cuando la aplicación inicia
- Empieza con una base de datos completamente limpia cada vez

**Cómo usar:**
1. **Detén la aplicación** (Ctrl+C en el terminal o detener en IntelliJ)
2. **Inicia la aplicación de nuevo**
3. ✅ ¡Base de datos limpia y lista!

**Ventajas:**
- ✅ Súper fácil - solo reinicia la aplicación
- ✅ No necesitas comandos SQL
- ✅ Perfecto para desarrollo y pruebas
- ✅ Siempre empiezas desde cero

**Desventajas:**
- ⚠️ Se borra TODO al cerrar la app
- ⚠️ NO usar en producción

---

### 🔧 **Opción 2: Reset Manual con SQL**

Si quieres mantener `ddl-auto=update` pero limpiar la base de datos manualmente:

#### Paso 1: Conectar a MySQL
```bash
mysql -u root -p
```
(Contraseña: `admin`)

#### Paso 2: Ejecutar comandos SQL
```sql
-- Ver las bases de datos
SHOW DATABASES;

-- Usar la base de datos
USE renticar_db;

-- Ver las tablas
SHOW TABLES;

-- Borrar todas las tablas (en orden correcto por las foreign keys)
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS telemetry;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- Verificar que se borraron
SHOW TABLES;

-- Salir
EXIT;
```

#### Paso 3: Reiniciar la aplicación
Las tablas se recrearán automáticamente cuando inicies la aplicación.

---

### 💥 **Opción 3: Borrar la Base de Datos Completa**

**⚠️ ADVERTENCIA:** Esto borra TODO incluyendo la estructura de la base de datos.

#### Paso 1: Conectar a MySQL
```bash
mysql -u root -p
```

#### Paso 2: Borrar la base de datos
```sql
-- Borrar la base de datos completa
DROP DATABASE IF EXISTS renticar_db;

-- Verificar que se borró
SHOW DATABASES;

-- Salir
EXIT;
```

#### Paso 3: Reiniciar la aplicación
La base de datos se recreará automáticamente gracias a `createDatabaseIfNotExist=true` en la URL.

---

## 🎯 ¿Cuál Opción Usar?

| Situación | Opción Recomendada |
|-----------|-------------------|
| **Estoy desarrollando y probando** | ✅ **Opción 1** (create-drop) - YA CONFIGURADA |
| **Quiero limpiar pero seguir trabajando** | Opción 1 - solo reinicia |
| **Tengo problemas con las tablas** | Opción 2 (SQL manual) |
| **Quiero empezar COMPLETAMENTE desde cero** | Opción 3 (borrar base de datos) |
| **Aplicación en producción** | ❌ NINGUNA - usa migraciones |

---

## 🚀 Reiniciar la Aplicación

### Desde el Terminal:
1. **Detener:** Presiona `Ctrl+C`
2. **Iniciar:** 
   ```bash
   mvnw.cmd spring-boot:run
   ```

### Desde IntelliJ IDEA:
1. **Detener:** Click en el botón rojo ⬛ (Stop)
2. **Iniciar:** Click en el botón verde ▶️ (Run)

---

## ✅ Verificar que Funcionó

Después de reiniciar, ve a Swagger y:

1. Intenta hacer **sign-up** con Carlos de nuevo
2. Si funciona sin error de "email already exists", ✅ ¡la base de datos se reseteó!

---

## 📝 Notas Importantes

### Sobre `ddl-auto` values:

- **`create-drop`**: Borra y recrea tablas en cada inicio/cierre (✅ YA CONFIGURADO)
  - 👍 Perfecto para desarrollo
  - ⚠️ Se pierde todo al cerrar

- **`create`**: Borra y recrea tablas solo al iniciar
  - 👍 Los datos persisten mientras la app esté corriendo
  - ⚠️ Se pierden al reiniciar

- **`update`**: Actualiza el esquema sin borrar datos
  - 👍 Los datos persisten entre reinicios
  - ⚠️ Puede causar inconsistencias

- **`validate`**: Solo valida el esquema
  - 👍 Seguro para producción
  - ⚠️ No crea tablas automáticamente

- **`none`**: No hace nada
  - 👍 Control total manual
  - ⚠️ Debes crear tablas manualmente

### Configuración Actual:
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

✅ **Perfecto para desarrollo y testing**

---

## 🔄 Volver a Configuración Normal

Si quieres que los datos persistan entre reinicios:

1. Abre `src/main/resources/application.properties`
2. Cambia la línea:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```
3. Reinicia la aplicación

---

## 🆘 Troubleshooting

### No se borran las tablas con create-drop

**Posibles causas:**
- La aplicación no se cerró correctamente
- Hay conexiones activas a la base de datos

**Solución:**
```sql
-- Conectar a MySQL
mysql -u root -padmin

-- Matar conexiones activas
SHOW PROCESSLIST;
KILL <process_id>;  -- Reemplaza con el ID que muestra PROCESSLIST

-- Luego usa Opción 2 o 3
```

### Error: "Table doesn't exist"

**Solución:** Simplemente reinicia la aplicación, las tablas se crearán automáticamente.

### Error: "Access denied for user 'root'"

**Solución:** Verifica tu contraseña de MySQL:
```properties
spring.datasource.password=admin
```

---

## 🎉 ¡Listo!

Ahora puedes resetear tu base de datos fácilmente:
1. ✅ **Detén la aplicación** (Ctrl+C)
2. ✅ **Inicia la aplicación** (mvnw.cmd spring-boot:run)
3. ✅ **Base de datos limpia**

¡Disfruta del desarrollo sin preocuparte por datos antiguos! 🚀

