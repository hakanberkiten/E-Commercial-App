// OrderItemServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.entity.Orders;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.repository.OrderItemRepository;
import com.example.e_commerce.repository.OrdersRepository;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository itemRepo;
    private final OrdersRepository orderRepo;
    private final ProductRepository productRepo;

    @Override
    public OrderItem saveOrderItem(OrderItem item) {
        Orders o = orderRepo.findById(item.getOrder().getOrderId())
            .orElseThrow(() -> new RuntimeException("Order not found"));
        Product p = productRepo.findById(item.getProduct().getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        item.setOrder(o);
        item.setProduct(p);
        return itemRepo.save(item);
    }

    @Override
    public List<OrderItem> getAllOrderItems() { return itemRepo.findAll(); }

    @Override
    public OrderItem getOrderItemById(Long id) {
        return itemRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("OrderItem not found"));
    }

    @Override
    public void deleteOrderItem(Long id) {
        itemRepo.deleteById(id);
    }
}
