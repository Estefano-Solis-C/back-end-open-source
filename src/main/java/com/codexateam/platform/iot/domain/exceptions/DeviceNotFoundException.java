package com.codexateam.platform.iot.domain.exceptions;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(Long id) { super("Device with ID " + id + " not found."); }
}

