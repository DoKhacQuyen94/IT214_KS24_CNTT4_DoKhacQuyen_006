package org.example.foodorderservice.models.repositories;

import org.example.foodorderservice.models.entities.FoodOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOrderDetailRepository extends JpaRepository<FoodOrderDetail, Long> {
}
