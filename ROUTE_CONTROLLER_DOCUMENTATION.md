# Route Controller - Documentación

## Descripción
El `RouteController` es un controlador REST que proporciona funcionalidad para obtener coordenadas de rutas entre dos puntos geográficos utilizando la API de OpenRouteService.

## Endpoint

### GET /api/v1/simulation/route

Obtiene las coordenadas de una ruta entre dos puntos geográficos.

#### Parámetros de Query

| Parámetro | Tipo | Requerido | Descripción | Ejemplo |
|-----------|------|-----------|-------------|---------|
| startLat | Double | Sí | Latitud del punto de inicio (-90 a 90) | -12.046374 |
| startLng | Double | Sí | Longitud del punto de inicio (-180 a 180) | -77.042793 |
| endLat | Double | Sí | Latitud del punto de destino (-90 a 90) | -12.056189 |
| endLng | Double | Sí | Longitud del punto de destino (-180 a 180) | -77.029317 |

#### Ejemplo de Solicitud

```bash
GET http://localhost:8080/api/v1/simulation/route?startLat=-12.046374&startLng=-77.042793&endLat=-12.056189&endLng=-77.029317
```

#### Ejemplo de Respuesta (200 OK)

```json
[
  {
    "lat": -12.046374,
    "lng": -77.042793
  },
  {
    "lat": -12.046812,
    "lng": -77.042456
  },
  {
    "lat": -12.047231,
    "lng": -77.042123
  },
  ...
  {
    "lat": -12.056189,
    "lng": -77.029317
  }
]
```

#### Códigos de Respuesta

| Código | Descripción | Escenario |
|--------|-------------|-----------|
| 200 | OK | Ruta obtenida exitosamente |
| 400 | Bad Request | Coordenadas inválidas (fuera de rango) |
| 404 | Not Found | No se pudo obtener la ruta o el servicio no está configurado |
| 500 | Internal Server Error | Error inesperado del servidor o servicio externo no disponible |

#### Ejemplos de Error

**400 Bad Request** - Coordenadas inválidas:
```json
"Invalid coordinates provided"
```

**404 Not Found** - Servicio no configurado:
```json
"Routing service is not properly configured"
```

**404 Not Found** - Ruta no encontrada:
```json
"Unable to retrieve route coordinates for the specified locations"
```

**500 Internal Server Error**:
```json
"An unexpected error occurred while processing the route request"
```

## Configuración

### API Key de OpenRouteService

El servicio requiere una API key de OpenRouteService configurada en `application.properties`:

```properties
openrouteservice.api.key=TU_API_KEY_AQUI
```

Para obtener una API key gratuita:
1. Ve a https://openrouteservice.org/
2. Crea una cuenta
3. Genera una API key en tu dashboard
4. Copia la key en tu archivo de configuración

## Arquitectura

### Componentes

1. **RouteController** (`interfaces/rest/RouteController.java`)
   - Controlador REST que expone el endpoint
   - Valida las coordenadas de entrada
   - Maneja excepciones y devuelve respuestas apropiadas

2. **RouteCoordinateResource** (`interfaces/rest/resources/RouteCoordinateResource.java`)
   - Record Java que representa un punto de coordenadas
   - Formato: `{lat: Double, lng: Double}`

3. **RouteNotFoundException** (`domain/exceptions/RouteNotFoundException.java`)
   - Excepción personalizada para errores de enrutamiento
   - Se lanza cuando no se puede obtener una ruta

4. **OpenRouteServiceApiClient** (`infrastructure/external/OpenRouteServiceApiClient.java`)
   - Cliente existente que realiza llamadas a la API externa
   - Maneja la comunicación HTTP y parseo de respuestas

### Flujo de Ejecución

```
Frontend Request
    ↓
RouteController
    ↓ (validación)
    ↓ (verifica configuración)
OpenRouteServiceApiClient
    ↓ (HTTP GET)
OpenRouteService API
    ↓ (JSON response)
OpenRouteServiceApiClient
    ↓ (parse y mapeo)
RouteController
    ↓ (transformación a Resource)
Frontend Response
```

## Uso con Frontend

### JavaScript/TypeScript

