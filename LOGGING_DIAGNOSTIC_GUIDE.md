# 🔍 Guía de Diagnóstico - Logging Mejorado para Simulación de Telemetría

## 📋 Resumen de Mejoras

Se ha mejorado el logging en **3 archivos clave** para diagnosticar por qué la simulación usa rutas de fallback (líneas rectas) en lugar de rutas reales de OpenRouteService:

1. **OpenRouteServiceApiClient.java** - Cliente API con detección de errores HTTP específicos
2. **TelemetrySimulatorService.java** - Servicio de simulación con advertencias claras
3. **AutomaticTelemetryGeneratorService.java** - Servicio autónomo con logging de fallback

---

## 🚨 Mensajes de Error que Verás

### ❌ Caso 1: API Key NO Configurada

```log
════════════════════════════════════════════════════════════════
❌ OpenRouteService API Key is NOT CONFIGURED
❌ Please set 'openrouteservice.api.key' in application.properties
❌ Get your FREE API key at: https://openrouteservice.org/dev/#/signup
════════════════════════════════════════════════════════════════
⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa no está configurada
```

**Solución:** Añade tu API key en `application.properties`:
```properties
openrouteservice.api.key=TU_API_KEY_AQUI
```

---

### ❌ Caso 2: Error 401 Unauthorized (API Key Inválida)

```log
════════════════════════════════════════════════════════════════
❌ HTTP ERROR calling OpenRouteService API
   Status Code: 401 - UNAUTHORIZED
   Response Body: {"error":{"code":401,"message":"Invalid API key"}}
   ════════════════════════════════════════════════
   🔑 ERROR 401 UNAUTHORIZED
   Your API Key is INVALID or MISSING
   Current API Key: 5ba3b***
   ════════════════════════════════════════════════
   📋 SOLUTION:
   1. Go to: https://openrouteservice.org/dev/#/signup
   2. Sign up and get your FREE API key
   3. Add to application.properties:
      openrouteservice.api.key=YOUR_API_KEY_HERE
   ════════════════════════════════════════════════
════════════════════════════════════════════════════════════════
⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa falló
```

**Solución:** Verifica que tu API key sea correcta y esté activa en https://openrouteservice.org/dev/#/home

---

### ❌ Caso 3: Error 403 Forbidden (Cuota Excedida)

```log
════════════════════════════════════════════════════════════════
❌ HTTP ERROR calling OpenRouteService API
   Status Code: 403 - FORBIDDEN
   Response Body: {"error":{"code":403,"message":"Daily quota exceeded"}}
   🚫 ERROR 403 FORBIDDEN
   Your API Key doesn't have permission or daily quota exceeded
   Check your account at: https://openrouteservice.org/dev/#/home
════════════════════════════════════════════════════════════════
⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa falló
```

**Solución:** Espera hasta mañana o actualiza a un plan con mayor cuota. Límites del plan gratuito:
- 40 requests/minuto
- 2000 requests/día

---

### ❌ Caso 4: Error 429 Too Many Requests (Rate Limit)

```log
════════════════════════════════════════════════════════════════
❌ HTTP ERROR calling OpenRouteService API
   Status Code: 429 - TOO_MANY_REQUESTS
   Response Body: {"error":{"code":429,"message":"Rate limit exceeded"}}
   ⏱️  ERROR 429 TOO MANY REQUESTS
   Rate limit exceeded. Free tier: 40 requests/minute, 2000 requests/day
════════════════════════════════════════════════════════════════
⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa falló
```

**Solución:** Reduce la frecuencia de solicitudes o implementa un cache de rutas.

---

### ❌ Caso 5: Error de Red (Sin Internet)

```log
════════════════════════════════════════════════════════════════
❌ NETWORK ERROR calling OpenRouteService API
   Error: I/O error on GET request for "https://api.openrouteservice.org/v2/directions/driving-car"
   Cannot reach: https://api.openrouteservice.org/v2/directions/driving-car
   Check your internet connection
════════════════════════════════════════════════════════════════
⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa falló
```

**Solución:** Verifica tu conexión a internet y que no haya firewall bloqueando la API.

---

### ⚠️ Caso 6: Usando Fallback Route (Línea Recta)

Cada vez que se genera una ruta de fallback (simulada), verás:

```log
════════════════════════════════════════════════════════════════
⚠️  ATENCIÓN: Generando ruta simulada (LÍNEA RECTA)
⚠️  Motivo: La API de OpenRouteService falló o no está configurada
⚠️  El vehículo NO seguirá las calles reales
════════════════════════════════════════════════════════════════
Generating FALLBACK route: distance=8542.35 meters, 854 points (STRAIGHT LINE)
```

---

## ✅ Caso Exitoso: API Funcionando Correctamente

