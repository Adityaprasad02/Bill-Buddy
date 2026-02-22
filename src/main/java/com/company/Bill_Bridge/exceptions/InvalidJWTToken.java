package com.company.Bill_Bridge.exceptions;

public class InvalidJWTToken extends RuntimeException {
    public InvalidJWTToken(String message) {
        super(message);
    }
}
