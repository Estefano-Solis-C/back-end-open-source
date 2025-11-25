package com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository interface for the Booking aggregate root.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    /**
     * Finds all bookings made by a specific renter.
     * @param renterId The ID of the renter (Arrendatario).
     */
    List<Booking> findByRenterId(Long renterId);
    
    /**
     * Finds all bookings associated with a specific owner's vehicles.
     * @param ownerId The ID of the owner (Arrendador).
     */
    List<Booking> findByOwnerId(Long ownerId);

    /**
     * Finds a booking for a vehicle that includes the given timestamp.
     */
    Optional<Booking> findFirstByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long vehicleId, Date start, Date end);

    // Added query for checking overlapping bookings with status PENDING or CONFIRMED
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
           "WHERE b.vehicleId = :vehicleId " +
           "AND b.status IN ('PENDING','CONFIRMED') " +
           "AND b.startDate < :endDate " +
           "AND b.endDate > :startDate")
    boolean existsOverlappingBooking(@Param("vehicleId") Long vehicleId,
                                     @Param("startDate") Date startDate,
                                     @Param("endDate") Date endDate);

    // Delete all bookings by vehicleId (cascade cleanup when deleting a vehicle)
    void deleteByVehicleId(Long vehicleId);
}