```javascript
async function getRoute(startLat, startLng, endLat, endLng) {
  try {
    const response = await fetch(
      `http://localhost:8080/api/v1/simulation/route?` +
      `startLat=${startLat}&startLng=${startLng}&` +
      `endLat=${endLat}&endLng=${endLng}`
    );
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const coordinates = await response.json();
    
    // Iterar sobre las coordenadas
    coordinates.forEach(coord => {
      console.log(`Lat: ${coord.lat}, Lng: ${coord.lng}`);
      // Agregar marcadores al mapa, etc.
    });
    
    return coordinates;
  } catch (error) {
    console.error('Error fetching route:', error);
    throw error;
  }
}

// Ejemplo de uso
const route = await getRoute(-12.046374, -77.042793, -12.056189, -77.029317);
```

### Angular Service

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RouteCoordinate {
  lat: number;
  lng: number;
}

@Injectable({
  providedIn: 'root'
})
export class RouteService {
  private apiUrl = 'http://localhost:8080/api/v1/simulation';

  constructor(private http: HttpClient) {}

  getRoute(
    startLat: number,
    startLng: number,
    endLat: number,
    endLng: number
  ): Observable<RouteCoordinate[]> {
    const params = {
      startLat: startLat.toString(),
      startLng: startLng.toString(),
      endLat: endLat.toString(),
      endLng: endLng.toString()
    };
    
    return this.http.get<RouteCoordinate[]>(
      `${this.apiUrl}/route`,
      { params }
    );
  }
}
```

### React Hook

```typescript
import { useState, useEffect } from 'react';

interface RouteCoordinate {
  lat: number;
  lng: number;
}

interface RouteParams {
  startLat: number;
  startLng: number;
  endLat: number;
  endLng: number;
}

export function useRoute(params: RouteParams) {
  const [coordinates, setCoordinates] = useState<RouteCoordinate[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchRoute = async () => {
      setLoading(true);
      setError(null);
      
      try {
        const queryParams = new URLSearchParams({
          startLat: params.startLat.toString(),
          startLng: params.startLng.toString(),
          endLat: params.endLat.toString(),
          endLng: params.endLng.toString(),
        });
        
        const response = await fetch(
          `http://localhost:8080/api/v1/simulation/route?${queryParams}`
        );
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        setCoordinates(data);
      } catch (err) {
        setError(err as Error);
      } finally {
        setLoading(false);
      }
    };

    fetchRoute();
  }, [params.startLat, params.startLng, params.endLat, params.endLng]);

  return { coordinates, loading, error };
}
```

## Pruebas

### Prueba con cURL

```bash
curl -X GET "http://localhost:8080/api/v1/simulation/route?startLat=-12.046374&startLng=-77.042793&endLat=-12.056189&endLng=-77.029317"
```

### Prueba con Postman

1. Crear nueva request GET
2. URL: `http://localhost:8080/api/v1/simulation/route`
3. Agregar Query Params:
   - `startLat`: `-12.046374`
   - `startLng`: `-77.042793`
   - `endLat`: `-12.056189`
   - `endLng`: `-77.029317`
4. Send

### Prueba con Swagger UI

1. Inicia la aplicación
2. Navega a: `http://localhost:8080/swagger-ui.html`
3. Busca la sección "Route Simulation"
4. Expande el endpoint GET `/api/v1/simulation/route`
5. Click en "Try it out"
6. Ingresa los parámetros
7. Click en "Execute"

## Manejo de Errores

El controlador implementa tres manejadores de excepciones:

1. **IllegalArgumentException** → 400 Bad Request
   - Se lanza cuando las coordenadas están fuera de rango
   
2. **RouteNotFoundException** → 404 Not Found
   - Se lanza cuando el servicio no está configurado
   - Se lanza cuando no se pueden obtener coordenadas
   
3. **Exception** → 500 Internal Server Error
   - Captura cualquier error inesperado

## Consideraciones de Seguridad

1. **Validación de Entrada**: Todas las coordenadas se validan antes de procesar
2. **Rate Limiting**: Considera implementar rate limiting para evitar abuso
3. **API Key**: Nunca expongas tu API key en el frontend
4. **CORS**: Configura CORS apropiadamente para producción

## Mejoras Futuras

- [ ] Implementar caché de rutas frecuentes
- [ ] Agregar parámetros opcionales (modo de transporte, evitar autopistas, etc.)
- [ ] Implementar rate limiting
- [ ] Agregar métricas y monitoreo
- [ ] Soporte para waypoints intermedios
- [ ] Estimación de tiempo de viaje
- [ ] Endpoints adicionales para geocodificación inversa

