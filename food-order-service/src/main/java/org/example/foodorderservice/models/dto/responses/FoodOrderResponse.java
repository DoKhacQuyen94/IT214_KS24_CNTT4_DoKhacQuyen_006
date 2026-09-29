package org.example.foodorderservice.models.dto.responses;

import org.example.foodorderservice.models.constants.FoodOrderStatus;

import java.util.List;

public record FoodOrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        Double totalAmount,
        FoodOrderStatus status,
        List<FoodOrderDetailResponse> items
) {
}
