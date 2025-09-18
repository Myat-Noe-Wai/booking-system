package com.bookingsystem.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GeneralException extends RuntimeException{
    public GeneralException(String error) {
        super(error);
    }
}
