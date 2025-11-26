package com.codexateam.platform.booking.application.internal.queryservices;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByOwnerIdQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingsByRenterIdQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingByVehicleIdAndDateQuery;
import com.codexateam.platform.booking.domain.model.queries.GetBookingByIdQuery;
import com.codexateam.platform.booking.domain.services.BookingQueryService;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import com.codexateam.platform.booking.domain.exceptions.BookingNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BookingQueryService.
 * Handles all booking-related queries following CQRS pattern.
 */
@Service
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;

    public BookingQueryServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Retrieves all bookings made by a specific renter.
     * @param query The query containing the renter ID
     * @return List of bookings made by the renter
     */
    @Override
    public List<Booking> handle(GetBookingsByRenterIdQuery query) {
        return bookingRepository.findByRenterId(query.renterId());
    }

    /**
     * Retrieves all bookings associated with vehicles owned by a specific owner.
     * @param query The query containing the owner ID
     * @return List of bookings for the owner's vehicles
     */
    @Override
    public List<Booking> handle(GetBookingsByOwnerIdQuery query) {
        return bookingRepository.findByOwnerId(query.ownerId());
    }

    /**
     * Finds a booking for a specific vehicle that includes the given timestamp.
     * @param query The query containing vehicle ID and timestamp
     * @return An Optional containing the booking if found, empty otherwise
     */
    @Override
    public Optional<Booking> handle(GetBookingByVehicleIdAndDateQuery query) {
        Date t = query.timestamp();
        return bookingRepository.findFirstByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(query.vehicleId(), t, t);
    }

    /**
     * Retrieves a booking by its unique identifier.
     * @param query The query containing the booking ID
     * @return An Optional containing the booking if found
     * @throws BookingNotFoundException if the booking does not exist
     */
    @Override
    public Optional<Booking> handle(GetBookingByIdQuery query) {
        var bookingOpt = bookingRepository.findById(query.bookingId());
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException(query.bookingId());
        }
        return bookingOpt;
    }
}
