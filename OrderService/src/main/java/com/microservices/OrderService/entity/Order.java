package com.microservices.OrderService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String productName;

    private Integer quantity;

    private Double totalPrice;

    private LocalDateTime orderDate;

    @PrePersist
    void doActivity(){
        this.orderDate = LocalDateTime.now();
    }

}
