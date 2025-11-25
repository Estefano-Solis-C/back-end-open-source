package com.codexateam.platform.listings.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Set;

@Embeddable
public record VehicleStatus(@Column(name = "status", nullable = false, length = 20) String value) {
    private static final Set<String> ALLOWED = Set.of("available", "rented", "maintenance");
    public VehicleStatus {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Vehicle status cannot be blank");
        var normalized = value.toLowerCase();
        if (!ALLOWED.contains(normalized)) throw new IllegalArgumentException("Invalid vehicle status: " + value);
        value = normalized;
    }
    @Override public String toString() { return value; }
}
