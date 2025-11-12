package com.codexateam.platform.reviews.interfaces.rest;

import com.codexateam.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.codexateam.platform.reviews.domain.model.queries.GetReviewsByRenterIdQuery;
import com.codexateam.platform.reviews.domain.model.queries.GetReviewsByVehicleIdQuery;
import com.codexateam.platform.reviews.domain.services.ReviewCommandService;
import com.codexateam.platform.reviews.domain.services.ReviewQueryService;
import com.codexateam.platform.reviews.interfaces.rest.resources.CreateReviewResource;
import com.codexateam.platform.reviews.interfaces.rest.resources.ReviewResource;
import com.codexateam.platform.reviews.interfaces.rest.transform.CreateReviewCommandFromResourceAssembler;
import com.codexateam.platform.reviews.interfaces.rest.transform.ReviewResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for the Reviews bounded context.
 * Handles API requests for creating and viewing reviews.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Endpoints for managing reviews")
public class ReviewsController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    public ReviewsController(ReviewCommandService reviewCommandService, ReviewQueryService reviewQueryService) {
        this.reviewCommandService = reviewCommandService;
        this.reviewQueryService = reviewQueryService;
    }

    /**
     * Extracts the authenticated user's ID from the security context.
     * @return The authenticated user's ID.
     */
    private Long getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Creates a new review for a vehicle.
     * Requires ARRENDATARIO role.
     *
     * @param resource The review data (vehicleId, rating, comment).
     * @return The created review resource.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARRENDATARIO')")
    public ResponseEntity<ReviewResource> createReview(@RequestBody CreateReviewResource resource) {
        Long renterId = getAuthenticatedUserId();
        var command = CreateReviewCommandFromResourceAssembler.toCommandFromResource(resource, renterId);
        
        var review = reviewCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Error creating review."));
        
        var reviewResource = ReviewResourceFromEntityAssembler.toResourceFromEntity(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResource);
    }

    /**
     * Gets all reviews for a specific vehicle. Publicly accessible.
     *
     * @param vehicleId The ID of the vehicle.
     * @return A list of review resources.
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ReviewResource>> getReviewsByVehicleId(@PathVariable Long vehicleId) {
        var query = new GetReviewsByVehicleIdQuery(vehicleId);
        var reviews = reviewQueryService.handle(query);
        var resources = reviews.stream()
                .map(ReviewResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
    
    /**
     * Gets all reviews written by the authenticated renter.
     * Requires ARRENDATARIO role.
     * @return A list of the renter's review resources.
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('ROLE_ARRENDATARIO')")
    public ResponseEntity<List<ReviewResource>> getMyReviews() {
        Long renterId = getAuthenticatedUserId();
        var query = new GetReviewsByRenterIdQuery(renterId);
        var reviews = reviewQueryService.handle(query);
        var resources = reviews.stream()
                .map(ReviewResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
