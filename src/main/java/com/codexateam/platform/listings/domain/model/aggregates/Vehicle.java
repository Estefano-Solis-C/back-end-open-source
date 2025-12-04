package com.codexateam.platform.listings.domain.model.aggregates;

import com.codexateam.platform.listings.domain.model.commands.CreateVehicleCommand;
import com.codexateam.platform.listings.domain.model.commands.UpdateVehicleCommand;
import com.codexateam.platform.listings.domain.model.valueobjects.VehicleStatus;
import com.codexateam.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the Vehicle aggregate root in the Listings bounded context.
 * Based on the 'vehicles' table in db.json.
 */
@NoArgsConstructor
@Getter
@Entity
@Table(name = "vehicles") // Matches 'db.json'
public class Vehicle extends AuditableAbstractAggregateRoot<Vehicle> {

    private static final String DEFAULT_STATUS_AVAILABLE = "available";

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Double pricePerDay;

    /**
     * The rental status of the vehicle (e.g., "available", "rented").
     */
    @Embedded
    private VehicleStatus status;

    /**
     * The image data of the vehicle stored as binary data.
     */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    /**
     * Foreign key to the User (Arrendador) who owns this vehicle.
     */
    @Column(nullable = false)
    private Long ownerId;

    public Vehicle(CreateVehicleCommand command) {
        this.brand = command.brand();
        this.model = command.model();
        this.year = command.year();
        this.pricePerDay = command.pricePerDay();
        this.status = new VehicleStatus(DEFAULT_STATUS_AVAILABLE); // Default status on creation
        this.image = command.image();
        this.ownerId = command.ownerId();
    }
    
    /**
     * Updates the status of the vehicle.
     * @param newStatus The new status (e.g., "rented").
     */
    public void updateStatus(String newStatus) {
        this.status = new VehicleStatus(newStatus);
    }

    /**
     * Updates the vehicle's information with values from the UpdateVehicleCommand.
     * Only updates the image if the command contains valid image data (not null and not empty).
     * @param command The command containing the updated vehicle data.
     */
    public void update(UpdateVehicleCommand command) {
        this.brand = command.brand();
        this.model = command.model();
        this.year = command.year();
        this.pricePerDay = command.pricePerDay();
        // Only update image if new image data is provided
        if (command.image() != null && command.image().length > 0) {
            this.image = command.image();
        }
    }

    /**
     * Returns the vehicle's status as a string.
     * @return The status value or null if status is not set.
     */
    public String getStatus() { return status != null ? status.value() : null; }
}
