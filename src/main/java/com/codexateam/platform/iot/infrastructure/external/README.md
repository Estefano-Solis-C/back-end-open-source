# OpenRouteService API Client - Documentation

## Overview

The `OpenRouteServiceApiClient` provides integration with the [OpenRouteService API](https://openrouteservice.org/) to retrieve realistic vehicle routing data for trip simulations in the IoT context.

## Features

✅ **Route Coordinate Retrieval**: Get actual road coordinates between two geographic points  
✅ **Graceful Error Handling**: Returns empty list on failures, logs detailed error messages  
✅ **Configurable API Key**: Uses Spring properties for easy configuration  
✅ **Logging**: SLF4J logging for debugging and monitoring  
✅ **REST Integration**: Uses Spring's RestTemplate for HTTP communication

## Setup

### 1. Get Your API Key

1. Visit [OpenRouteService Sign Up](https://openrouteservice.org/dev/#/signup)
2. Create a free account
3. Generate an API key from your dashboard
4. Copy the API key

### 2. Configure Application Properties

Add the API key to your `application.properties`:

```properties
# OpenRouteService API Configuration
openrouteservice.api.key=YOUR_API_KEY_HERE
```

Or set it as an environment variable:

```bash
export OPENROUTESERVICE_API_KEY=YOUR_API_KEY_HERE
```

## Usage

### Basic Example

```java
@Service
public class TripSimulationService {
    
    private final OpenRouteServiceApiClient routeClient;
    
    public TripSimulationService(OpenRouteServiceApiClient routeClient) {
        this.routeClient = routeClient;
    }
    
    public void simulateTrip() {
        // Example: Route from Lima, Peru to Callao, Peru
        double startLat = -12.046374;  // Lima
        double startLng = -77.042793;
        double endLat = -12.050000;    // Callao
        double endLng = -77.112500;
        
        List<double[]> coordinates = routeClient.getRouteCoordinates(
            startLat, startLng, endLat, endLng
        );
        
        if (!coordinates.isEmpty()) {
            System.out.println("Route contains " + coordinates.size() + " points");
            
            // Simulate telemetry for each coordinate
            for (double[] coord : coordinates) {
                double lat = coord[0];
                double lng = coord[1];
                // Send telemetry data with this coordinate
                recordTelemetry(vehicleId, lat, lng);
            }
        }
    }
}
```

### Integration with Telemetry

```java
@Service
public class RealisticTelemetrySimulator {
    
    private final OpenRouteServiceApiClient routeClient;
    private final TelemetryCommandService telemetryCommandService;
    
    public RealisticTelemetrySimulator(
        OpenRouteServiceApiClient routeClient,
        TelemetryCommandService telemetryCommandService
    ) {
        this.routeClient = routeClient;
        this.telemetryCommandService = telemetryCommandService;
    }
    
    public void simulateRealTrip(Long vehicleId, Location start, Location end) {
        List<double[]> route = routeClient.getRouteCoordinates(
            start.getLatitude(), start.getLongitude(),
            end.getLatitude(), end.getLongitude()
        );
        
        double speed = 60.0; // km/h
        double fuelLevel = 100.0;
        
        for (double[] coordinate : route) {
            RecordTelemetryCommand command = new RecordTelemetryCommand(
                vehicleId,
                coordinate[0],  // latitude
                coordinate[1],  // longitude
                speed,
                fuelLevel
            );
            
            telemetryCommandService.handle(command);
            
            // Simulate fuel consumption
            fuelLevel -= 0.5;
            
            // Add delay between telemetry points (optional)
            Thread.sleep(1000);
        }
    }
}
```

## API Response Format

The OpenRouteService API returns coordinates in **[longitude, latitude]** format, but this client automatically converts them to **[latitude, longitude]** for consistency with common mapping conventions.

### Original API Response Structure:
```json
{
  "routes": [
    {
      "geometry": {
        "coordinates": [
          [-77.042793, -12.046374],
          [-77.043000, -12.046500],
          ...
        ]
      }
    }
  ]
}
```

### Returned by Client:
```java
List<double[]> coordinates = [
    [lat, lng],  // [-12.046374, -77.042793]
    [lat, lng],  // [-12.046500, -77.043000]
    ...
]
```

## Error Handling

The client handles errors gracefully and returns an empty list in the following cases:

- ❌ **API Key not configured**: Logs error and returns empty list
- ❌ **HTTP errors** (4xx, 5xx): Logs status code and message
- ❌ **Network errors**: Logs connection issues
- ❌ **Invalid response format**: Logs parsing errors
- ❌ **No routes found**: Logs warning

### Example Error Log:
```
ERROR - OpenRouteService API key is not configured. Please set 'openrouteservice.api.key' in application.properties
ERROR - HTTP error calling OpenRouteService API: 401 UNAUTHORIZED - Invalid API key
ERROR - Network error calling OpenRouteService API: Connection timeout
WARN  - No route data found in OpenRouteService response
```

## Configuration Check

You can verify if the client is properly configured:

```java
if (routeClient.isConfigured()) {
    System.out.println("OpenRouteService is ready to use");
} else {
    System.out.println("Please configure your API key");
}
```

## Limitations

- **Free Tier Limits**: 
  - 2,000 requests per day
  - 40 requests per minute
  - Up to 350 km per route
  
- **Coordinate Format**: Always provide coordinates in decimal degrees
- **Route Type**: Currently configured for `driving-car` profile only

## Testing

### Unit Test Example:
```java
@Test
void testGetRouteCoordinates() {
    // Setup
    OpenRouteServiceApiClient client = new OpenRouteServiceApiClient();
    
    // Test with valid coordinates
    List<double[]> route = client.getRouteCoordinates(
        -12.046374, -77.042793,  // Lima
        -12.050000, -77.112500   // Callao
    );
    
    // Assertions
    assertNotNull(route);
    assertTrue(route.size() > 0);
    
    // Verify coordinate format
    double[] firstPoint = route.get(0);
    assertEquals(2, firstPoint.length);
    assertTrue(firstPoint[0] >= -90 && firstPoint[0] <= 90);  // Valid latitude
    assertTrue(firstPoint[1] >= -180 && firstPoint[1] <= 180); // Valid longitude
}
```

## Architecture

```
IoT Context
  └── Infrastructure Layer
       └── external/
            ├── OpenRouteServiceApiClient.java      (Main API client)
            └── dto/
                 └── OpenRouteServiceResponse.java  (Response DTOs)
```

## Dependencies

- ✅ `spring-boot-starter-web` (RestTemplate)
- ✅ `jackson-databind` (JSON parsing)
- ✅ `slf4j-api` (Logging)

All dependencies are already included in Spring Boot Starter Web.

## Troubleshooting

### Issue: "API key is not configured"
**Solution**: Add the API key to `application.properties` or set the environment variable `OPENROUTESERVICE_API_KEY`

### Issue: "401 Unauthorized"
**Solution**: Verify your API key is valid and not expired

### Issue: "Empty route returned"
**Solution**: 
- Check if coordinates are valid (lat: -90 to 90, lng: -180 to 180)
- Verify the route distance is within the API limits (< 350 km for free tier)
- Ensure both points are accessible by car

### Issue: "Connection timeout"
**Solution**: Check your network connection and firewall settings

## References

- [OpenRouteService Documentation](https://openrouteservice.org/dev/#/api-docs)
- [Directions API](https://openrouteservice.org/dev/#/api-docs/v2/directions)
- [Sign Up for Free API Key](https://openrouteservice.org/dev/#/signup)

---

**Created**: 2025-11-25  
**Version**: 1.0.0  
**Context**: IoT - Vehicle Telemetry Simulation

