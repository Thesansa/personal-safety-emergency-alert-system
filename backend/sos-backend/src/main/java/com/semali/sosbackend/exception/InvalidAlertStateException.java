package com.semali.sosbackend.exception;

public class InvalidAlertStateException extends RuntimeException {
    public InvalidAlertStateException(String message) {
        super(message);
    }
}