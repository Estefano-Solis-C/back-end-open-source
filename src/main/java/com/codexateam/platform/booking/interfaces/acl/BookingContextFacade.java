package com.codexateam.platform.booking.interfaces.acl;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.queries.GetBookingByVehicleIdAndDateQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByRenterIdQuery;
import com.codexateam.platform.booking.domain.services.BookingQueryService;
import com.codexateam.platform.booking.domain.model.valueobjects.BookingStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class BookingContextFacade {

    /**
     * Facade ACL para exponer operaciones del contexto Booking a otros bounded contexts
     * sin acoplarlos con capas internas (controladores, repositorios).
     */

    private final BookingQueryService bookingQueryService;

    public BookingContextFacade(BookingQueryService bookingQueryService) {
        this.bookingQueryService = bookingQueryService;
    }

    /**
     * Retrieves an active booking ID for a given vehicle at a specific timestamp.
     * Used by the IoT bounded context to track active bookings.
     *
     * @param vehicleId The vehicle ID to search for.
     * @param timestamp The timestamp to check for an active booking.
     * @return An Optional containing the booking ID if found.
     */
    public Optional<Long> getBookingIdByVehicleIdAndDate(Long vehicleId, Date timestamp) {
        var query = new GetBookingByVehicleIdAndDateQuery(vehicleId, timestamp);
        var booking = bookingQueryService.handle(query);
        return booking.map(Booking::getId);
    }

    /**
     * Checks if a renter has completed at least one booking for the specified vehicle.
     * A booking is considered completed if its status is CANCELED, REJECTED, or CONFIRMED with an end date in the past.
     * Used by the Reviews bounded context to verify that a user has rented the vehicle before posting a review.
     *
     * @param renterId The ID of the renter.
     * @param vehicleId The ID of the vehicle.
     * @return true if the renter has a completed booking for the vehicle, false otherwise.
     */
    public boolean hasCompletedBooking(Long renterId, Long vehicleId) {
        var bookings = bookingQueryService.handle(new GetBookingsByRenterIdQuery(renterId));
        Date now = new Date();
        return bookings.stream().anyMatch(b ->
                b.getVehicleId().equals(vehicleId) && (
                        BookingStatus.CANCELED.equalsIgnoreCase(b.getStatus()) ||
                        BookingStatus.REJECTED.equalsIgnoreCase(b.getStatus()) ||
                        (BookingStatus.CONFIRMED.equalsIgnoreCase(b.getStatus()) && b.getEndDate() != null && b.getEndDate().before(now))
                )
        );
    }
}
