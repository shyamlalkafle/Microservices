package com.microservices.OrderService.service;

import com.microservices.OrderService.entity.Order;
import com.microservices.OrderService.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;

    public OrderService(OrderRepository orderRepository, UserServiceClient userServiceClient) {
        this.orderRepository = orderRepository;
        this.userServiceClient = userServiceClient;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(Order order) {
        if (!userServiceClient.userExists(order.getUserId())) {
            throw new IllegalArgumentException("User with id " + order.getUserId() + " does not exist");
        }
        order.setOrderDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public boolean deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            return false;
        }
        orderRepository.deleteById(id);
        return true;
    }
}
