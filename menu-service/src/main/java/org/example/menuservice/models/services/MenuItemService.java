package org.example.menuservice.models.services;

import org.example.menuservice.models.entities.MenuItem;

public interface MenuItemService {
    MenuItem getMenuItemById(Long id);
}
