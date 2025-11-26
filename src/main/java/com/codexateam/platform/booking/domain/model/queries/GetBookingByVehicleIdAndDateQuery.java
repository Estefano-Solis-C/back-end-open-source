package com.codexateam.platform.booking.domain.model.queries;

import java.util.Date;

/**
 * Query to find a booking for a specific vehicle that includes a given timestamp.
 * Used to check if a vehicle has an active booking at a specific point in time.
 * @param vehicleId The unique identifier of the vehicle
 * @param timestamp The specific date and time to check
 */
public record GetBookingByVehicleIdAndDateQuery(Long vehicleId, Date timestamp) {
}

