// OrdersServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.dto.OrderItemRequest;
import com.example.e_commerce.dto.OrderRequest;
import com.example.e_commerce.dto.PaymentRequest;
import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.entity.Orders;
import com.example.e_commerce.entity.Payment;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.OrderItemRepository;
import com.example.e_commerce.repository.OrdersRepository;
import com.example.e_commerce.repository.PaymentRepository;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.OrdersService;
import com.example.e_commerce.service.NotificationService;
import com.example.e_commerce.service.PaymentService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {
    private final OrdersRepository orderRepo;
    private final PaymentRepository payRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderItemRepository itemRepo;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final StripeServiceImpl stripeService;

    @Override
    public Orders saveOrder(Orders order) {
        if (order.getPayment() != null) {
            Payment p = payRepo.findById(order.getPayment().getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            order.setPayment(p);
        }
        return orderRepo.save(order);
    }

    @Transactional
    @Override
    public Orders placeOrder(OrderRequest req) {
        // 1️⃣ Kullanıcıyı çek
        User user = userRepo.findById(req.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has appropriate role to place an order (ROLE_CUSTOMER or ROLE_SELLER)
        if(user.getRole() == null || (user.getRole().getRoleId() != 3 && user.getRole().getRoleId() != 2)) {
            throw new RuntimeException("User not authorized to place order");
        }
        
        // 2️⃣ Process payment if paymentMethodId is provided
        Payment payment = null;
        if (req.getPaymentMethodId() != null && user.getStripeCustomerId() != null) {
            // Calculate total amount from items
            BigDecimal totalAmount = req.getItems().stream()
                .map(item -> {
                    Product product = productRepo.findById(item.getProductId()).get();
                    return BigDecimal.valueOf(product.getPrice() * item.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Create payment request
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setUserId(user.getUserId());
            paymentRequest.setStripeCustomerId(user.getStripeCustomerId());
            paymentRequest.setPaymentMethodId(req.getPaymentMethodId());
            paymentRequest.setAmount(totalAmount);
            paymentRequest.setCurrency("USD");
            paymentRequest.setDescription("Payment for order");
            
            // Process the payment
            payment = paymentService.processPayment(paymentRequest);
        }

        // 3️⃣ Stok kontrolü ve stoktan düşme
        for (OrderItemRequest it : req.getItems()) {
            Product p = productRepo.findById(it.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
            if (p.getQuantityInStock() < it.getQuantity()) {
                throw new RuntimeException(
                    "Insufficient stock for product " + p.getProductId()
                );
            }
            p.setQuantityInStock(p.getQuantityInStock() - it.getQuantity());
            productRepo.save(p);
        }

        // 4️⃣ Yeni siparişi oluştur ve kaydet
        Orders order = Orders.builder()
            .user(user)
            .email(user.getEmail())
            .orderDate(LocalDate.now())
            .orderStatus("PENDING")
            .payment(payment)
            .totalAmount(BigDecimal.ZERO)  // sonra güncelleyeceğiz
            .build();
        order = orderRepo.save(order);

        // 5️⃣ Her kalem için OrderItem yarat, kaydet, listeye ekle
        List<OrderItem> savedItems = new ArrayList<>();
        for (OrderItemRequest it : req.getItems()) {
            Product p = productRepo.findById(it.getProductId()).get();  // varlığı garanti
            OrderItem oi = OrderItem.builder()
                .order(order)
                .product(p)
                .quantityInOrder(it.getQuantity())
                .orderedProductPrice(
                    BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(it.getQuantity()))
                )
                .build();
            savedItems.add(itemRepo.save(oi));
        }
        order.setItems(savedItems);

        // 6️⃣ Toplam tutarı hesapla ve siparişi güncelle
        BigDecimal total = savedItems.stream()
            .map(OrderItem::getOrderedProductPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        // Add notification after successful order placement
        notificationService.createNotification(
            order.getUser().getUserId(),
            "Your order #" + order.getOrderId() + " has been placed successfully!",
            "ORDER",
            "/orders/" + order.getOrderId()
        );

        // 7️⃣ Son kez kaydedip döndür
        return orderRepo.save(order);
    }

    @Override
    public List<Orders> getAllOrders() { return orderRepo.findAll(); }

    @Override
    public Orders getOrderById(Long id) {
        return orderRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepo.deleteById(id);
    }

    @Override
    public List<Orders> getOrdersBySellerId(Long sellerId) {
        // Find all orders through order items that contain products from this seller
        // This is a more robust approach that doesn't require a custom repository method
        
        // 1. Get all orders
        List<Orders> allOrders = orderRepo.findAll();
        
        // 2. Filter orders that contain products from the specified seller
        // AND are not cancelled orders
        return allOrders.stream()
            .filter(order -> 
                // Check that the order has items that belong to this seller
                order.getItems() != null && 
                order.getItems().stream()
                    .anyMatch(item -> item.getProduct() != null && 
                              item.getProduct().getSeller() != null &&
                              item.getProduct().getSeller().getUserId() != null &&
                              item.getProduct().getSeller().getUserId().equals(sellerId)) &&
                // And the order is not cancelled
                !"CANCELLED".equals(order.getOrderStatus())
            )
            .peek(order -> {
                // Filter the order items to only include those for this seller's products
                // This ensures sellers only see their own products in each order
                List<OrderItem> sellerItems = order.getItems().stream()
                    .filter(item -> item.getProduct() != null && 
                            item.getProduct().getSeller() != null &&
                            item.getProduct().getSeller().getUserId().equals(sellerId))
                    .toList();
                
                // Create a new list with only the seller's items
                order.setItems(new ArrayList<>(sellerItems));
            })
            .toList();
    }

    @Override
    public Orders updateOrderStatus(Long orderId, String status) {
        Orders order = orderRepo.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Validate status using a simple check for allowed values
        String upperCaseStatus = status.toUpperCase();
        if (!isValidOrderStatus(upperCaseStatus)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        
        // Set the status directly as a string
        order.setOrderStatus(upperCaseStatus);
        return orderRepo.save(order);
    }

    @Transactional
    @Override
    public Orders refundAndCancelOrder(Long orderId) {
        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        // Only allow refund if the order status is PENDING or APPROVED
        if (!"PENDING".equals(order.getOrderStatus()) && 
            !"APPROVED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Order cannot be refunded in its current status: " + order.getOrderStatus());
        }
        
        // Return items to inventory
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    // Add the ordered quantity back to stock
                    int newQuantity = product.getQuantityInStock() + item.getQuantityInOrder();
                    product.setQuantityInStock(newQuantity);
                    productRepo.save(product);
                    
                    System.out.println("Returned " + item.getQuantityInOrder() + " units of product " + 
                        product.getProductId() + " to stock. New quantity: " + newQuantity);
                }
            }
        }
        
        // If there's a payment associated with the order
        if (order.getPayment() != null) {
            Payment payment = order.getPayment();
            
            try {
                // Process refund through Stripe
                stripeService.refundPayment(payment.getStripePaymentIntentId());
                
                // Update payment status
                payment.setStatus("REFUNDED");
                payRepo.save(payment);
                
                // Create notification for customer
                if (order.getUser() != null) {
                    notificationService.createNotification(
                        order.getUser().getUserId(),
                        "Your order #" + orderId + " has been cancelled and refunded",
                        "ORDER_CANCELLED",
                        "/profile?tab=orders"
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process refund: " + e.getMessage());
            }
        }
        
        // Update order status
        order.setOrderStatus("CANCELLED");
        return orderRepo.save(order);
    }

    @Transactional
    @Override
    public Orders approveSellerItems(Long orderId, Long sellerId) {
        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Check if order status allows approval
        if ("CANCELLED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Cannot approve items for a cancelled order");
        }
        
        boolean foundSellerItems = false;
        boolean allItemsApproved = true;
        
        // Update status of this seller's items
        for (OrderItem item : order.getItems()) {
            // Check if this item belongs to the current seller
            if (item.getProduct() != null && 
                item.getProduct().getSeller() != null && 
                item.getProduct().getSeller().getUserId().equals(sellerId)) {
                
                // Mark this seller's item as shipped
                item.setItemStatus("SHIPPED");
                itemRepo.save(item);
                foundSellerItems = true;
                
                // Notify customer that seller has approved their items
                if (order.getUser() != null) {
                    notificationService.createNotification(
                        order.getUser().getUserId(),
                        "Items from " + item.getProduct().getSeller().getFirstName() + 
                        " in your order #" + orderId + " have been shipped!",
                        "ORDER_PARTIAL_SHIPPED",
                        "/profile?tab=orders"
                    );
                }
            } else if (!"SHIPPED".equals(item.getItemStatus())) {
                // If any item is not shipped yet, the whole order isn't ready
                allItemsApproved = false;
            }
        }
        
        if (!foundSellerItems) {
            throw new RuntimeException("No items found for this seller in the order");
        }
        
        // If all items are now approved, update the overall order status
        if (allItemsApproved) {
            order.setOrderStatus("SHIPPED");
            
            // Send a final notification that the entire order is shipped
            if (order.getUser() != null) {
                notificationService.createNotification(
                    order.getUser().getUserId(),
                    "Great news! Your complete order #" + orderId + " has been shipped!",
                    "ORDER_SHIPPED",
                    "/profile?tab=orders"
                );
            }
        }
        
        return orderRepo.save(order);
    }

    // Helper method to validate order status
    private boolean isValidOrderStatus(String status) {
        // Define the valid status values your application supports
        List<String> validStatuses = List.of("PENDING", "APPROVED", "SHIPPED", "DELIVERED", "CANCELLED");
        return validStatuses.contains(status);
    }
}
