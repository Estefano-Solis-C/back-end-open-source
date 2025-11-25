package com.codexateam.platform.shared.domain.exceptions;

public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
}

