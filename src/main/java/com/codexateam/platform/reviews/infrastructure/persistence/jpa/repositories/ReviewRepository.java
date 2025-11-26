package com.codexateam.platform.reviews.infrastructure.persistence.jpa.repositories;

import com.codexateam.platform.reviews.domain.model.aggregates.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository interface for the Review aggregate root.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * Finds all reviews for a specific vehicle.
     * @param vehicleId The ID of the vehicle.
     * @return list of reviews for the vehicle
     */
    List<Review> findByVehicleId(Long vehicleId);
    
    /**
     * Finds all reviews written by a specific renter.
     * @param renterId The ID of the renter (Arrendatario).
     * @return list of reviews by the renter
     */
    List<Review> findByRenterId(Long renterId);

    /**
     * Checks if a review already exists for a given vehicle and renter combination.
     * @param vehicleId vehicle identifier
     * @param renterId renter identifier
     * @return true if a review exists, false otherwise
     */
    boolean existsByVehicleIdAndRenterId(Long vehicleId, Long renterId);

    /**
     * Deletes all reviews associated with a specific vehicle (cascade cleanup scenario).
     * @param vehicleId vehicle identifier
     */
    void deleteByVehicleId(Long vehicleId);
}
