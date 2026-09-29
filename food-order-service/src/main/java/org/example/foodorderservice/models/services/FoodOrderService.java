package org.example.foodorderservice.models.services;

import org.example.foodorderservice.models.dto.requests.CreateFoodOrderRequest;
import org.example.foodorderservice.models.dto.responses.FoodOrderResponse;

public interface FoodOrderService {

    FoodOrderResponse createFoodOrder(CreateFoodOrderRequest request);
}
