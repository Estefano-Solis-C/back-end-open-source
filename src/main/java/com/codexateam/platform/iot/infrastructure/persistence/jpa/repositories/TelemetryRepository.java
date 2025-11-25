package com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository interface for the Telemetry aggregate root.
 */
@Repository
public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
    /**
     * Finds all telemetry data for a specific vehicle.
     * @param vehicleId The ID of the vehicle.
     * @param sort Sorting criteria (e.g., by timestamp).
     */
    List<Telemetry> findByVehicleId(Long vehicleId, Sort sort);

    // Delete all telemetry entries by vehicleId (cascade cleanup when deleting a vehicle)
    void deleteByVehicleId(Long vehicleId);
}
