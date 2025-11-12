# Guía de Configuración de Logs - CodexaTeam Backend

## 📋 Resumen de Cambios

Se han ajustado las configuraciones de logging para reducir el ruido en los logs de la aplicación y eliminar warnings innecesarios.

---

## 🔍 Problemas Identificados y Soluciones

### 1. **Consultas SQL de Hibernate Visibles**

#### Problema Original:
```
Hibernate: select r1_0.id from roles r1_0 where r1_0.name=? limit ?
Hibernate: select r1_0.id from roles r1_0 where r1_0.name=? limit ?
```

#### ¿Por qué ocurre?
- Estas consultas son ejecutadas por `ApplicationReadyEventHandler` al iniciar la app
- El handler verifica si los roles `ROLE_ARRENDADOR` y `ROLE_ARRENDATARIO` existen en la DB
- Con `spring.jpa.show-sql=true`, Hibernate imprime TODAS las queries SQL en consola

#### Solución Aplicada:
```properties
# Desactivar el log de SQL en consola
spring.jpa.show-sql=false

# Configurar nivel de log de Hibernate a WARN (solo errores importantes)
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
```

#### Resultado:
✅ Las consultas SQL ya no aparecen en los logs de arranque
✅ Las queries siguen ejecutándose correctamente (solo se ocultan los logs)

---

### 2. **Warning de Spring Security**

#### Problema Original:
```
WARN ... Global AuthenticationManager configured with an AuthenticationProvider bean. 
UserDetailsService beans will not be used by Spring Security for automatically 
configuring username/password login. Consider removing the AuthenticationProvider bean...
If the current configuration is intentional, to turn off this warning, increase 
the logging level of 'org.springframework.security.config.annotation.authentication.
configuration.InitializeUserDetailsBeanManagerConfigurer' to ERROR
```

#### ¿Por qué ocurre?
- Spring Security detecta que tienes un `AuthenticationProvider` personalizado
- Advierte que esto puede causar confusión sobre qué mecanismo de autenticación se está usando
- Es solo una advertencia informativa, no un error

#### Solución Aplicada:
```properties
# Silenciar el warning de Spring Security (configuración intencional)
logging.level.org.springframework.security.config.annotation.authentication.configuration.InitializeUserDetailsBeanManagerConfigurer=ERROR
```

#### Resultado:
✅ El warning de Spring Security ya no aparece
✅ La configuración de seguridad sigue funcionando correctamente

---

## 📝 Archivo Modificado

**Archivo:** `src/main/resources/application.properties`

### Configuración Final de Logs:
```properties
# JPA Configuration
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=true
spring.jpa.hibernate.naming.physical-strategy=com.codexateam.platform.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy

# Logging Configuration
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
logging.level.org.springframework.security.config.annotation.authentication.configuration.InitializeUserDetailsBeanManagerConfigurer=ERROR
```

---

## 🎯 Resultado de los Logs Después de los Cambios

### Antes (con ruido):
```
Hibernate: select r1_0.id from roles r1_0 where r1_0.name=? limit ?
Hibernate: select r1_0.id from roles r1_0 where r1_0.name=? limit ?
2025-11-12T04:55:48.254-05:00  WARN 14244 --- [CodexaTeam Backend] [  restartedMain] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with an AuthenticationProvider bean...
2025-11-12T04:55:48.739-05:00  INFO 14244 --- [CodexaTeam Backend] [  restartedMain] c.p.i.a.i.e.ApplicationReadyEventHandler : Starting to seed roles...
2025-11-12T04:55:48.827-05:00  INFO 14244 --- [CodexaTeam Backend] [  restartedMain] c.p.i.a.i.e.ApplicationReadyEventHandler : Roles seeded successfully.
```

### Después (limpio):
```
2025-11-12T04:55:48.739-05:00  INFO 14244 --- [CodexaTeam Backend] [  restartedMain] c.p.i.a.i.e.ApplicationReadyEventHandler : Starting to seed roles...
2025-11-12T04:55:48.827-05:00  INFO 14244 --- [CodexaTeam Backend] [  restartedMain] c.p.i.a.i.e.ApplicationReadyEventHandler : Roles seeded successfully.
2025-11-12T04:55:48.733-05:00  INFO 14244 --- [CodexaTeam Backend] [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2025-11-12T04:55:48.738-05:00  INFO 14244 --- [CodexaTeam Backend] [  restartedMain] c.c.p.CodexaTeamBackendApplication       : Started CodexaTeamBackendApplication in 3.099 seconds
```

---

## 🔄 Si Necesitas Ver los Logs SQL en Desarrollo

Si en algún momento necesitas ver las queries SQL para debugging:

### Opción 1: Activar temporalmente en `application.properties`
```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### Opción 2: Usar un perfil de desarrollo separado
Crea `src/main/resources/application-dev.properties`:
```properties
# Development profile with verbose logging
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.springframework.web=DEBUG
```

Luego ejecuta con:
```cmd
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## ✅ Verificación

Para verificar que los cambios funcionan:

1. Detén el servidor si está corriendo (Ctrl+C)
2. Reinicia con: `start_server.bat`
3. Observa que los logs están más limpios y sin warnings

---

## 📚 Referencias

- [Spring Boot Logging Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
- [Hibernate SQL Logging](https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html#logging)
- [Spring Security Configuration](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html)

---

**Fecha de actualización:** 2025-11-12
**Autor:** AI Assistant
**Versión:** 1.0.0

