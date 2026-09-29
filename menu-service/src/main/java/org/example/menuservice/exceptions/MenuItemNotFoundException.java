package org.example.menuservice.exceptions;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(Long id) {
        super("Không tìm thấy món ăn với id: " + id);
    }
}
