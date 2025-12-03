# 🚀 Quick Start - Complete Route API

## ⚡ Inicio Rápido (5 minutos)

### Paso 1: Configurar API Key

Edita `src/main/resources/application.properties`:

```properties
openrouteservice.api.key=TU_API_KEY_AQUI
```

**Obtener API Key gratuita:**
1. Ve a https://openrouteservice.org/
2. Regístrate (gratis)
3. Ve a Dashboard → API Keys
4. Copia la key
5. Pégala en application.properties

---

### Paso 2: Iniciar la Aplicación

```bash
./mvnw spring-boot:run
```

O en Windows:
```bash
.\mvnw.cmd spring-boot:run
```

---

### Paso 3: Probar el Endpoint

#### Opción A: cURL

```bash
curl "http://localhost:8080/api/v1/simulation/route/complete?startLat=-12.046374&startLng=-77.042793&endLat=-12.056189&endLng=-77.029317"
```

#### Opción B: Navegador

Abre: http://localhost:8080/api/v1/simulation/route/complete?startLat=-12.046374&startLng=-77.042793&endLat=-12.056189&endLng=-77.029317

#### Opción C: Swagger UI

1. Abre: http://localhost:8080/swagger-ui.html
2. Busca "Route Simulation"
3. Expande `/route/complete`
4. Click "Try it out"
5. Ingresa coordenadas
6. Click "Execute"

---

## 📊 Respuesta Esperada

```json
{
  "coordinates": [
    [-12.046374, -77.042793],
    [-12.046450, -77.042720],
    [-12.046520, -77.042650],
    ...
    (100+ puntos siguiendo las calles)
    ...
    [-12.056189, -77.029317]
  ],
  "distanceMeters": 2487.5,
  "durationSeconds": 418.0,
  "distanceKm": 2.4875,
  "durationMinutes": 6.967,
  "averageSpeedKmh": 21.43
}
```

---

## 🎯 Uso en Frontend

### JavaScript/Fetch

```javascript
async function getRoute(startLat, startLng, endLat, endLng) {
  const response = await fetch(
    `http://localhost:8080/api/v1/simulation/route/complete?` +
    `startLat=${startLat}&startLng=${startLng}&` +
    `endLat=${endLat}&endLng=${endLng}`
  );
  
  const route = await response.json();
  
  console.log(`📍 ${route.coordinates.length} puntos`);
  console.log(`📏 ${route.distanceKm} km`);
  console.log(`⏱️ ${route.durationMinutes} min`);
  console.log(`🚗 ${route.averageSpeedKmh} km/h`);
  
  return route;
}

// Usar
const route = await getRoute(-12.046374, -77.042793, -12.056189, -77.029317);
```

### Angular Service

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CompleteRoute {
  coordinates: [number, number][];
  distanceMeters: number;
  durationSeconds: number;
  distanceKm: number;
  durationMinutes: number;
  averageSpeedKmh: number;
}

@Injectable({ providedIn: 'root' })
export class RouteService {
  private apiUrl = 'http://localhost:8080/api/v1/simulation';

  constructor(private http: HttpClient) {}

  getCompleteRoute(
    startLat: number, startLng: number,
    endLat: number, endLng: number
  ): Observable<CompleteRoute> {
    return this.http.get<CompleteRoute>(
      `${this.apiUrl}/route/complete`,
      {
        params: {
          startLat: startLat.toString(),
          startLng: startLng.toString(),
          endLat: endLat.toString(),
          endLng: endLng.toString()
        }
      }
    );
  }
}
```

---

## 🗺️ Visualizar en Leaflet

```javascript
import L from 'leaflet';

function drawRoute(route, map) {
  // Convertir coordenadas al formato Leaflet
  const latLngs = route.coordinates.map(c => [c[0], c[1]]);
  
  // Dibujar polyline
  const polyline = L.polyline(latLngs, {
    color: 'blue',
    weight: 4,
    opacity: 0.7
  }).addTo(map);
  
  // Ajustar zoom
  map.fitBounds(polyline.getBounds());
  
  // Marcador de inicio
  L.marker(latLngs[0])
    .bindPopup(`
      <b>Inicio</b><br>
      Distancia: ${route.distanceKm.toFixed(2)} km<br>
      Tiempo: ${route.durationMinutes.toFixed(1)} min<br>
      Velocidad: ${route.averageSpeedKmh.toFixed(1)} km/h
    `)
    .addTo(map);
  
  // Marcador de fin
  L.marker(latLngs[latLngs.length - 1])
    .bindPopup('<b>Destino</b>')
    .addTo(map);
}

// Usar
const route = await getRoute(-12.046374, -77.042793, -12.056189, -77.029317);
drawRoute(route, map);
```

