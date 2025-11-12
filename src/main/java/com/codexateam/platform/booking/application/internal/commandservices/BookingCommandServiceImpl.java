package com.codexateam.platform.booking.application.internal.commandservices;

import com.codexateam.platform.booking.application.internal.outboundservices.acl.ExternalListingsService;
import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.commands.ConfirmBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.CreateBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.RejectBookingCommand;
import com.codexateam.platform.booking.domain.services.BookingCommandService;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of BookingCommandService.
 */
@Service
public class BookingCommandServiceImpl implements BookingCommandService {

    private final BookingRepository bookingRepository;
    private final ExternalListingsService externalListingsService;

    public BookingCommandServiceImpl(BookingRepository bookingRepository, ExternalListingsService externalListingsService) {
        this.bookingRepository = bookingRepository;
        this.externalListingsService = externalListingsService;
    }

    /**
     * Handles the CreateBookingCommand.
     * 1. Fetches vehicle data (price, owner) via ACL.
     * 2. Validates dates and vehicle availability.
     * 3. Calculates total price.
     * 4. Saves the new booking.
     * 5. (Future) Update vehicle status via ACL.
     */
    @Override
    public Optional<Booking> handle(CreateBookingCommand command) {
        
        // 1. Fetch vehicle data using ACL - throws exception if not found
        var vehicleResource = externalListingsService.fetchVehicleById(command.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Vehicle with ID " + command.vehicleId() + " not found."
                ));

        // 2. Validate business rules
        validateBookingRequest(command, vehicleResource);

        // 3. Calculate total price based on vehicle's daily rate
        Double totalPrice = calculateTotalPrice(
            vehicleResource.pricePerDay(),
            command.startDate(),
            command.endDate()
        );

        // 4. Create and save booking aggregate
        var booking = new Booking(command, totalPrice);
        try {
            bookingRepository.save(booking);
            
            // 5. (Future Enhancement) Notify Listings context to update vehicle status
            // This would mark the vehicle as "RESERVED" or "RENTED" during the booking period
            // externalListingsService.updateVehicleStatus(command.vehicleId(), BookingStatus.CONFIRMED);

            return Optional.of(booking);
        } catch (Exception e) {
            // Log error details for debugging
            System.err.println("Error saving booking: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Validates the booking request against business rules.
     */
    private void validateBookingRequest(CreateBookingCommand command,
                                       com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource vehicleResource) {
        // Validate owner ID matches
        if (!vehicleResource.ownerId().equals(command.ownerId())) {
            throw new IllegalArgumentException(
                "Owner ID mismatch for vehicle " + command.vehicleId()
            );
        }

        // Validate date logic
        if (command.startDate().after(command.endDate())) {
            throw new IllegalArgumentException(
                "Start date must be before end date."
            );
        }

        // Validate vehicle is available (status should be "available")
        if (!"available".equalsIgnoreCase(vehicleResource.status())) {
            throw new IllegalArgumentException(
                "Vehicle " + command.vehicleId() + " is not available for booking. Current status: " + vehicleResource.status()
            );
        }

        // (Future Enhancement) Validate no overlapping bookings
        // This would require a repository method like:
        // boolean hasOverlap = bookingRepository.existsOverlappingBooking(
        //     command.vehicleId(),
        //     command.startDate(),
        //     command.endDate()
        // );
        // if (hasOverlap) {
        //     throw new IllegalArgumentException(
        //         "Vehicle is not available for the selected dates."
        //     );
        // }
    }

    /**
     * Calculates the total booking price based on daily rate and duration.
     * Minimum rental period is 1 day.
     */
    private Double calculateTotalPrice(Double pricePerDay, java.util.Date startDate, java.util.Date endDate) {
        long diffInMillis = Math.abs(endDate.getTime() - startDate.getTime());
        long days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        // Ensure minimum 1 day rental
        if (days == 0) {
            days = 1;
        }

        return pricePerDay * days;
    }

    /**
     * Handles the ConfirmBookingCommand.
     * Confirms a booking by changing its status to CONFIRMED.
     */
    @Override
    public Optional<Booking> handle(ConfirmBookingCommand command) {
        var booking = bookingRepository.findById(command.bookingId());

        if (booking.isEmpty()) {
            throw new IllegalArgumentException("Booking with ID " + command.bookingId() + " not found.");
        }

        var bookingToConfirm = booking.get();

        // Validate that the booking is in PENDING status
        if (!"PENDING".equals(bookingToConfirm.getStatus())) {
            throw new IllegalArgumentException(
                "Only bookings with PENDING status can be confirmed. Current status: " + bookingToConfirm.getStatus()
            );
        }

        // Confirm the booking
        bookingToConfirm.confirm();

        try {
            bookingRepository.save(bookingToConfirm);
            return Optional.of(bookingToConfirm);
        } catch (Exception e) {
            System.err.println("Error confirming booking: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Handles the RejectBookingCommand.
     * Rejects a booking by changing its status to REJECTED.
     */
    @Override
    public Optional<Booking> handle(RejectBookingCommand command) {
        var booking = bookingRepository.findById(command.bookingId());

        if (booking.isEmpty()) {
            throw new IllegalArgumentException("Booking with ID " + command.bookingId() + " not found.");
        }

        var bookingToReject = booking.get();

        // Validate that the booking is in PENDING status
        if (!"PENDING".equals(bookingToReject.getStatus())) {
            throw new IllegalArgumentException(
                "Only bookings with PENDING status can be rejected. Current status: " + bookingToReject.getStatus()
            );
        }

        // Reject the booking
        bookingToReject.reject();

        try {
            bookingRepository.save(bookingToReject);
            return Optional.of(bookingToReject);
        } catch (Exception e) {
            System.err.println("Error rejecting booking: " + e.getMessage());
            return Optional.empty();
        }
    }
}
