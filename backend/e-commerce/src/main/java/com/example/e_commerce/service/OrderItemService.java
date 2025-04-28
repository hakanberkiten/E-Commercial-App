// OrderItemService.java
package com.example.e_commerce.service;

import com.example.e_commerce.entity.OrderItem;

import java.util.List;

public interface OrderItemService {
    OrderItem saveOrderItem(OrderItem item);
    List<OrderItem> getAllOrderItems();
    OrderItem getOrderItemById(Long id);
    void deleteOrderItem(Long id);
}
