package com.codexateam.platform.iot.application.internal.outboundservices.acl;

import java.util.Date;
import java.util.Optional;

/**
 * Anti-Corruption Layer (ACL) facade for accessing Booking bounded context from IoT.
 * Provides methods to validate tracking permissions.
 */
public interface ExternalBookingService {

    /**
     * Validates if a user has permission to view tracking data for a vehicle.
     * A user has permission if:
     * - They are the owner of the vehicle (ARRENDADOR), OR
     * - They have an active booking for the vehicle (ARRENDATARIO)
     *
     * @param userId The ID of the user requesting tracking data.
     * @param vehicleId The ID of the vehicle.
     * @return true if the user has permission, false otherwise.
     */
    boolean hasTrackingPermission(Long userId, Long vehicleId);

    Optional<Long> getBookingIdByVehicleIdAndDate(Long vehicleId, Date timestamp);
}
