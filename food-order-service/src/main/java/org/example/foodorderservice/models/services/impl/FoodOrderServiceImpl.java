package org.example.foodorderservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.foodorderservice.models.constants.FoodOrderStatus;
import org.example.foodorderservice.models.dto.requests.CreateFoodOrderDetailRequest;
import org.example.foodorderservice.models.dto.requests.CreateFoodOrderRequest;
import org.example.foodorderservice.models.dto.responses.FoodOrderDetailResponse;
import org.example.foodorderservice.models.dto.responses.FoodOrderResponse;
import org.example.foodorderservice.models.dto.responses.MenuItemResponse;
import org.example.foodorderservice.models.entities.FoodOrder;
import org.example.foodorderservice.models.entities.FoodOrderDetail;
import org.example.foodorderservice.models.repositories.FoodOrderDetailRepository;
import org.example.foodorderservice.models.repositories.FoodOrderRepository;
import org.example.foodorderservice.models.services.FoodOrderService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodOrderServiceImpl implements FoodOrderService {

        private final FoodOrderRepository foodOrderRepository;
        private final FoodOrderDetailRepository foodOrderDetailRepository;
        private final MenuItemGatewayService menuItemGatewayService;

        @Override
        @Transactional
        public FoodOrderResponse createFoodOrder(CreateFoodOrderRequest request) {
                double totalAmount = 0.0;

                FoodOrder foodOrder = FoodOrder.builder()
                        .customerName(request.customerName())
                        .customerEmail(request.customerEmail())
                        .totalAmount(0.0)
                        .status(FoodOrderStatus.PENDING)
                        .build();
                foodOrderRepository.save(foodOrder);

                List<FoodOrderDetailResponse> itemResponses = new ArrayList<>();
                List<FoodOrderDetail> orderDetailsToSave = new ArrayList<>();

                for (CreateFoodOrderDetailRequest itemReq : request.items()) {
                        MenuItemResponse menuItem = menuItemGatewayService.getMenuItemById(itemReq.menuItemId());

                        double unitPrice = menuItem.currentPrice();
                        double subtotal = unitPrice * itemReq.quantity();
                        totalAmount += subtotal;

                        FoodOrderDetail detail = FoodOrderDetail.builder()
                                .foodOrder(foodOrder)
                                .menuItemId(menuItem.menuItemId())
                                .quantity(itemReq.quantity())
                                .currentPrice(unitPrice)
                                .build();
                        orderDetailsToSave.add(detail);

                        itemResponses.add(new FoodOrderDetailResponse(
                                null,
                                menuItem.menuItemId(),
                                menuItem.name(),
                                itemReq.quantity(),
                                unitPrice,
                                subtotal
                        ));
                }
                foodOrderDetailRepository.saveAll(orderDetailsToSave);
                foodOrder.setTotalAmount(totalAmount);
                foodOrderRepository.save(foodOrder);
                return new FoodOrderResponse(
                        foodOrder.getId(),
                        foodOrder.getCustomerName(),
                        foodOrder.getCustomerEmail(),
                        foodOrder.getTotalAmount(),
                        foodOrder.getStatus(),
                        itemResponses
                );
        }
}

