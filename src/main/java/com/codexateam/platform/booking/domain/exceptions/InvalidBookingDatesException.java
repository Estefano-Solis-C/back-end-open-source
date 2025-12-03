package com.codexateam.platform.booking.domain.exceptions;

import com.codexateam.platform.shared.domain.exceptions.DomainException;
import java.util.Date;

/**
 * Exception thrown when booking dates are invalid.
 * For example, when the start date is after the end date.
 */
public class InvalidBookingDatesException extends DomainException {

    private final Date startDate;
    private final Date endDate;

    /**
     * Constructs a new InvalidBookingDatesException.
     *
     * @param startDate The invalid start date
     * @param endDate The invalid end date
     * @param message The detailed error message
     */
    public InvalidBookingDatesException(Date startDate, Date endDate, String message) {
        super(message);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Constructs a new InvalidBookingDatesException for start date after end date.
     *
     * @param startDate The start date
     * @param endDate The end date
     */
    public InvalidBookingDatesException(Date startDate, Date endDate) {
        super(String.format("Start date %s must be before end date %s", startDate, endDate));
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}

