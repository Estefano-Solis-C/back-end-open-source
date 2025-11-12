package com.codexateam.platform.booking.interfaces.acl;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.queries.GetBookingByVehicleIdAndDateQuery;
import com.codexateam.platform.booking.domain.services.BookingQueryService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class BookingContextFacade {

    private final BookingQueryService bookingQueryService;

    public BookingContextFacade(BookingQueryService bookingQueryService) {
        this.bookingQueryService = bookingQueryService;
    }

    /**
     * Usado por 'iot' para encontrar un booking activo y devolver su ID.
     */
    public Optional<Long> getBookingIdByVehicleIdAndDate(Long vehicleId, Date timestamp) {
        var query = new GetBookingByVehicleIdAndDateQuery(vehicleId, timestamp);
        var booking = bookingQueryService.handle(query); // Asume que este handle devuelve Optional<Booking>
        return booking.map(Booking::getId);
    }
}

