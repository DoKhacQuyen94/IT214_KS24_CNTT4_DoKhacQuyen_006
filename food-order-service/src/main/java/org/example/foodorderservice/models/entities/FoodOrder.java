package org.example.foodorderservice.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.foodorderservice.models.constants.FoodOrderStatus;

@Entity
@Table(name = "food_orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FoodOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FoodOrderStatus status;

}
