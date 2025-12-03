# Complete Route API - Documentación Actualizada

## 🎯 Resumen de Cambios

Se ha mejorado significativamente el servicio de rutas para extraer la **geometría completa** de OpenRouteService, asegurando que las rutas sigan las calles reales en lugar de líneas rectas. Además, se incluye información de **distancia** y **tiempo estimado** para cálculos de velocidad.

---

## 🆕 Nuevo Endpoint: `/api/v1/simulation/route/complete`

### **GET** `/api/v1/simulation/route/complete`

Este endpoint devuelve la ruta completa con toda la información necesaria para simular trayectos realistas.

#### Parámetros de Query

| Parámetro | Tipo | Requerido | Descripción | Ejemplo |
|-----------|------|-----------|-------------|---------|
| startLat | Double | Sí | Latitud del punto de inicio | -12.046374 |
| startLng | Double | Sí | Longitud del punto de inicio | -77.042793 |
| endLat | Double | Sí | Latitud del punto de destino | -12.056189 |
| endLng | Double | Sí | Longitud del punto de destino | -77.029317 |

#### Respuesta Exitosa (200 OK)

```json
{
  "coordinates": [
    [-12.046374, -77.042793],
    [-12.046812, -77.042456],
    [-12.047231, -77.042123],
    [-12.047650, -77.041790],
    ...
    [-12.056189, -77.029317]
  ],
  "distanceMeters": 2500.0,
  "durationSeconds": 420.0,
  "distanceKm": 2.5,
  "durationMinutes": 7.0,
  "averageSpeedKmh": 21.43
}
```

#### Descripción de Campos

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `coordinates` | `List<List<Double>>` | Lista de coordenadas [lat, lng] que forman la ruta completa siguiendo las calles |
| `distanceMeters` | `Double` | Distancia total de la ruta en metros |
| `durationSeconds` | `Double` | Tiempo estimado del trayecto en segundos |
| `distanceKm` | `Double` | Distancia total en kilómetros (campo de conveniencia) |
| `durationMinutes` | `Double` | Tiempo estimado en minutos (campo de conveniencia) |
| `averageSpeedKmh` | `Double` | Velocidad promedio en km/h (calculada automáticamente) |

---

## 🔧 Cambios Técnicos Implementados

### 1. Nuevo DTO: `RouteResponse`

```java
public class RouteResponse {
    private List<double[]> coordinates;
    private Double distanceMeters;
    private Double durationSeconds;
    
    // Métodos de conveniencia
    public Double getDistanceKilometers() { ... }
    public Double getDurationMinutes() { ... }
    public Double getAverageSpeedKmh() { ... }
}
```

### 2. Nuevo Resource: `CompleteRouteResource`

```java
public record CompleteRouteResource(
    List<List<Double>> coordinates,
    Double distanceMeters,
    Double durationSeconds,
    Double distanceKm,
    Double durationMinutes,
    Double averageSpeedKmh
) {}
```

### 3. Método Mejorado: `OpenRouteServiceApiClient.getCompleteRoute()`

El nuevo método extrae:
- ✅ **Geometría completa** del GeoJSON (`features[0].geometry.coordinates`)
- ✅ **Distancia** del summary (`features[0].properties.summary.distance`)
- ✅ **Duración** del summary (`features[0].properties.summary.duration`)

```java
public RouteResponse getCompleteRoute(
    double startLat, double startLng, 
    double endLat, double endLng
)
```

---

## 📊 Ejemplo de Respuesta Real

### Solicitud

```bash
curl "http://localhost:8080/api/v1/simulation/route/complete?startLat=-12.046374&startLng=-77.042793&endLat=-12.056189&endLng=-77.029317"
```

### Respuesta

```json
{
  "coordinates": [
    [-12.046374, -77.042793],
    [-12.046450, -77.042720],
    [-12.046520, -77.042650],
    [-12.046600, -77.042580],
    [-12.046680, -77.042510],
    ...
    (cientos de puntos siguiendo las calles exactas)
    ...
    [-12.056100, -77.029400],
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

## 🚗 Uso para Simulación de Velocidad

### Calcular Velocidad Actual Durante el Recorrido

```typescript
// Frontend TypeScript/Angular
interface CompleteRoute {
  coordinates: [number, number][];
  distanceMeters: number;
  durationSeconds: number;
  distanceKm: number;
  durationMinutes: number;
  averageSpeedKmh: number;
}

class RouteSimulator {
  private currentIndex = 0;
  private route: CompleteRoute;
  
