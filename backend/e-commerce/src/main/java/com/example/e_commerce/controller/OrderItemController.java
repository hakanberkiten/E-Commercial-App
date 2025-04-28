// OrderItemController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemService itemService;

    @PostMapping("/save")
    public OrderItem save(@RequestBody OrderItem i) {
        return itemService.saveOrderItem(i);
    }

    @GetMapping("/all")
    public List<OrderItem> all() { return itemService.getAllOrderItems(); }

    @GetMapping("/{id}")
    public OrderItem byId(@PathVariable Long id) {
        return itemService.getOrderItemById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        itemService.deleteOrderItem(id);
    }
}
