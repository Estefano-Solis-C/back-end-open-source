package com.codexateam.platform.iot.domain.model.aggregates;

import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the Telemetry aggregate root in the IoT bounded context.
 * Based on the 'telemetry.model.ts' from the frontend.
 * The 'createdAt' field from AuditableAbstractAggregateRoot serves as the 'timestamp'.
 */
@NoArgsConstructor
@Getter
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
}
