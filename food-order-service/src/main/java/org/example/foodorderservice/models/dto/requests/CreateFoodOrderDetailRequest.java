package org.example.foodorderservice.models.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateFoodOrderDetailRequest(
        @NotNull(message = "MenuItem id is required")
        @Min(value = 1, message = "MenuItem id must be greater than 0")
        Long menuItemId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        Integer quantity
) {
}
