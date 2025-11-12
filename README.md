# CodexaTeam Backend Platform 🚗

Backend API RESTful para plataforma de alquiler de vehículos P2P (Peer-to-Peer) construida con Spring Boot 3.3.0 y arquitectura DDD (Domain-Driven Design).

## 📋 Tabla de Contenidos
- [Características](#-características)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Documentación API](#-documentación-api)
- [Endpoints Principales](#-endpoints-principales)
- [Seguridad](#-seguridad)
- [Estado del Proyecto](#-estado-del-proyecto)

## ✨ Características

### Funcionalidades Implementadas ✅
- 🔐 **Autenticación JWT** - Sistema completo de registro y login con tokens
- 👥 **Gestión de Usuarios** - Roles diferenciados (Arrendador/Arrendatario)
- 🚙 **Catálogo de Vehículos** - CRUD completo con búsqueda y filtros
- 📅 **Sistema de Reservas** - Bookings con validación de fechas y disponibilidad
- ⭐ **Reseñas y Ratings** - Sistema de calificación de vehículos
- 📍 **Telemetría IoT** - Tracking GPS en tiempo real de vehículos
- 📚 **Documentación Swagger** - API autodocumentada
- 🛡️ **Seguridad Robusta** - Validación multicapa y autorización por roles

## 🛠 Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 3.3.0 | Framework backend |
| Spring Security | 6.3.0 | Autenticación y autorización |
| Spring Data JPA | 3.3.0 | Capa de persistencia |
| MySQL | 8.x | Base de datos |
| JWT (jjwt) | 0.12.6 | Tokens de autenticación |
| SpringDoc OpenAPI | 2.6.0 | Documentación API |
| BCrypt | - | Hash de contraseñas |
| Lombok | 1.18.34 | Reducción de boilerplate |
| Maven | 3.9+ | Gestión de dependencias |

## 🏗 Arquitectura

### Bounded Contexts (DDD)
El proyecto está organizado en 5 dominios principales:

```
src/main/java/com/codexateam/platform/
├── iam/              # Identity and Access Management
├── listings/         # Catálogo de Vehículos
├── booking/          # Sistema de Reservas
├── reviews/          # Reseñas y Ratings
├── iot/              # Telemetría IoT
└── shared/           # Componentes Compartidos
```

### Estructura por Capas (Hexagonal Architecture)

Cada bounded context sigue esta estructura:

```
domain/
├── application/      # Casos de uso y servicios de aplicación
│   └── internal/
│       ├── commandservices/    # Comandos (write operations)
│       ├── queryservices/      # Consultas (read operations)
│       └── outboundservices/   # ACL para otros dominios
├── domain/          # Lógica de negocio pura
│   ├── model/
│   │   ├── aggregates/         # Entidades raíz
│   │   ├── commands/           # DTOs para comandos
│   │   ├── queries/            # DTOs para consultas
│   │   └── valueobjects/       # Value Objects
│   └── services/               # Interfaces de servicios
├── infrastructure/  # Implementaciones técnicas
│   └── persistence/
│       └── jpa/
│           └── repositories/   # Repositorios JPA
└── interfaces/      # Capa de presentación
    └── rest/
        ├── resources/          # DTOs de API
        ├── transform/          # Mappers/Assemblers
        └── *Controller.java    # REST Controllers
```

## 📦 Requisitos Previos

- ☕ **Java 21** o superior
- 🗄️ **MySQL 8.0** o superior
- 📦 **Maven 3.9+** (incluido con wrapper)
- 🔧 **IDE** recomendado: IntelliJ IDEA, Eclipse, o VS Code

## 🚀 Instalación

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/codexateam-backend.git
cd codexateam-backend
```

### 2. Configurar Base de Datos MySQL
```sql
CREATE DATABASE renticar_db;
CREATE USER 'codexateam_user'@'localhost' IDENTIFIED BY 'tu_password';
GRANT ALL PRIVILEGES ON renticar_db.* TO 'codexateam_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configurar Variables de Entorno (Opcional)
```bash
# Windows
set SPRING_DATASOURCE_USERNAME=codexateam_user
set SPRING_DATASOURCE_PASSWORD=tu_password
set JWT_SECRET=tu_clave_secreta_minimo_256_bits

# Linux/Mac
export SPRING_DATASOURCE_USERNAME=codexateam_user
export SPRING_DATASOURCE_PASSWORD=tu_password
export JWT_SECRET=tu_clave_secreta_minimo_256_bits
```

## ⚙ Configuración

### application.properties
Edita `src/main/resources/application.properties`:

```properties
# Datasource
spring.datasource.url=jdbc:mysql://localhost:3306/renticar_db
spring.datasource.username=root
spring.datasource.password=admin

# JWT
authorization.jwt.secret=MyVerySecureSecretKey...
authorization.jwt.expiration.days=7

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**Nota:** Para producción, usa variables de entorno en lugar de credenciales hardcodeadas.

## ▶ Ejecución

### Compilar el Proyecto
```bash
# Windows
mvnw.cmd clean compile

# Linux/Mac
./mvnw clean compile
```

### Ejecutar la Aplicación
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Verificar que está Funcionando
```bash
curl http://localhost:8090/actuator/health
```

Respuesta esperada:
```json
{"status":"UP"}
```

## 🗑️ Reset de Base de Datos

### Opción 1: Auto-Reset al Reiniciar (Recomendado para Desarrollo)

El proyecto está configurado con `spring.jpa.hibernate.ddl-auto=create-drop` para reseteo automático.

**¿Cómo funciona?**
- Borra todas las tablas al cerrar la aplicación
- Recrea todas las tablas al iniciar
- Perfecto para desarrollo y testing

**Para usar:**
1. Detén la aplicación (Ctrl+C)
2. Inicia la aplicación de nuevo
3. ✅ Base de datos limpia

### Opción 2: Script de Reset Manual

Ejecuta el script de Windows:
```bash
reset_database.bat
```

### Opción 3: Reset Manual con SQL

```sql
mysql -u root -p
USE renticar_db;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS telemetry;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
EXIT;
```

Luego reinicia la aplicación para recrear las tablas.

📖 **Guía Completa:** Ver [DATABASE_RESET_GUIDE.md](DATABASE_RESET_GUIDE.md)

## 📚 Documentación API

### Swagger UI
Una vez iniciada la aplicación, accede a:

```
http://localhost:8080/swagger-ui.html
```

### 🎯 Tutorial Completo Paso a Paso
Para aprender a usar todos los endpoints con ejemplos funcionales, consulta:

📘 **[SWAGGER_TUTORIAL_COMPLETO.md](SWAGGER_TUTORIAL_COMPLETO.md)** - Tutorial completo con 22 pasos que cubre:
- ✅ Registro e inicio de sesión
- ✅ Creación de vehículos
- ✅ Sistema de reservas completo
- ✅ Confirmación y rechazo de reservas
- ✅ Telemetría IoT
- ✅ Reseñas y ratings
- ✅ Todos los datos de ejemplo ya corregidos

### OpenAPI JSON
```
http://localhost:8080/v3/api-docs
```

## 🌐 Endpoints Principales

### Autenticación (IAM)
```http
POST /api/v1/authentication/sign-up
POST /api/v1/authentication/sign-in
GET  /api/v1/users/{id}
```

### Vehículos (Listings)
```http
GET  /api/v1/vehicles                    # Público
GET  /api/v1/vehicles/{id}               # Público
POST /api/v1/vehicles                    # ARRENDADOR
GET  /api/v1/vehicles/my-listings        # ARRENDADOR
```

### Reservas (Booking)
```http
POST /api/v1/bookings                    # ARRENDATARIO
GET  /api/v1/bookings/my-bookings        # ARRENDATARIO
GET  /api/v1/bookings/my-requests        # ARRENDADOR
```

### Reseñas (Reviews)
```http
POST /api/v1/reviews                     # ARRENDATARIO
GET  /api/v1/reviews/vehicle/{id}        # Público
GET  /api/v1/reviews/my-reviews          # ARRENDATARIO
```

### Telemetría (IoT)
```http
POST /api/v1/telemetry                   # ARRENDADOR
GET  /api/v1/telemetry/vehicle/{id}      # ARRENDADOR/ARRENDATARIO
```

## 🔐 Seguridad

### Autenticación JWT
Todos los endpoints protegidos requieren un token JWT en el header:
```http
Authorization: Bearer <tu_token_jwt>
```

### Flujo de Autenticación
1. **Sign-up**: Registrar usuario con email, password y roles
2. **Sign-in**: Obtener token JWT
3. **Usar token**: Incluir en header Authorization para endpoints protegidos

### Ejemplo con cURL
```bash
# 1. Sign-up
curl -X POST http://localhost:8080/api/v1/authentication/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "propietario@email.com",
    "password": "Password123!",
    "roles": ["ROLE_ARRENDADOR"]
  }'

# 2. Sign-in
curl -X POST http://localhost:8080/api/v1/authentication/sign-in \
  -H "Content-Type: application/json" \
  -d '{
    "username": "propietario@email.com",
    "password": "Password123!"
  }'

# Respuesta: {"id": 1, "username": "...", "token": "eyJhbGc..."}

# 3. Usar token
curl -X GET http://localhost:8080/api/v1/vehicles/my-listings \
  -H "Authorization: Bearer eyJhbGc..."
```

### Roles Disponibles
- **ROLE_ARRENDADOR** - Propietarios de vehículos
- **ROLE_ARRENDATARIO** - Usuarios que alquilan vehículos

## 📊 Estado del Proyecto

### ✅ Implementado (100%)
- [x] Arquitectura DDD completa
- [x] Autenticación y autorización JWT
- [x] CRUD de vehículos con imágenes
- [x] Sistema de reservas con validación de fechas
- [x] Reseñas y ratings
- [x] Telemetría IoT básica
- [x] Documentación Swagger
- [x] Validación de seguridad multicapa
- [x] Anti-Corruption Layers (ACL) entre dominios

### 🚧 Mejoras Futuras (Opcionales)
- [ ] Confirmación/cancelación de bookings por propietarios
- [ ] Validación de reservas superpuestas
- [ ] Notificaciones (email/push)
- [ ] Pagos integrados (Stripe/PayPal)
- [ ] Tests unitarios e integración
- [ ] Paginación en listados
- [ ] Filtros avanzados de búsqueda
- [ ] Subida de imágenes a cloud storage

## 📁 Documentación Adicional

- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) - Estado detallado de implementación
- [SECURITY_IMPROVEMENTS.md](SECURITY_IMPROVEMENTS.md) - Mejoras de seguridad realizadas
- [JWT_AUTHENTICATION_GUIDE.md](JWT_AUTHENTICATION_GUIDE.md) - Guía de autenticación
- [WARNINGS_FIXES.md](WARNINGS_FIXES.md) - Correcciones de warnings

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👥 Autores

- **CodexaTeam** - [GitHub](https://github.com/codexateam)

## 🙏 Agradecimientos

- Spring Framework Team
- Domain-Driven Design Community
- PlantSync Architecture Reference

---

**¿Preguntas o problemas?** Abre un [issue](https://github.com/tu-usuario/codexateam-backend/issues)

**¿Listo para comenzar?** Sigue la [guía de instalación](#-instalación)

---

*Última actualización: 12 de noviembre de 2025*

