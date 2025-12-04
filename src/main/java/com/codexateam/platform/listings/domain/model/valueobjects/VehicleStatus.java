package com.codexateam.platform.listings.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.util.Set;

/**
 * Represents the status of a vehicle.
 * <p>
 * This class is used to define and validate the various states a vehicle can be in,
 * such as available, rented, or under maintenance. The status is normalized to
 * ensure consistency in its representation.
 * </p>
 */
@Embeddable
public record VehicleStatus(String value) {
    public static final String AVAILABLE = "available";
    public static final String RENTED = "rented";
    public static final String MAINTENANCE = "maintenance";
    private static final Set<String> ALLOWED = Set.of(AVAILABLE, RENTED, MAINTENANCE);

    /**
     * Constructs a VehicleStatus with validation and normalization.
     *
     * @param value the status of the vehicle
     * @throws IllegalArgumentException if the status is null, blank, or invalid
     */
    public VehicleStatus {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Vehicle status cannot be blank");
        var normalized = value.toLowerCase();
        if (!ALLOWED.contains(normalized)) throw new IllegalArgumentException("Invalid vehicle status: " + value);
        value = normalized;
    }

    /**
     * Returns the string representation of the vehicle status.
     * @return
     */
    @Override public String toString() { return value; }
}
