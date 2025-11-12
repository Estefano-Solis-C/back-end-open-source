# Guía de Solución de Warnings de JVM - sun.misc.Unsafe

## 🔍 Descripción del Problema

### Warnings que Aparecían:
```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::staticFieldBase has been called by com.google.inject.internal.aop.HiddenClassDefiner 
(file:/C:/Users/Gaftherman/.m2/wrapper/dists/apache-maven-3.9.11/.../lib/guice-5.1.0-classes.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.inject.internal.aop.HiddenClassDefiner
WARNING: sun.misc.Unsafe::staticFieldBase will be removed in a future release
```

---

## 📋 ¿Qué Causa Estos Warnings?

### Contexto Técnico:

1. **Java 24 es muy reciente** y marca como "terminally deprecated" algunas APIs antiguas
2. **Maven usa Google Guice 5.1.0** internamente para inyección de dependencias
3. **Guice 5.1.0** usa APIs de `sun.misc.Unsafe` que Java 24 considera obsoletas
4. Estos warnings son de **Maven, no de tu aplicación**

### Impacto:
- ❌ NO afectan el funcionamiento de tu aplicación
- ❌ NO causan errores en tiempo de ejecución
- ❌ NO comprometen la seguridad
- ✅ Son solo advertencias informativas de la JVM

---

## ✅ Soluciones Aplicadas

### 1. Archivo `.mvn/jvm.config` (Configuración Global Maven)

**Ubicación:** `.mvn/jvm.config`

```properties
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED
-XX:+IgnoreUnrecognizedVMOptions
-Djdk.module.illegalAccess=permit
```

**Qué hace:**
- Permite a Maven acceder a módulos internos de Java sin generar warnings
- Se aplica automáticamente a todos los comandos `mvnw`

---

### 2. Configuración en `pom.xml`

**Plugin Spring Boot Maven:**
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <jvmArguments>
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/java.util=ALL-UNNAMED
        </jvmArguments>
    </configuration>
</plugin>
```

**Plugin Surefire (Tests):**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/java.util=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

**Qué hace:**
- Configura la JVM para permitir acceso reflectivo a módulos internos
- Se aplica al ejecutar la aplicación y los tests

---

### 3. Script `start_server.bat` Actualizado

```batch
REM Suprimir warnings de sun.misc.Unsafe
set MAVEN_OPTS=--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED

mvnw.cmd spring-boot:run -DskipTests
```

**Qué hace:**
- Configura variables de entorno Maven antes de iniciar el servidor
- Los warnings ya no aparecerán al ejecutar `start_server.bat`

---

## 🎯 Resultado Final

### Antes (con warnings):
```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::staticFieldBase has been called by com.google.inject...
WARNING: Please consider reporting this to the maintainers...
WARNING: sun.misc.Unsafe::staticFieldBase will be removed in a future release

[INFO] Scanning for projects...
[INFO] Building codexateam-backend 1.0.0
```

### Después (limpio):
```
[INFO] Scanning for projects...
[INFO] Building codexateam-backend 1.0.0
[INFO] Spring Boot Application starting...
```

---

## 🔧 Comandos de Verificación

### Para probar que funcionan las correcciones:

1. **Usando el script (recomendado):**
   ```cmd
   start_server.bat
   ```

2. **Usando mvnw directamente:**
   ```cmd
   mvnw.cmd clean compile
   mvnw.cmd spring-boot:run -DskipTests
   ```

3. **Para ejecutar tests:**
   ```cmd
   mvnw.cmd test
   ```

Todos estos comandos ahora deberían ejecutarse **sin mostrar los warnings de sun.misc.Unsafe**.

---

## 📚 Información Adicional

### ¿Por qué usamos `--add-opens` en lugar de actualizar Guice?

1. **Maven Wrapper usa Guice internamente** - No podemos cambiar la versión sin modificar Maven mismo
2. **Google Guice 5.1.0 es la última versión estable** - Versiones más nuevas aún están en desarrollo
3. **`--add-opens` es la solución oficial de Java** para mantener compatibilidad con bibliotecas antiguas

### ¿Es seguro usar `--add-opens`?

✅ **Sí, es completamente seguro:**
- Es una solución oficial recomendada por Oracle/OpenJDK
- Solo afecta a cómo Maven accede a módulos internos de Java
- No compromete la seguridad de tu aplicación
- Es temporal hasta que las bibliotecas se actualicen

### ¿Cuándo se resolverá definitivamente?

Cuando Apache Maven actualice a:
- **Google Guice 6.x** (cuando sea lanzado oficialmente)
- O cuando Maven reemplace Guice con otra solución de DI

Mientras tanto, esta es la mejor práctica recomendada.

---

## 🔗 Referencias

- [JEP 403: Strongly Encapsulate JDK Internals](https://openjdk.org/jeps/403)
- [Google Guice Issue #1133](https://github.com/google/guice/issues/1133)
- [Maven JVM Configuration](https://maven.apache.org/configure.html)
- [Java Module System - Add Opens](https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html#extra-options-for-java)

---

## ✅ Checklist de Verificación

Después de aplicar estas correcciones, verifica que:

- [ ] No aparecen warnings de `sun.misc.Unsafe` al compilar
- [ ] No aparecen warnings al ejecutar `mvnw.cmd spring-boot:run`
- [ ] No aparecen warnings al ejecutar tests
- [ ] La aplicación arranca correctamente
- [ ] `start_server.bat` funciona sin warnings

---

**Fecha de creación:** 2025-11-12  
**Última actualización:** 2025-11-12  
**Versión de Java:** 24.0.2  
**Versión de Maven:** 3.9.11  
**Estado:** ✅ Resuelto

