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
     */
    List<Review> findByVehicleId(Long vehicleId);
    
    /**
     * Finds all reviews written by a specific renter.
     * @param renterId The ID of the renter (Arrendatario).
     */
    List<Review> findByRenterId(Long renterId);

    // Prevent duplicate review per renter per vehicle
    boolean existsByVehicleIdAndRenterId(Long vehicleId, Long renterId);

    // Delete all reviews by vehicleId (cascade cleanup when deleting a vehicle)
    void deleteByVehicleId(Long vehicleId);
}