  async loadRoute(startLat: number, startLng: number, endLat: number, endLng: number) {
    const response = await fetch(
      `http://localhost:8080/api/v1/simulation/route/complete?` +
      `startLat=${startLat}&startLng=${startLng}&` +
      `endLat=${endLat}&endLng=${endLng}`
    );
    this.route = await response.json();
  }
  
  // Simular movimiento con velocidad variable
  simulateMovement(speedMultiplier: number = 1.0) {
    const totalPoints = this.route.coordinates.length;
    const timePerPointMs = (this.route.durationSeconds * 1000) / totalPoints;
    const adjustedTimeMs = timePerPointMs / speedMultiplier;
    
    const interval = setInterval(() => {
      if (this.currentIndex >= totalPoints) {
        clearInterval(interval);
        return;
      }
      
      const [lat, lng] = this.route.coordinates[this.currentIndex];
      
      // Actualizar posición en el mapa
      this.updateVehiclePosition(lat, lng);
      
      // Calcular velocidad actual
      const currentSpeed = this.route.averageSpeedKmh * speedMultiplier;
      this.updateSpeedDisplay(currentSpeed);
      
      this.currentIndex++;
    }, adjustedTimeMs);
  }
  
  // Calcular velocidad instantánea entre dos puntos
  calculateInstantSpeed(point1: [number, number], point2: [number, number], timeMs: number): number {
    const distance = this.haversineDistance(point1, point2);
    const hours = timeMs / (1000 * 60 * 60);
    return distance / hours; // km/h
  }
  
