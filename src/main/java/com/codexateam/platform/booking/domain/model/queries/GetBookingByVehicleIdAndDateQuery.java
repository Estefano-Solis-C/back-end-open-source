package com.codexateam.platform.booking.domain.model.queries;

import java.util.Date;

public record GetBookingByVehicleIdAndDateQuery(Long vehicleId, Date timestamp) {
}

