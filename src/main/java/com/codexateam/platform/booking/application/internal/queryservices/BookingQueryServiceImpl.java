package com.codexateam.platform.booking.application.internal.queryservices;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByOwnerIdQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByRenterIdQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingByVehicleIdAndDateQuery;
import com.codexateam.platform.booking.domain.services.BookingQueryService;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BookingQueryService.
 */
@Service
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;

    public BookingQueryServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<Booking> handle(GetBookingsByRenterIdQuery query) {
        return bookingRepository.findByRenterId(query.renterId());
    }

    @Override
    public List<Booking> handle(GetBookingsByOwnerIdQuery query) {
        return bookingRepository.findByOwnerId(query.ownerId());
    }

    @Override
    public Optional<Booking> handle(GetBookingByVehicleIdAndDateQuery query) {
        Date t = query.timestamp();
        return bookingRepository.findFirstByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(query.vehicleId(), t, t);
    }
}
