package org.example.foodorderservice.exceptions;

public class MenuItemServiceException extends RuntimeException {

    public MenuItemServiceException(String message) {
        super(message);
    }

    public MenuItemServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
