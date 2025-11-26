package com.codexateam.platform.booking.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * Value Object representing the status of a booking.
 * Encapsulates booking status validation and business rules.
 */
@Embeddable
public record BookingStatus(String status) {

    /** Estado inicial de una reserva recién creada */
    public static final String PENDING = "PENDING";
    /** Estado cuando la reserva fue confirmada por el propietario */
    public static final String CONFIRMED = "CONFIRMED";
    /** Estado cuando la reserva fue rechazada por el propietario */
    public static final String REJECTED = "REJECTED";
    /** Estado cuando la reserva fue cancelada por el arrendatario */
    public static final String CANCELED = "CANCELED";

    /**
     * Default constructor that validates the status value.
     * @param status The status value to encapsulate
     * @throws IllegalArgumentException if status is null or invalid
     */
    public BookingStatus {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Booking status cannot be null or empty");
        }
    }

    /**
     * Creates a PENDING status.
     * @return A new BookingStatus with PENDING value
     */
    public static BookingStatus pending() {
        return new BookingStatus(PENDING);
    }

    /**
     * Creates a CONFIRMED status.
     * @return A new BookingStatus with CONFIRMED value
     */
    public static BookingStatus confirmed() {
        return new BookingStatus(CONFIRMED);
    }

    /**
     * Creates a REJECTED status.
     * @return A new BookingStatus with REJECTED value
     */
    public static BookingStatus rejected() {
        return new BookingStatus(REJECTED);
    }

    /**
     * Creates a CANCELED status.
     * @return A new BookingStatus with CANCELED value
     */
    public static BookingStatus canceled() {
        return new BookingStatus(CANCELED);
    }

    /**
     * Checks if the status is PENDING.
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return PENDING.equals(status);
    }

    /**
     * Checks if the status is CONFIRMED.
     * @return true if status is CONFIRMED
     */
    public boolean isConfirmed() {
        return CONFIRMED.equals(status);
    }

    /**
     * Checks if the status is REJECTED.
     * @return true if status is REJECTED
     */
    public boolean isRejected() {
        return REJECTED.equals(status);
    }

    /**
     * Checks if the status is CANCELED.
     * @return true if status is CANCELED
     */
    public boolean isCanceled() {
        return CANCELED.equals(status);
    }

    /**
     * Checks if the booking is in an active state (PENDING or CONFIRMED).
     * @return true if status is PENDING or CONFIRMED
     */
    public boolean isActive() {
        return isPending() || isConfirmed();
    }

    /**
     * Compares the internal value with the provided one, ignoring case.
     * @param other The value to compare with
     * @return true if both values are equal ignoring case
     */
    public boolean equalsIgnoreCase(String other) { return status.equalsIgnoreCase(other); }

    /**
     * Checks if the provided value represents an active state (PENDING or CONFIRMED).
     * @param value The value to check
     * @return true if the value represents an active state
     */
    public static boolean isActive(String value) { return PENDING.equalsIgnoreCase(value) || CONFIRMED.equalsIgnoreCase(value); }
}
