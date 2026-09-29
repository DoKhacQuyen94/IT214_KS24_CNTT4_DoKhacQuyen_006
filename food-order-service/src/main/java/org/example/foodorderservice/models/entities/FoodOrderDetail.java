package org.example.foodorderservice.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_order_details")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FoodOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "food_order_id")
    private FoodOrder foodOrder;

    @Column(name = "menu_item_id")
    private Long menuItemId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "current_price")
    private Double currentPrice;
}
