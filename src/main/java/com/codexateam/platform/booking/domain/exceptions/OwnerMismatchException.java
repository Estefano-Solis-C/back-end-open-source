package com.codexateam.platform.booking.domain.exceptions;

/**
 * Exception thrown when there's a mismatch between the vehicle's owner and the booking's owner.
 * This can happen if the vehicle data is inconsistent with the booking request.
 */
public class OwnerMismatchException extends RuntimeException {

    private final Long vehicleId;
    private final Long expectedOwnerId;
    private final Long actualOwnerId;

    /**
     * Constructs a new OwnerMismatchException.
     *
     * @param vehicleId The ID of the vehicle
     * @param expectedOwnerId The owner ID provided in the booking request
     * @param actualOwnerId The actual owner ID of the vehicle
     */
    public OwnerMismatchException(Long vehicleId, Long expectedOwnerId, Long actualOwnerId) {
        super(String.format("Owner ID mismatch for vehicle %d: expected %d but vehicle belongs to %d",
                           vehicleId, expectedOwnerId, actualOwnerId));
        this.vehicleId = vehicleId;
        this.expectedOwnerId = expectedOwnerId;
        this.actualOwnerId = actualOwnerId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getExpectedOwnerId() {
        return expectedOwnerId;
    }

    public Long getActualOwnerId() {
        return actualOwnerId;
    }
}

