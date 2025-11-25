package com.codexateam.platform.listings.domain.exceptions;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long id) { super("Vehicle with ID " + id + " not found."); }
}

