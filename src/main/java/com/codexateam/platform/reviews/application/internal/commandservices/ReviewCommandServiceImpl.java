package com.codexateam.platform.reviews.application.internal.commandservices;

import com.codexateam.platform.reviews.domain.model.aggregates.Review;
import com.codexateam.platform.reviews.domain.model.commands.CreateReviewCommand;
import com.codexateam.platform.reviews.domain.services.ReviewCommandService;
import com.codexateam.platform.reviews.domain.exceptions.CompletedBookingRequiredException;
import com.codexateam.platform.reviews.domain.exceptions.ReviewAlreadyExistsException;
import com.codexateam.platform.reviews.infrastructure.persistence.jpa.repositories.ReviewRepository;
import com.codexateam.platform.booking.interfaces.acl.BookingContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of ReviewCommandService.
 * Handles review creation with validation through ACL.
 */
@Service
public class ReviewCommandServiceImpl implements ReviewCommandService {


    private final ReviewRepository reviewRepository;
    private final BookingContextFacade bookingContextFacade;

    public ReviewCommandServiceImpl(ReviewRepository reviewRepository, BookingContextFacade bookingContextFacade) {
        this.reviewRepository = reviewRepository;
        this.bookingContextFacade = bookingContextFacade;
    }

    /**
     * Handles the CreateReviewCommand.
     * Validates that the renter has a completed booking for the vehicle and hasn't already reviewed it.
     *
     * @param command The command containing review details
     * @return Optional containing the created review if successful
     * @throws CompletedBookingRequiredException if renter hasn't completed a booking for this vehicle
     * @throws ReviewAlreadyExistsException if renter has already reviewed this vehicle
     */
    @Override
    public Optional<Review> handle(CreateReviewCommand command) {
        boolean hasCompletedBooking = bookingContextFacade.hasCompletedBooking(command.renterId(), command.vehicleId());
        if (!hasCompletedBooking) {
            throw new CompletedBookingRequiredException(command.renterId(), command.vehicleId());
        }
        if (reviewRepository.existsByVehicleIdAndRenterId(command.vehicleId(), command.renterId())) {
            throw new ReviewAlreadyExistsException(command.renterId(), command.vehicleId());
        }
        var review = new Review(command);
        try {
            reviewRepository.save(review);
            return Optional.of(review);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
