package com.codexateam.platform.iot.application.internal.outboundservices.acl;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByRenterIdQuery;
import com.codexateam.platform.booking.domain.services.BookingQueryService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Implementation of ExternalBookingService ACL for IoT context.
 * Communicates with the Booking bounded context to validate tracking permissions.
 */
@Service
public class ExternalBookingServiceImpl implements ExternalBookingService {

    private final BookingQueryService bookingQueryService;

    public ExternalBookingServiceImpl(BookingQueryService bookingQueryService) {
        this.bookingQueryService = bookingQueryService;
    }

    @Override
    public boolean hasTrackingPermission(Long userId, Long vehicleId) {
        // Get all bookings for this renter
        var query = new GetBookingsByRenterIdQuery(userId);
        var bookings = bookingQueryService.handle(query);

        // Check if user has an active or confirmed booking for this vehicle
        Date now = new Date();
        return bookings.stream()
                .anyMatch(booking ->
                    booking.getVehicleId().equals(vehicleId) &&
                    isBookingActiveOrConfirmed(booking) &&
                    isWithinBookingPeriod(booking, now)
                );
    }

    private boolean isBookingActiveOrConfirmed(Booking booking) {
        String status = booking.getStatus();
        return "CONFIRMED".equals(status) || "PENDING".equals(status);
    }

    private boolean isWithinBookingPeriod(Booking booking, Date now) {
        return !now.before(booking.getStartDate()) && !now.after(booking.getEndDate());
    }
}

