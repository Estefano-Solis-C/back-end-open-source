package com.codexateam.platform.iot.application.internal.outboundservices;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for automatically generating telemetry data for active bookings.
 * Provides route planning and vehicle monitoring functionality.
 */
@Service
@EnableScheduling
public class AutomaticTelemetryGeneratorService {

    private final Map<Long, Queue<double[]>> vehicleRoutes = new ConcurrentHashMap<>();

    private final Map<Long, Date> lastHeartbeatMap = new ConcurrentHashMap<>();

    public AutomaticTelemetryGeneratorService() {
    }

    /**
     * Notifies that the frontend is actively monitoring a vehicle.
     * Maintained for controller compatibility.
     *
     * @param vehicleId The ID of the vehicle being monitored
     */
    public void notifyActiveMonitoring(Long vehicleId) {
        if (vehicleId == null) return;
        lastHeartbeatMap.put(vehicleId, new Date());
    }

    /**
     * Gets the current planned route for a vehicle.
     *
     * @param vehicleId The ID of the vehicle
     * @return List of coordinate points forming the route, or empty list if none
     */
    public List<double[]> getPlannedRoute(Long vehicleId) {
        Queue<double[]> queue = vehicleRoutes.get(vehicleId);
        if (queue == null || queue.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(queue);
    }
}