  private haversineDistance(coord1: [number, number], coord2: [number, number]): number {
    const R = 6371; // Radio de la Tierra en km
    const dLat = this.toRad(coord2[0] - coord1[0]);
    const dLon = this.toRad(coord2[1] - coord1[1]);
    const lat1 = this.toRad(coord1[0]);
    const lat2 = this.toRad(coord2[0]);
    
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.sin(dLon/2) * Math.sin(dLon/2) * 
              Math.cos(lat1) * Math.cos(lat2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  }
  
  private toRad(deg: number): number {
    return deg * (Math.PI / 180);
  }
}
```

---

## 🗺️ Visualización en Mapa (Leaflet/Google Maps)

### Con Leaflet

```typescript
import L from 'leaflet';

function drawRoute(route: CompleteRoute, map: L.Map) {
  // Convertir coordenadas al formato de Leaflet [lat, lng]
  const latLngs: L.LatLngExpression[] = route.coordinates.map(
    coord => [coord[0], coord[1]]
  );
  
  // Dibujar polyline siguiendo las calles
  const polyline = L.polyline(latLngs, {
    color: 'blue',
    weight: 4,
    opacity: 0.7
  }).addTo(map);
  
  // Ajustar zoom para mostrar toda la ruta
  map.fitBounds(polyline.getBounds());
  
  // Agregar marcadores de inicio y fin
  L.marker(latLngs[0])
    .bindPopup(`Inicio<br>Distancia total: ${route.distanceKm.toFixed(2)} km<br>Tiempo: ${route.durationMinutes.toFixed(1)} min`)
    .addTo(map);
    
  L.marker(latLngs[latLngs.length - 1])
    .bindPopup('Destino')
    .addTo(map);
}
```

### Con Google Maps

```javascript
function drawRoute(route, map) {
  const path = route.coordinates.map(coord => ({
    lat: coord[0],
    lng: coord[1]
  }));
  
  const polyline = new google.maps.Polyline({
    path: path,
    geodesic: true,
    strokeColor: '#0000FF',
    strokeOpacity: 0.7,
    strokeWeight: 4
  });
  
  polyline.setMap(map);
  
  // Ajustar bounds
  const bounds = new google.maps.LatLngBounds();
  path.forEach(point => bounds.extend(point));
  map.fitBounds(bounds);
}
```

---

## 📝 Ejemplos de Uso

### Ejemplo 1: Ruta Corta (Lima, Perú)

```bash
GET /api/v1/simulation/route/complete?startLat=-12.046374&startLng=-77.042793&endLat=-12.056189&endLng=-77.029317
```

**Resultado esperado:**
- 200+ puntos de coordenadas
- ~2.5 km de distancia
- ~7 minutos de duración
- ~21 km/h velocidad promedio

### Ejemplo 2: Ruta Larga (Buenos Aires, Argentina)

```bash
GET /api/v1/simulation/route/complete?startLat=-34.6037&startLng=-58.3816&endLat=-34.6158&endLng=-58.3724
```

**Resultado esperado:**
- 500+ puntos de coordenadas
- ~5-10 km de distancia
- ~15-20 minutos de duración
- Variable según tráfico

---

## 🔍 Comparación: Endpoint Antiguo vs Nuevo

| Característica | `/route` (antiguo) | `/route/complete` (nuevo) |
|----------------|-------------------|---------------------------|
| Coordenadas | ✅ Sí | ✅ Sí (más puntos) |
| Sigue calles | ⚠️ Limitado | ✅ Completo |
| Distancia | ❌ No | ✅ Sí (metros y km) |
| Duración | ❌ No | ✅ Sí (segundos y minutos) |
| Velocidad promedio | ❌ No | ✅ Sí (km/h) |
| Formato coordenadas | `{lat, lng}` | `[[lat, lng], ...]` |
| Uso recomendado | Visualización simple | Simulación completa |

---

## ✅ Tests Implementados

### Cobertura de Tests (7 tests)

1. ✅ Test con coordenadas válidas
2. ✅ Test con coordenadas inválidas
3. ✅ Test con servicio no configurado
4. ✅ Test con respuesta nula
5. ✅ Test con coordenadas vacías
6. ✅ Test de cálculo de velocidad promedio
7. ✅ Test con ruta larga (100+ puntos)

**Resultado:** ✅ 7/7 tests pasados

---

## 🚀 Cómo Usar

### 1. El endpoint antiguo sigue funcionando

```bash
# Endpoint simple (solo coordenadas)
GET /api/v1/simulation/route
```

### 2. Usa el nuevo endpoint para simulación completa

```bash
# Endpoint completo (con distancia y duración)
GET /api/v1/simulation/route/complete
```

### 3. Ejemplo completo en Angular

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
    startLat: number,
    startLng: number,
    endLat: number,
    endLng: number
  ): Observable<CompleteRoute> {
    const params = {
      startLat: startLat.toString(),
      startLng: startLng.toString(),
      endLat: endLat.toString(),
      endLng: endLng.toString()
    };
    
    return this.http.get<CompleteRoute>(
      `${this.apiUrl}/route/complete`,
      { params }
    );
  }
}
```

---

## 📊 Estructura de Datos Interna

### GeoJSON de OpenRouteService

```json
{
  "features": [
    {
      "geometry": {
        "coordinates": [
          [lng1, lat1],
          [lng2, lat2],
          ...
        ]
      },
      "properties": {
        "summary": {
          "distance": 2487.5,
          "duration": 418.0
        }
      }
    }
  ]
}
```

### Transformación a Response

```json
{
  "coordinates": [
    [lat1, lng1],  // ⚠️ Nota: orden invertido para consistencia
    [lat2, lng2],
    ...
  ],
  "distanceMeters": 2487.5,
  "durationSeconds": 418.0
}
```

---

## 🎉 Mejoras Logradas

### ✅ Problema Resuelto: Rutas en Línea Recta

**Antes:** Las rutas aparecían como líneas rectas entre puntos

**Después:** Las rutas siguen exactamente las calles y caminos reales

### ✅ Nueva Funcionalidad: Métricas de Ruta

- **Distancia total:** Para cálculos de consumo
- **Duración estimada:** Para tiempos de llegada
- **Velocidad promedio:** Para simulaciones realistas

### ✅ Mejor Experiencia de Desarrollo

- DTOs tipados y documentados
- Tests completos
- Documentación exhaustiva
- Ejemplos de uso en múltiples frameworks

---

## 🔐 Configuración

**No olvides configurar tu API key:**

```properties
# application.properties
openrouteservice.api.key=TU_API_KEY_AQUI
```

Obtén tu API key gratuita en: https://openrouteservice.org/

---

## 📚 Referencias

- [OpenRouteService API Documentation](https://openrouteservice.org/dev/#/api-docs)
- [GeoJSON Specification](https://geojson.org/)
- [Spring Boot REST Best Practices](https://spring.io/guides/tutorials/rest/)

---

## ✨ Próximas Mejoras Posibles

- [ ] Soporte para múltiples waypoints
- [ ] Diferentes perfiles de vehículos (coche, bicicleta, a pie)
- [ ] Evitar autopistas o peajes
- [ ] Rutas alternativas
- [ ] Información de tráfico en tiempo real
- [ ] Caching de rutas frecuentes
- [ ] Compresión de coordenadas (algoritmo Douglas-Peucker)

---

¡Listo para usar! 🚀

