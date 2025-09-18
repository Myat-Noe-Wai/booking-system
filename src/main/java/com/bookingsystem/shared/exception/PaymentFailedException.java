package com.bookingsystem.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String error) {
        super(error);
    }
}
