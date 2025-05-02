// OrdersController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.dto.OrderRequest;
import com.example.e_commerce.entity.Orders;
import com.example.e_commerce.service.OrdersService;
import com.example.e_commerce.service.NotificationService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {
    private final OrdersService orderService;
    private final NotificationService notificationService;

    @PostMapping("/place")
    public Orders placeOrder(@RequestBody OrderRequest req, Principal principal) {
        // Add debugging
        System.out.println("Order placement requested by: " + (principal != null ? principal.getName() : "unknown user"));
        System.out.println("Request data: " + req.toString());
        
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

    @PostMapping("/{id}/status-update")
    public ResponseEntity<Orders> updateOrderStatusPost(
            @PathVariable Long id, 
            @RequestBody Map<String, String> statusUpdate) {
        
        String status = statusUpdate.get("status");
        Orders updatedOrder = orderService.updateOrderStatus(id, status);
        
        // Notify the customer about the status change
        if (updatedOrder.getUser() != null && "SHIPPED".equals(status)) {
            notificationService.createNotification(
                updatedOrder.getUser().getUserId(),
                "Your order #" + id + " has been approved and shipped!",
                "ORDER_SHIPPED",
                "/profile?tab=orders"
            );
        }
        
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Orders> refundAndCancelOrder(@PathVariable Long id) {
        Orders updatedOrder = orderService.refundAndCancelOrder(id);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/all")
    public List<Orders> all() { 
        return orderService.getAllOrders(); 
    }

    @GetMapping("/{id}")
    public Orders byId(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    @PostMapping("/{orderId}/approve-seller-items")
    public ResponseEntity<Orders> approveSellerItems(
        @PathVariable Long orderId,
        @RequestBody Map<String, Long> requestBody,
        Principal principal
    ) {
        Long sellerId = requestBody.get("sellerId");
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }
        
        // Verify the principal has permission to approve these items
        // (Check if the authenticated user is the seller)
        
        Orders updatedOrder = orderService.approveSellerItems(orderId, sellerId);
        return ResponseEntity.ok(updatedOrder);
    }
}
