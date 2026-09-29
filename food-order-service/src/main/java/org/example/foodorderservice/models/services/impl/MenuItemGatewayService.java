package org.example.foodorderservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.foodorderservice.clients.MenuItemClient;
import org.example.foodorderservice.models.dto.responses.MenuItemResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuItemGatewayService {

    private final MenuItemClient menuItemClient;

    public MenuItemResponse getMenuItemById(Long id) {
        return menuItemClient.getMenuItemById(id);
    }
}
