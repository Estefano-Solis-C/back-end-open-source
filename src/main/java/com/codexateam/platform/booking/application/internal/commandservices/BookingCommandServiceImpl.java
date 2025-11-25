package com.codexateam.platform.booking.application.internal.commandservices;

import com.codexateam.platform.booking.application.internal.outboundservices.acl.ExternalListingsService;
import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.commands.CancelBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.ConfirmBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.CreateBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.RejectBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.DeleteBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.UpdateBookingCommand;
import com.codexateam.platform.booking.domain.services.BookingCommandService;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import com.codexateam.platform.booking.domain.exceptions.BookingNotFoundException;
import com.codexateam.platform.booking.domain.exceptions.VehicleNotAvailableException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
                .orElseThrow(() -> new VehicleNotAvailableException(command.vehicleId(), "not found"));

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
            throw new VehicleNotAvailableException(command.vehicleId(), vehicleResource.status());
        }

        // Validate no overlapping bookings (PENDING or CONFIRMED)
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
            command.vehicleId(),
            command.startDate(),
            command.endDate()
        );
        if (hasOverlap) {
            throw new VehicleNotAvailableException(command.vehicleId(), "dates overlap");
        }
    }

    /**
     * Calculates the total booking price based on daily rate and duration.
     * Minimum rental period is 1 day.
     */
    private Double calculateTotalPrice(Double pricePerDay, java.util.Date startDate, java.util.Date endDate) {
        long diffInMillis = Math.abs(endDate.getTime() - startDate.getTime());
        long days = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillis, java.util.concurrent.TimeUnit.MILLISECONDS);

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
            throw new BookingNotFoundException(command.bookingId());
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

            // Update vehicle status to rented via ACL (lowercase)
            externalListingsService.updateVehicleStatus(bookingToConfirm.getVehicleId(), "rented");

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
            throw new BookingNotFoundException(command.bookingId());
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

            // Update vehicle status back to available via ACL
            externalListingsService.updateVehicleStatus(bookingToReject.getVehicleId(), "available");

            return Optional.of(bookingToReject);
        } catch (Exception e) {
            System.err.println("Error rejecting booking: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Handles the CancelBookingCommand.
     * Cancels a booking by changing its status to CANCELED.
     * Only the renter who created the booking can cancel it.
     */
    @Override
    public Optional<Booking> handle(CancelBookingCommand command) {
        var booking = bookingRepository.findById(command.bookingId());

        if (booking.isEmpty()) {
            throw new BookingNotFoundException(command.bookingId());
        }

        var bookingToCancel = booking.get();

        // Validate that the booking belongs to the renter
        if (!bookingToCancel.getRenterId().equals(command.renterId())) {
            throw new SecurityException(
                "You are not authorized to cancel this booking. Booking belongs to another user."
            );
        }

        // Validate that the booking is in PENDING or CONFIRMED status
        if (!"PENDING".equals(bookingToCancel.getStatus()) && !"CONFIRMED".equals(bookingToCancel.getStatus())) {
            throw new IllegalArgumentException(
                "Only bookings with PENDING or CONFIRMED status can be canceled. Current status: " + bookingToCancel.getStatus()
            );
        }

        // Cancel the booking
        bookingToCancel.cancel();

        try {
            bookingRepository.save(bookingToCancel);

            // Update vehicle status back to available via ACL
            externalListingsService.updateVehicleStatus(bookingToCancel.getVehicleId(), "available");

            return Optional.of(bookingToCancel);
        } catch (Exception e) {
            System.err.println("Error canceling booking: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Handles the DeleteBookingCommand.
     * Deletes a booking from the database.
     */
    @Override
    public void handle(DeleteBookingCommand command) {
        if (!bookingRepository.existsById(command.bookingId())) {
            throw new BookingNotFoundException(command.bookingId());
        }
        bookingRepository.deleteById(command.bookingId());
    }

    /**
     * Handles the UpdateBookingCommand.
     * Updates endDate and totalPrice of the booking.
     */
    @Override
    public Optional<Booking> handle(UpdateBookingCommand command) {
        var bookingOpt = bookingRepository.findById(command.bookingId());
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException(command.bookingId());
        }
        var bookingToUpdate = bookingOpt.get();
        bookingToUpdate.setEndDate(command.endDate());
        bookingToUpdate.setTotalPrice(command.totalPrice());
        bookingRepository.save(bookingToUpdate);
        return Optional.of(bookingToUpdate);
    }
}
