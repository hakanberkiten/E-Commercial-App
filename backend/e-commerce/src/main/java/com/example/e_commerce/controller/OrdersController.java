// OrdersController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.dto.OrderRequest;
import com.example.e_commerce.entity.Orders;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.service.OrdersService;
import com.example.e_commerce.service.impl.StripeServiceImpl;
import com.example.e_commerce.service.NotificationService;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.UserService; // Import UserService
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
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
    private final StripeServiceImpl stripeService;
    private final UserRepository userRepository;
    private final UserService userService; // Add this field

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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Orders>> getOrdersByUserId(@PathVariable Long userId) {
        List<Orders> orders = orderService.getOrdersByUserId(userId);
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
        if (updatedOrder.getUser() != null) {
            if ("SHIPPED".equals(status)) {
                notificationService.createNotification(
                    updatedOrder.getUser().getUserId(),
                    "Your order #" + id + " has been approved and shipped!",
                    "ORDER_SHIPPED",
                    "/profile?tab=orders"
                );
            } 
            // Add this new condition for delivery notifications
            else if ("DELIVERED".equals(status)) {
                notificationService.createNotification(
                    updatedOrder.getUser().getUserId(),
                    "Your order #" + id + " has been delivered! Enjoy your purchase.",
                    "ORDER_DELIVERED",
                    "/profile?tab=orders"
                );
            }
        }
        
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Orders> refundAndCancelOrder(
            @PathVariable Long id, 
            @RequestParam(required = false) String cancelledBy) {
        
        Orders updatedOrder = orderService.refundAndCancelOrder(id);
        
        // If cancelled by seller, create an additional notification for the customer
        if ("seller".equals(cancelledBy)) {
            Orders order = updatedOrder; // use the returned order
            if (order.getUser() != null) {
                // Find a sample seller name from order items
                String sellerName = "The seller";
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    for (OrderItem item : order.getItems()) {
                        if (item.getProduct() != null && item.getProduct().getSeller() != null) {
                            sellerName = item.getProduct().getSeller().getFirstName();
                            break;
                        }
                    }
                }
                
                notificationService.createNotification(
                    order.getUser().getUserId(),
                    sellerName + " has cancelled your order #" + id + ". A full refund has been processed.",
                    "ORDER_CANCELLED_BY_SELLER",
                    "/profile?tab=orders"
                );
            }
        }
        
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/{id}/refund-delivered")
    public ResponseEntity<Orders> refundDeliveredOrder(@PathVariable Long id) {
        Orders updatedOrder = orderService.refundDeliveredOrder(id);
        
        // Create notification for customer
        if (updatedOrder.getUser() != null) {
            notificationService.createNotification(
                updatedOrder.getUser().getUserId(),
                "Your return request for order #" + id + " has been approved. A refund has been processed to your original payment method.",
                "ORDER_RETURNED",
                "/profile?tab=orders"
            );
        }
        
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/all")
    public List<Orders> all() { 
        return orderService.getAllOrders(); 
    }

    @GetMapping("/{id}")
    public Orders byId(@PathVariable Long id, @RequestParam(required = false) Boolean forceRefresh) {
        // forceRefresh parametresi ile doğrudan veritabanından güncel veriyi çekmek için
        if (Boolean.TRUE.equals(forceRefresh)) {
            // EntityManager veya JdbcTemplate ile direkt sorgulama yapılabilir
            // Bu örnek kod değildir, sizin yapınıza göre düzenleyin
            return orderService.getOrderByIdFresh(id);
        }
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    @PostMapping("/{orderId}/approve-seller-items")
    public ResponseEntity<?> approveSellerItems(
        @PathVariable Long orderId,
        @RequestBody Map<String, Long> requestBody,
        Principal principal
    ) {
        Long sellerId = requestBody.get("sellerId");
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }
        
        // First check if seller has a payment method registered
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        // Check if the seller has a Stripe account and payment method
        if (seller.getStripeCustomerId() == null || seller.getStripeCustomerId().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "You need to add a payment method in your profile before approving orders",
                            "code", "NO_PAYMENT_METHOD"));
        }
        
        try {
            // Check if the seller has any payment methods
            List<Map<String, Object>> cards = stripeService.getCustomerCards(seller.getStripeCustomerId());
            if (cards.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "You need to add a payment method in your profile before approving orders",
                                "code", "NO_PAYMENT_METHOD"));
            }
            
            // Verify the principal has permission to approve these items
            // (Check if the authenticated user is the seller)
            
            // Process the approval and payment
            Orders updatedOrder = orderService.approveSellerItemsAndProcessPayment(orderId, sellerId);
            return ResponseEntity.ok(updatedOrder);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process approval: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/cancel-seller-items")
    public ResponseEntity<?> cancelSellerItems(
        @PathVariable Long orderId,
        @RequestBody Map<String, Long> requestBody,
        Principal principal
    ) {
        Long sellerId = requestBody.get("sellerId");
        if (sellerId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Seller ID is required"));
        }
        
        try {
            // Verify the principal has permission to cancel these items
            User currentUser = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            System.out.println("Cancel seller items request - Order ID: " + orderId + ", Seller ID: " + sellerId + 
                                ", User: " + principal.getName() + " (ID: " + currentUser.getUserId() + ")");
            
            // Only allow seller to cancel their own items
            if (!currentUser.getUserId().equals(sellerId)) {
                System.out.println("Permission denied: User ID " + currentUser.getUserId() + " tried to cancel items for Seller ID " + sellerId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only cancel your own items"));
            }
            
            Orders updatedOrder = orderService.cancelSellerItems(orderId, sellerId);
            System.out.println("Successfully cancelled seller items - New order status: " + updatedOrder.getOrderStatus());
            
            // Return more item details in response so frontend can update properly
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            System.err.println("Error cancelling seller items: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to cancel items: " + e.getMessage()));
        }
    }
}
