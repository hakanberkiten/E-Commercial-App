// OrdersController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.dto.OrderRequest;
import com.example.e_commerce.entity.Orders;
import com.example.e_commerce.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {
    private final OrdersService orderService;
    
@PostMapping("/place")
public Orders placeOrder(@RequestBody OrderRequest req) {
    return orderService.placeOrder(req);
}

    @PostMapping("/save")
    public Orders save(@RequestBody Orders o) {
        return orderService.saveOrder(o);
    }

    @GetMapping("/all")
    public List<Orders> all() { return orderService.getAllOrders(); }

    @GetMapping("/{id}")
    public Orders byId(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}
