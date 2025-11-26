package com.codexateam.platform.iot.domain.model.aggregates;

import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the Telemetry aggregate root in the IoT bounded context.
 * Based on the 'telemetry.model.ts' from the frontend.
 * The 'createdAt' field from AuditableAbstractAggregateRoot serves as the 'timestamp'.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "telemetry") // Pluralized by naming strategy
public class Telemetry extends AuditableAbstractAggregateRoot<Telemetry> {

    @Column(nullable = false)
    private Long vehicleId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double speed;
    
    private Double fuelLevel;

    /**
     * Note: The 'timestamp' field from the frontend model
     * is represented by the 'createdAt' field from the AuditableAbstractAggregateRoot.
     */

    public Telemetry(RecordTelemetryCommand command) {
        this.vehicleId = command.vehicleId();
        this.latitude = command.latitude();
        this.longitude = command.longitude();
        this.speed = command.speed();
        this.fuelLevel = command.fuelLevel();
    }

    /**
     * Updates the telemetry data with new position, speed, and fuel level.
     * This method is used for UPSERT operations where we maintain a single record per vehicle.
     * Updates the timestamp to reflect the latest reading.
     *
     * @param latitude The new latitude coordinate
     * @param longitude The new longitude coordinate
     * @param speed The current speed in km/h
     * @param fuelLevel The current fuel level percentage (0-100)
     * @param timestamp The timestamp for this telemetry reading
     */
    public void updateTelemetryData(Double latitude, Double longitude, Double speed, Double fuelLevel, java.util.Date timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.fuelLevel = fuelLevel;
        this.overwriteCreatedAt(timestamp);
    }
}
