package com.codexateam.platform.reviews.domain.exceptions;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(Long id) { super("Review with ID " + id + " not found."); }
}