---

## 🚗 Simular Movimiento

```javascript
class VehicleSimulator {
  constructor(route) {
    this.route = route;
    this.currentIndex = 0;
  }
  
  start(speedMultiplier = 1.0) {
    const totalPoints = this.route.coordinates.length;
    const timePerPoint = (this.route.durationSeconds * 1000) / totalPoints;
    const adjustedTime = timePerPoint / speedMultiplier;
    
    this.interval = setInterval(() => {
      if (this.currentIndex >= totalPoints) {
        this.stop();
        return;
      }
      
      const [lat, lng] = this.route.coordinates[this.currentIndex];
      const currentSpeed = this.route.averageSpeedKmh * speedMultiplier;
      
      // Actualizar posición en el mapa
      this.updatePosition(lat, lng, currentSpeed);
      
      this.currentIndex++;
    }, adjustedTime);
  }
  
  stop() {
    if (this.interval) {
      clearInterval(this.interval);
    }
  }
  
  updatePosition(lat, lng, speed) {
    console.log(`📍 Lat: ${lat}, Lng: ${lng}, 🚗 Speed: ${speed.toFixed(1)} km/h`);
    // Actualizar marcador en el mapa
  }
}

// Usar
const route = await getRoute(-12.046374, -77.042793, -12.056189, -77.029317);
const simulator = new VehicleSimulator(route);
simulator.start(1.5); // 1.5x velocidad
```

---

## 🧪 Testing

### Verificar que todo funciona

```bash
# Ejecutar tests
./mvnw test

# O solo los tests del nuevo endpoint
./mvnw test -Dtest=CompleteRouteControllerTest
```

**Resultado esperado:**
```
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
✅ BUILD SUCCESS
```

---

## 📍 Coordenadas de Prueba

### Lima, Perú
```
startLat=-12.046374, startLng=-77.042793
endLat=-12.056189, endLng=-77.029317
```

### Buenos Aires, Argentina
```
startLat=-34.6037, startLng=-58.3816
endLat=-34.6158, endLng=-58.3724
```

### Madrid, España
```
startLat=40.4168, startLng=-3.7038
endLat=40.4234, endLng=-3.6926
```

### Ciudad de México
```
startLat=19.4326, startLng=-99.1332
endLat=19.4420, endLng=-99.1269
```

---

## 📚 Documentación Completa

- **COMPLETE_ROUTE_API_DOCUMENTATION.md** - Guía detallada
- **complete-route-examples.http** - 15 ejemplos de uso
- **IMPLEMENTATION_SUMMARY.md** - Resumen técnico

---

## ❓ Troubleshooting

### Error: "Routing service is not properly configured"

**Solución:** Configura tu API key en `application.properties`

```properties
openrouteservice.api.key=TU_API_KEY
```

### Error: "Unable to retrieve route coordinates"

**Causas posibles:**
1. API key inválida
2. Sin conexión a internet
3. Coordenadas inválidas
4. Límite de requests excedido (OpenRouteService: 2000/día gratis)

**Solución:** Verifica tu API key y conexión

### Las coordenadas siguen apareciendo en línea recta

**Problema:** Estás usando el endpoint antiguo `/route`

**Solución:** Usa el nuevo endpoint `/route/complete`

---

## 🎯 Endpoints Disponibles

| Endpoint | Descripción | Uso |
|----------|-------------|-----|
| `/route` | Solo coordenadas | Visualización simple |
| `/route/complete` | ⭐ Completo | **Simulación realista** |

---

## ✅ Checklist de Implementación

- [x] API key configurada
- [x] Aplicación iniciada
- [x] Endpoint probado
- [x] Frontend integrado
- [x] Visualización en mapa
- [x] Simulación funcionando

---

## 🚀 ¡Todo Listo!

Tu servicio de rutas ahora:
- ✅ Sigue calles reales (no líneas rectas)
- ✅ Incluye distancia total
- ✅ Incluye tiempo estimado
- ✅ Calcula velocidad promedio
- ✅ Está completamente testeado
- ✅ Tiene documentación completa

**¡A simular! 🚗💨**

---

## 📧 Soporte

Para más información:
- Documentación: `COMPLETE_ROUTE_API_DOCUMENTATION.md`
- Ejemplos: `complete-route-examples.http`
- Tests: `CompleteRouteControllerTest.java`

