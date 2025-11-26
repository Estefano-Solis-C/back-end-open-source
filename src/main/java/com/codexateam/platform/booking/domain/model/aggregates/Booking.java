package com.codexateam.platform.booking.domain.model.aggregates;

import com.codexateam.platform.booking.domain.model.commands.CreateBookingCommand;
import com.codexateam.platform.booking.domain.model.valueobjects.BookingStatus;
import com.codexateam.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Represents the Booking aggregate root in the Booking bounded context.
 * Based on the 'bookings' table in db.json.
 */
@NoArgsConstructor
@Getter
@Entity
@Table(name = "bookings") // Pluralized by naming strategy
public class Booking extends AuditableAbstractAggregateRoot<Booking> {

    @Column(nullable = false)
    private Long vehicleId;

    /**
     * Foreign key to the User (Arrendatario) who is renting the vehicle.
     * Renamed from 'userId' in db.json to 'renterId' for clarity.
     */
    @Column(nullable = false)
    private Long renterId;
    
    /**
     * Foreign key to the User (Arrendador) who owns the vehicle.
     * This is denormalized for easier query access, based on frontend needs.
     */
    @Column(nullable = false)
    private Long ownerId;

    /**
     * Renamed from 'fechaInicio' in db.json.
     */
    @Column(nullable = false)
    private Date startDate;

    /**
     * Renamed from 'fechaFin' in db.json.
     */
    @Setter
    @Column(nullable = false)
    private Date endDate;

    /**
     * Renamed from 'precioTotal' in db.json.
     */
    @Setter
    @Column(nullable = false)
    private Double totalPrice;

    /**
     * The current status of the booking (e.g., "PENDING", "CONFIRMED", "CANCELED").
     * Renamed from 'estado' in db.json.
     */
    @Embedded
    private BookingStatus bookingStatus;

    /**
     * Constructor for creating a new Booking.
     * @param command The CreateBookingCommand containing initial data.
     * @param calculatedPrice The calculated total price for the booking.
     */
    public Booking(CreateBookingCommand command, Double calculatedPrice) {
        this.vehicleId = command.vehicleId();
        this.renterId = command.renterId();
        this.ownerId = command.ownerId();
        this.startDate = command.startDate();
        this.endDate = command.endDate();
        this.totalPrice = calculatedPrice;
        this.bookingStatus = BookingStatus.pending(); // Default status on creation
    }

    /**
     * Confirms the booking.
     * Changes the status to "CONFIRMED".
     */
    public void confirm() {
        this.bookingStatus = BookingStatus.confirmed();
    }

    /**
     * Rejects the booking.
     * Changes the status to "REJECTED".
     */
    public void reject() {
        this.bookingStatus = BookingStatus.rejected();
    }

    /**
     * Cancels the booking.
     * Changes the status to "CANCELED".
     */
    public void cancel() {
        this.bookingStatus = BookingStatus.canceled();
    }

    /**
     * Gets the status value as a String.
     * @return The status value
     */
    public String getStatus() {
        return this.bookingStatus != null ? this.bookingStatus.status() : null;
    }
}
