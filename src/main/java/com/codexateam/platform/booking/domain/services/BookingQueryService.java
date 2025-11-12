package com.codexateam.platform.booking.domain.services;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByOwnerIdQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByRenterIdQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingByVehicleIdAndDateQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling Booking queries.
 */
public interface BookingQueryService {
    /**
     * Handles the query to get bookings by renter ID.
     */
    List<Booking> handle(GetBookingsByRenterIdQuery query);
    
    /**
     * Handles the query to get bookings by owner ID.
     */
    List<Booking> handle(GetBookingsByOwnerIdQuery query);

    /**
     * Handles the query to find a booking for a vehicle at a specific timestamp.
     */
    Optional<Booking> handle(GetBookingByVehicleIdAndDateQuery query);
}