Cuando todo funciona bien, verás:

```log
🌐 Requesting route from OpenRouteService API
   Start: (-12.0464, -77.0428) -> End: (-12.119, -77.029)
   URL: https://api.openrouteservice.org/v2/directions/driving-car (api_key hidden)
✅ Successfully retrieved 342 coordinate points from OpenRouteService (full street geometry)
Vehículo 1 planificó ruta con 35 puntos
```

**Esto significa:** El vehículo seguirá las calles reales de Lima con curvas y giros.

---

## 🔧 Cómo Configurar tu API Key

### Paso 1: Obtén tu API Key GRATUITA

1. Ve a: https://openrouteservice.org/dev/#/signup
2. Regístrate con tu email
3. Confirma tu email
4. Inicia sesión en: https://openrouteservice.org/dev/#/home
5. Haz clic en "Request a Token"
6. Copia tu API Key

### Paso 2: Configura en application.properties

Abre `src/main/resources/application.properties` y añade:

```properties
# OpenRouteService API Configuration
openrouteservice.api.key=5b3ce3597851110001cf6248a1b2c3d4e5f6g7h8i9j0k1l2m3n4
```

**IMPORTANTE:** Reemplaza con tu API key real (el ejemplo anterior es ficticio).

### Paso 3: Reinicia la Aplicación

```bash
./mvnw spring-boot:run
```

---

## 📊 Niveles de Logging

Los mensajes de diagnóstico usan estos niveles:

| Nivel | Uso | Ejemplo |
|-------|-----|---------|
| `ERROR` | Errores críticos (API Key inválida, HTTP errors) | ❌ ERROR 401 UNAUTHORIZED |
| `WARN` | Advertencias (usando fallback) | ⚠️ ATENCIÓN: Usando ruta simulada |
| `INFO` | Información normal (rutas exitosas) | ✅ Successfully retrieved 342 points |
| `DEBUG` | Detalles técnicos (URLs, respuestas) | URL: https://api... |

Para ver todos los logs, configura en `application.properties`:

```properties
# Ver todos los logs de OpenRouteService
logging.level.com.codexateam.platform.iot.infrastructure.external=DEBUG
logging.level.com.codexateam.platform.iot.application.internal=DEBUG
```

---

## 🧪 Prueba de Diagnóstico

Para verificar que el logging funciona, ejecuta:

```bash
# Iniciar la aplicación
./mvnw spring-boot:run

# Luego en otro terminal, hacer una petición
curl "http://localhost:8080/api/v1/simulation/route?startLat=-12.0464&startLng=-77.0428&endLat=-12.119&endLng=-77.029"
```

Revisa la consola para ver los mensajes de diagnóstico.

---

## 🎯 Checklist de Diagnóstico

- [ ] ¿Ves el mensaje `❌ OpenRouteService API Key is NOT CONFIGURED`?
  - **→ Configura tu API key en application.properties**

- [ ] ¿Ves `ERROR 401 UNAUTHORIZED`?
  - **→ Tu API key es inválida, verifica en https://openrouteservice.org/dev/#/home**

- [ ] ¿Ves `ERROR 403 FORBIDDEN`?
  - **→ Cuota diaria excedida, espera hasta mañana**

- [ ] ¿Ves `ERROR 429 TOO MANY REQUESTS`?
  - **→ Demasiadas peticiones, reduce la frecuencia**

- [ ] ¿Ves `NETWORK ERROR`?
  - **→ Sin conexión a internet o firewall bloqueando**

- [ ] ¿Ves `⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA)`?
  - **→ Revisa los logs anteriores para saber por qué falló la API**

- [ ] ¿Ves `✅ Successfully retrieved X coordinate points`?
  - **→ ¡Todo funciona! El vehículo seguirá calles reales**

---

## 📞 Soporte

Si después de revisar todos los logs sigues teniendo problemas:

1. Revisa que tu API key esté activa: https://openrouteservice.org/dev/#/home
2. Verifica los límites de tu plan: 40 req/min, 2000 req/día
3. Prueba tu API key manualmente:

```bash
curl "https://api.openrouteservice.org/v2/directions/driving-car?api_key=TU_API_KEY&start=-77.0428,-12.0464&end=-77.029,-12.119"
```

Si este comando falla, el problema está en tu API key o cuenta.

---

## ✨ Changelog

**2025-12-02**: Mejoras implementadas

- ✅ Logging detallado de errores HTTP con código de estado y body
- ✅ Detección específica de errores 401, 403, 404, 429
- ✅ Advertencias visibles cuando se usa ruta de fallback
- ✅ Instrucciones claras de solución en los logs
- ✅ Emojis para fácil identificación visual
- ✅ Logging mejorado en 3 archivos críticos

