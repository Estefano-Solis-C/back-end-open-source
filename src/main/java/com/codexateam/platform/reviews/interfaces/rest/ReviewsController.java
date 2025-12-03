package com.codexateam.platform.reviews.interfaces.rest;

import com.codexateam.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.codexateam.platform.reviews.domain.model.queries.GetReviewsByRenterIdQuery;
import com.codexateam.platform.reviews.domain.model.queries.GetReviewsByVehicleIdQuery;
import com.codexateam.platform.reviews.domain.model.queries.GetReviewByIdQuery;
import com.codexateam.platform.reviews.domain.exceptions.ReviewNotFoundException;
import com.codexateam.platform.reviews.domain.exceptions.ReviewAlreadyExistsException;
import com.codexateam.platform.reviews.domain.exceptions.CompletedBookingRequiredException;
import com.codexateam.platform.reviews.domain.services.ReviewCommandService;
import com.codexateam.platform.reviews.domain.services.ReviewQueryService;
import com.codexateam.platform.reviews.interfaces.rest.resources.CreateReviewResource;
import com.codexateam.platform.reviews.interfaces.rest.resources.ReviewResource;
import com.codexateam.platform.reviews.interfaces.rest.transform.CreateReviewCommandFromResourceAssembler;
import com.codexateam.platform.reviews.interfaces.rest.transform.ReviewResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing reviews.
 * Provides endpoints to create and retrieve vehicle reviews.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Endpoints for managing reviews")
public class ReviewsController {
    private static final String ERROR_USER_NOT_AUTHENTICATED = "User not authenticated";
    private static final String ERROR_CREATING_REVIEW = "Error creating review";
    private static final String ANONYMOUS_USER = "anonymousUser";

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    public ReviewsController(ReviewCommandService reviewCommandService, ReviewQueryService reviewQueryService) {
        this.reviewCommandService = reviewCommandService;
        this.reviewQueryService = reviewQueryService;
    }

    /**
     * Extracts authenticated user ID.
     * @return user id
     */
    private Long getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || ANONYMOUS_USER.equals(authentication.getPrincipal())) {
            throw new SecurityException(ERROR_USER_NOT_AUTHENTICATED);
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Creates a new review (renter only).
     * @param resource review payload
     * @return created review resource
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARRENDATARIO')")
    @Operation(summary = "Create Review", description = "Create a new review for a vehicle (ROLE_ARRENDATARIO)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ReviewResource> createReview(@RequestBody CreateReviewResource resource) {
        Long renterId = getAuthenticatedUserId();
        var command = CreateReviewCommandFromResourceAssembler.toCommandFromResource(resource, renterId);
        var review = reviewCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException(ERROR_CREATING_REVIEW));
        var reviewResource = ReviewResourceFromEntityAssembler.toResourceFromEntity(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResource);
    }

    /**
     * Retrieves reviews for a vehicle (public).
     * @param vehicleId vehicle identifier
     * @return list of reviews
     */
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get Reviews by Vehicle", description = "Get all reviews for a specific vehicle (Public)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews found")
    })
    public ResponseEntity<List<ReviewResource>> getReviewsByVehicleId(@PathVariable Long vehicleId) {
        var query = new GetReviewsByVehicleIdQuery(vehicleId);
        var reviews = reviewQueryService.handle(query);
        var resources = reviews.stream()
                .map(ReviewResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves reviews written by authenticated renter.
     * @return list of renter reviews
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('ROLE_ARRENDATARIO')")
    @Operation(summary = "Get Renter Reviews", description = "Get all reviews written by the authenticated renter (ROLE_ARRENDATARIO)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ReviewResource>> getMyReviews() {
        Long renterId = getAuthenticatedUserId();
        var query = new GetReviewsByRenterIdQuery(renterId);
        var reviews = reviewQueryService.handle(query);
        var resources = reviews.stream()
                .map(ReviewResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a single review by its ID.
     * @param reviewId review identifier
     * @return review resource if found
     */
    @GetMapping("/{reviewId}")
    @Operation(summary = "Get Review by ID", description = "Retrieve a specific review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review found"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ReviewResource> getReviewById(@PathVariable Long reviewId) {
        var query = new GetReviewByIdQuery(reviewId);
        var review = reviewQueryService.handle(query)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        var resource = ReviewResourceFromEntityAssembler.toResourceFromEntity(review);
        return ResponseEntity.ok(resource);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<String> handleReviewNotFoundException(ReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<String> handleReviewAlreadyExistsException(ReviewAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(CompletedBookingRequiredException.class)
    public ResponseEntity<String> handleCompletedBookingRequiredException(CompletedBookingRequiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
