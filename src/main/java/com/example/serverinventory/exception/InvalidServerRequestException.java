package com.example.serverinventory.exception;

public class InvalidServerRequestException extends RuntimeException {

    public InvalidServerRequestException(String message) {
        super(message);
    }
}
