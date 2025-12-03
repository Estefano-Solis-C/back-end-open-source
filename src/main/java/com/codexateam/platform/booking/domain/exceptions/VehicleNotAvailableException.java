package com.codexateam.platform.booking.domain.exceptions;

import com.codexateam.platform.shared.domain.exceptions.DomainException;

/**
 * Domain exception thrown when a vehicle is not available for booking.
 */
public class VehicleNotAvailableException extends DomainException {
    private final Long vehicleId;
    private final String status;

    public VehicleNotAvailableException(Long vehicleId, String status) {
        super("Vehicle " + vehicleId + " is not available for booking. Current status: " + status);
        this.vehicleId = vehicleId;
        this.status = status;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getStatus() {
        return status;
    }
}


