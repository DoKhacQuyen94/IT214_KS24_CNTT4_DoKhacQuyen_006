package org.example.foodorderservice.models.dto.responses;

import com.fasterxml.jackson.annotation.JsonAlias;

public record MenuItemResponse(
        @JsonAlias("id") Long menuItemId,
        String name,
        Double currentPrice
) {
}
