package org.example.foodorderservice.models.dto.responses;

public record FoodOrderDetailResponse(
        Long id,
        Long menuItemId,
        String menuItemName,
        Integer quantity,
        Double currentPrice,
        Double lineTotal
) {
}
