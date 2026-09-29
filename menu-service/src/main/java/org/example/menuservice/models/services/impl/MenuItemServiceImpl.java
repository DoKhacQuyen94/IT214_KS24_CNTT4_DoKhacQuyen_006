package org.example.menuservice.models.services.impl;

import org.example.menuservice.exceptions.MenuItemNotFoundException;
import org.example.menuservice.models.entities.MenuItem;
import org.example.menuservice.models.repositories.MenuItemRepository;
import org.example.menuservice.models.services.MenuItemService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemServiceImpl(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "menuItems", key = "#id", sync = true)
    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
    }
}
