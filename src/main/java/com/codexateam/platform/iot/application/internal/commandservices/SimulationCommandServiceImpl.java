package com.codexateam.platform.iot.application.internal.commandservices;

import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalListingsService;
import com.codexateam.platform.iot.domain.exceptions.VehicleNotFoundException;
import com.codexateam.platform.iot.domain.services.SimulationCommandService;
import com.codexateam.platform.shared.domain.exceptions.UnauthorizedAccessException;
import org.springframework.stereotype.Service;

/**
 * Implementation of SimulationCommandService.
 * Handles simulation-related commands with proper authorization checks.
 */
@Service
public class SimulationCommandServiceImpl implements SimulationCommandService {

    private final ExternalListingsService externalListingsService;

    public SimulationCommandServiceImpl(ExternalListingsService externalListingsService) {
        this.externalListingsService = externalListingsService;
    }

    /**
     * Validates that the vehicle exists and the user is authorized to simulate it.
     * Business Rule: Only vehicle owners can start simulations.
     */
    private void validateSimulationAuthorization(Long vehicleId, Long userId) {
        // Validate vehicle exists
        if (externalListingsService.fetchVehicleById(vehicleId).isEmpty()) {
            throw new VehicleNotFoundException(vehicleId);
        }

        // Validate user is vehicle owner
        if (!externalListingsService.isVehicleOwner(vehicleId, userId)) {
            throw new UnauthorizedAccessException(
                    "Not authorized to simulate telemetry for vehicle " + vehicleId);
        }
    }

    @Override
    public void startSimulation(Long vehicleId, Long userId) {
        validateSimulationAuthorization(vehicleId, userId);
        // Note: Actual simulation logic is handled by AutomaticTelemetryGeneratorService
        // This service only handles authorization
    }

    @Override
    public void startCustomSimulation(Long vehicleId, Long userId, double startLat, double startLng, double endLat, double endLng) {
        validateSimulationAuthorization(vehicleId, userId);
        // Note: Actual simulation logic is handled by AutomaticTelemetryGeneratorService
        // This service only handles authorization
    }
}

