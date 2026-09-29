package org.example.foodorderservice.models.repositories;

import org.example.foodorderservice.models.entities.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
}
