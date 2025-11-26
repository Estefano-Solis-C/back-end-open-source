package com.codexateam.platform.iot.domain.exceptions;

/**
 * Exception thrown when a device (IoT device) is not found.
 */
public class DeviceNotFoundException extends RuntimeException {
    /**
     * Constructs a new DeviceNotFoundException with the device ID.
     * @param id The ID of the device that was not found
     */
    public DeviceNotFoundException(Long id) {
        super("Device with ID " + id + " not found.");
    }
}

