package org.example.foodorderservice.exceptions;

public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(Long menuItemId) {
        super("MenuItem not found with id: " + menuItemId);
    }
}
