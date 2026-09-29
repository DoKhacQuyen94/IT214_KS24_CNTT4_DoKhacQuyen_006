package org.example.foodorderservice.clients;

import org.example.foodorderservice.models.dto.responses.MenuItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "menu-service", path = "/api/v1/menu-items")
public interface MenuItemClient {

    @GetMapping("/{id}")
    MenuItemResponse getMenuItemById(@PathVariable Long id);
}
