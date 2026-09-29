package org.example.menuservice.models.services;

import org.example.menuservice.exceptions.MenuItemNotFoundException;
import org.example.menuservice.models.entities.MenuItem;
import org.example.menuservice.models.repositories.MenuItemRepository;
import org.example.menuservice.models.services.impl.MenuItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuItemServiceImplTest {

    private MenuItemRepository menuItemRepository;
    private MenuItemService menuItemService;

    @BeforeEach
    void setUp() {
        menuItemRepository = mock(MenuItemRepository.class);
        menuItemService = new MenuItemServiceImpl(menuItemRepository);
    }

    @Test
    void getMenuItemByIdReturnsMenuItemWhenItExists() {
        MenuItem menuItem = new MenuItem(1L, "Laptop", "Laptop văn phòng", new BigDecimal("15000000.00"), 5);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        MenuItem result = menuItemService.getMenuItemById(1L);

        assertSame(menuItem, result);
        verify(menuItemRepository).findById(1L);
    }

    @Test
    void getMenuItemByIdThrowsExceptionWhenItDoesNotExist() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        MenuItemNotFoundException exception = assertThrows(
                MenuItemNotFoundException.class,
                () -> menuItemService.getMenuItemById(99L)
        );

        assertEquals("Không tìm thấy món ăn với id: 99", exception.getMessage());
    }

    @Test
    void getMenuItemByIdUsesMenuItemsCacheWithIdAsKey() throws NoSuchMethodException {
        Cacheable cacheable = MenuItemServiceImpl.class
                .getMethod("getMenuItemById", Long.class)
                .getAnnotation(Cacheable.class);

        assertEquals("menuItems", cacheable.cacheNames()[0]);
        assertEquals("#id", cacheable.key());
        assertTrue(cacheable.sync());
        assertTrue(Serializable.class.isAssignableFrom(MenuItem.class));
    }
}
