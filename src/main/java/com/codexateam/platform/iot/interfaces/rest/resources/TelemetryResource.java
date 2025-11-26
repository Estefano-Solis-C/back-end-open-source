package com.codexateam.platform.iot.interfaces.rest.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Represents a telemetry data resource.
 * This class is used to transfer telemetry data with enriched information.
 */
@Getter
@Setter
@AllArgsConstructor
public class TelemetryResource {
    private Long id;
    private Long vehicleId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double fuelLevel;
    private Date timestamp;
    private List<List<Double>> plannedRoute;
    private String renterName;
}
