package com.codexateam.platform.booking.domain.services;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByOwnerIdQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByRenterIdQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingByVehicleIdAndDateQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling Booking queries.
 */
public interface BookingQueryService {
    /**
     * Handles the query to get bookings by renter ID.
     * @param query The query containing the renter ID
     * @return List of bookings made by the renter
     */
    List<Booking> handle(GetBookingsByRenterIdQuery query);
    
    /**
     * Handles the query to get bookings by owner ID.
     * @param query The query containing the owner ID
     * @return List of bookings for vehicles owned by the owner
     */
    List<Booking> handle(GetBookingsByOwnerIdQuery query);

    /**
     * Handles the query to find a booking for a vehicle at a specific timestamp.
     * @param query The query containing vehicle ID and timestamp
     * @return An Optional containing the booking if found, empty otherwise
     */
    Optional<Booking> handle(GetBookingByVehicleIdAndDateQuery query);

    /**
     * Handles the query to find a booking by its ID.
     * @param query The query containing the booking ID
     * @return An Optional containing the booking if found, empty otherwise
     */
    Optional<Booking> handle(GetBookingByIdQuery query);
}
