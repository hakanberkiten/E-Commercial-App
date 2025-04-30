// OrdersController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.dto.OrderRequest;
import com.example.e_commerce.entity.Orders;
import com.example.e_commerce.service.OrdersService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
@GetMapping("/seller/{sellerId}")
public ResponseEntity<List<Orders>> getOrdersBySellerId(@PathVariable Long sellerId) {
    List<Orders> orders = orderService.getOrdersBySellerId(sellerId);
    return ResponseEntity.ok(orders);
}

@PatchMapping("/{id}/status")
public ResponseEntity<Orders> updateOrderStatus(
        @PathVariable Long id, 
        @RequestBody Map<String, String> statusUpdate) {
    
    String status = statusUpdate.get("status");
    Orders updatedOrder = orderService.updateOrderStatus(id, status);
    return ResponseEntity.ok(updatedOrder);
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
