package com.codexateam.platform.iot.application.internal.outboundservices.acl;

import com.codexateam.platform.booking.domain.model.queries.GetBookingsByRenterIdQuery;
import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.services.BookingQueryService;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import com.codexateam.platform.booking.interfaces.acl.BookingContextFacade;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * Implementation of ExternalBookingService ACL for IoT context.
 * Communicates with the Booking bounded context to validate tracking permissions.
 */
@Service
public class ExternalBookingServiceImpl implements ExternalBookingService {

    private static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";
    private static final String BOOKING_STATUS_PENDING = "PENDING";

    private final BookingQueryService bookingQueryService;
    private final BookingContextFacade bookingContextFacade;
    private final BookingRepository bookingRepository;

    public ExternalBookingServiceImpl(BookingQueryService bookingQueryService,
                                     BookingContextFacade bookingContextFacade,
                                     BookingRepository bookingRepository) {
        this.bookingQueryService = bookingQueryService;
        this.bookingContextFacade = bookingContextFacade;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public boolean hasTrackingPermission(Long userId, Long vehicleId) {
        var query = new GetBookingsByRenterIdQuery(userId);
        var bookings = bookingQueryService.handle(query);

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
        return BOOKING_STATUS_CONFIRMED.equals(status) || BOOKING_STATUS_PENDING.equals(status);
    }

    private boolean isWithinBookingPeriod(Booking booking, Date now) {
        return !now.before(booking.getStartDate()) && !now.after(booking.getEndDate());
    }

    @Override
    public Optional<Long> getBookingIdByVehicleIdAndDate(Long vehicleId, Date timestamp) {
        return bookingContextFacade.getBookingIdByVehicleIdAndDate(vehicleId, timestamp);
    }

    @Override
    public Optional<Long> getActiveRenterIdByVehicleId(Long vehicleId) {
        Date now = new Date();
        return bookingRepository.findByBookingStatus_StatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                BOOKING_STATUS_CONFIRMED,
                now,
                now).stream()
                .filter(booking -> booking.getVehicleId().equals(vehicleId))
                .findFirst()
                .map(Booking::getRenterId);
    }
}
