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
import com.example.e_commerce.service.EmailService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
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
    private final EntityManager entityManager;
    private final EmailService emailService;

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
        Orders savedOrder = orderRepo.save(order);

        // Send email confirmation if this is a valid email
        try {
            // Your existing email sending code
            emailService.sendOrderConfirmation(savedOrder);
        } catch (Exception e) {
            // Log the error but don't throw it
            System.out.println("Failed to send order confirmation email: " + e.getMessage());
            // Don't rethrow - let the order complete anyway
        }
        return savedOrder;
    }

    @Override
    public List<Orders> getAllOrders() { return orderRepo.findAll(); }

    @Override
    public Orders getOrderById(Long id) {
        return orderRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public Orders getOrderByIdFresh(Long id) {
        // Clear any first level cache (Hibernate)
        entityManager.clear();
        
        // Log işlemi başlangıcı
        System.out.println("Fetching fresh order details for order ID: " + id);
        
        // Then load the order with all its items eagerly
        Orders order = orderRepo.findWithItemsById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
            
        // Order status değerini loglayarak kontrol et
        System.out.println("Fresh order " + id + " status: " + order.getOrderStatus());
        if (order.getItems() != null) {
            System.out.println("Items count: " + order.getItems().size());
            for (OrderItem item : order.getItems()) {
                System.out.println("Item " + item.getOrderItemId() + " status: " + item.getItemStatus());
            }
        }
        
        return order;
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
        // AND are not completely cancelled orders
        return allOrders.stream()
            .filter(order -> 
                // Check that the order has items that belong to this seller
                order.getItems() != null && 
                // And at least one of those items is NOT cancelled for this seller
                order.getItems().stream()
                    .anyMatch(item -> 
                        item.getProduct() != null && 
                        item.getProduct().getSeller() != null &&
                        item.getProduct().getSeller().getUserId() != null &&
                        item.getProduct().getSeller().getUserId().equals(sellerId) &&
                        !"CANCELLED".equals(item.getItemStatus()))
            )
            .peek(order -> {
                // Filter the order items to only include those for this seller's products
                // that are NOT cancelled
                List<OrderItem> sellerItems = order.getItems().stream()
                    .filter(item -> 
                        item.getProduct() != null && 
                        item.getProduct().getSeller() != null &&
                        item.getProduct().getSeller().getUserId().equals(sellerId) &&
                        !"CANCELLED".equals(item.getItemStatus()))
                    .toList();
                
                // Create a new list with only the seller's active items
                order.setItems(new ArrayList<>(sellerItems));
            })
            // Remove orders where we filtered out all items (empty orders)
            .filter(order -> !order.getItems().isEmpty())
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
        
        // Update all order items to match the order status
        // Not just for DELIVERED but for ANY status change
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                // Log before update
                System.out.println("Updating item #" + item.getOrderItemId() + " status from " + 
                                  item.getItemStatus() + " to " + upperCaseStatus);
                
                // Update item status
                item.setItemStatus(upperCaseStatus);
                itemRepo.save(item);
            }
            
            System.out.println("Updated all items for order #" + orderId + " to status: " + upperCaseStatus);
        }
        
        return orderRepo.save(order);
    }

    @Transactional
    @Override
    public Orders refundAndCancelOrder(Long orderId) {
        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        // Check if order is already cancelled
        if ("CANCELLED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Order is already cancelled");
        }
        
        // Don't allow cancellation of delivered orders
        if ("DELIVERED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Delivered orders cannot be cancelled");
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
                    
                    // If seller has already been paid for this item, deduct the earnings
                    if ("SHIPPED".equals(item.getItemStatus()) && item.getProduct().getSeller() != null) {
                        User seller = item.getProduct().getSeller();
                        
                        // Calculate the original seller earnings for this item
                        BigDecimal itemPrice = BigDecimal.valueOf(item.getProduct().getPrice());
                        BigDecimal quantity = BigDecimal.valueOf(item.getQuantityInOrder());
                        BigDecimal subtotal = itemPrice.multiply(quantity);
                        
                        // Apply the same platform fee calculation that was used for earnings (e.g., 10%)
                        BigDecimal platformFeePercent = new BigDecimal("0.10");
                        BigDecimal platformFee = subtotal.multiply(platformFeePercent);
                        BigDecimal sellerAmount = subtotal.subtract(platformFee);
                        
                        // Track this deduction by creating a negative payment record
                        Payment deductionPayment = Payment.builder()
                            .user(seller)
                            .amount(sellerAmount.negate()) // Use negative amount to indicate deduction
                            .currency("USD")
                            .paymentMethod("stripe_deduction")
                            .status("DEDUCTED")
                            .build();
                        
                        payRepo.save(deductionPayment);
                        
                        // Send notification to seller about earnings deduction
                        notificationService.createNotification(
                            seller.getUserId(),
                            "A refund has been issued for order #" + orderId + ". $" + sellerAmount + 
                            " has been deducted from your earnings.",
                            "EARNINGS_DEDUCTION",
                            "/profile?tab=earnings"
                        );
                    }
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
        
        // Also update item statuses
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setItemStatus("CANCELLED");
                itemRepo.save(item);
            }
        }
        
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

    @Transactional
    @Override
    public Orders approveSellerItemsAndProcessPayment(Long orderId, Long sellerId) {
        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Check if order status allows approval
        if ("CANCELLED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Cannot approve items for a cancelled order");
        }
        
        boolean foundSellerItems = false;
        boolean allItemsApproved = true;
        BigDecimal sellerEarnings = BigDecimal.ZERO;
        
        // Update status of this seller's items and calculate earnings
        for (OrderItem item : order.getItems()) {
            // Check if this item belongs to the current seller
            if (item.getProduct() != null && 
                item.getProduct().getSeller() != null && 
                item.getProduct().getSeller().getUserId().equals(sellerId)) {
                
                // Mark this seller's item as shipped
                item.setItemStatus("SHIPPED");
                itemRepo.save(item);
                foundSellerItems = true;
                
                // Calculate earnings for this item
                BigDecimal itemPrice = BigDecimal.valueOf(item.getProduct().getPrice());
                BigDecimal quantity = BigDecimal.valueOf(item.getQuantityInOrder());
                BigDecimal subtotal = itemPrice.multiply(quantity);
                
                // Apply platform fee (e.g., 10%)
                BigDecimal platformFeePercent = new BigDecimal("0.10");
                BigDecimal platformFee = subtotal.multiply(platformFeePercent);
                BigDecimal sellerAmount = subtotal.subtract(platformFee);
                
                sellerEarnings = sellerEarnings.add(sellerAmount);
                
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
        
        // Process payment to seller
        if (sellerEarnings.compareTo(BigDecimal.ZERO) > 0) {
            User seller = userRepo.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
            
            try {
                // Process payment to seller account
                Payment paymentRecord = Payment.builder()
                    .user(seller)
                    .amount(sellerEarnings)
                    .currency("USD")
                    .paymentMethod("stripe_transfer")
                    .status("COMPLETED")
                    .build();
                
                payRepo.save(paymentRecord);
                
                // Create a notification for the seller about earnings
                notificationService.createNotification(
                    sellerId,
                    "You've earned $" + sellerEarnings + " from order #" + orderId,
                    "EARNINGS",
                    "/profile?tab=earnings"
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to process seller payment: " + e.getMessage());
            }
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

    @Transactional
    @Override
    public Orders cancelSellerItems(Long orderId, Long sellerId) {
        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        // Don't allow cancellation if order is already delivered
        if ("DELIVERED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Cannot cancel items from a delivered order");
        }
        
        boolean foundSellerItems = false;
        BigDecimal refundAmount = BigDecimal.ZERO;
        
        // 1. Önce tüm öğeleri kontrol et ve kaç tane iptal edilmemiş öğe kaldığını say
        int remainingNonCancelledItems = 0;
        int sellerItemCount = 0;
        
        for (OrderItem item : order.getItems()) {
            // Satıcıya ait öğeleri say
            if (item.getProduct() != null && 
                item.getProduct().getSeller() != null && 
                item.getProduct().getSeller().getUserId().equals(sellerId)) {
                sellerItemCount++;
            }
            
            // Henüz iptal edilmemiş öğeleri say
            if (!"CANCELLED".equals(item.getItemStatus())) {
                remainingNonCancelledItems++;
            }
        }
        
        // Son satıcı olup olmadığını kontrol et - kalan iptal edilmemiş öğe sayısı bu satıcının öğe sayısına eşitse
        boolean isLastSeller = remainingNonCancelledItems <= sellerItemCount;
        System.out.println("Remaining non-cancelled items: " + remainingNonCancelledItems + ", Seller items: " + sellerItemCount + ", Is last seller: " + isLastSeller);
        
        // 2. Şimdi satıcının öğelerini iptal et ve iade miktarını hesapla
        for (OrderItem item : order.getItems()) {
            // Check if this item belongs to the current seller
            if (item.getProduct() != null && 
                item.getProduct().getSeller() != null && 
                item.getProduct().getSeller().getUserId().equals(sellerId)) {
                
                // Only process if item isn't already cancelled
                if (!"CANCELLED".equals(item.getItemStatus())) {
                    System.out.println("Cancelling item ID: " + item.getOrderItemId() + ", previous status: " + item.getItemStatus());
                    
                    // Return product to inventory
                    Product product = item.getProduct();
                    int newQuantity = product.getQuantityInStock() + item.getQuantityInOrder();
                    product.setQuantityInStock(newQuantity);
                    productRepo.save(product);
                    
                    System.out.println("Returned " + item.getQuantityInOrder() + " units of product " + 
                        product.getProductId() + " to stock. New quantity: " + newQuantity);
                    
                    // Add to refund amount
                    refundAmount = refundAmount.add(item.getOrderedProductPrice());
                    
                    // Mark item as cancelled
                    item.setItemStatus("CANCELLED");
                    itemRepo.save(item);
                    foundSellerItems = true;
                    
                    // Deduct from seller earnings if the item was already shipped
                    if ("SHIPPED".equals(item.getItemStatus()) || "APPROVED".equals(item.getItemStatus())) {
                        deductSellerEarnings(item, orderId);
                    }
                }
            }
        }
        
        if (!foundSellerItems) {
            throw new RuntimeException("No items found for this seller in the order");
        }
        
        // 3. İade işlemini yap - eğer son satıcı ise tam iade, değilse kısmi iade yap
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0 && order.getPayment() != null) {
            try {
                if (isLastSeller) {
                    // Son satıcı için tam iade işlemi yap
                    System.out.println("Processing full refund for last seller");
                    stripeService.refundPayment(order.getPayment().getStripePaymentIntentId());
                    
                    // Tam iadeyi kaydet
                    Payment refundPayment = Payment.builder()
                        .user(order.getUser())
                        .amount(order.getTotalAmount().negate()) // Tüm sipariş tutarı
                        .currency("USD")
                        .paymentMethod("refund")
                        .status("REFUNDED")
                        .build();
                    payRepo.save(refundPayment);
                    
                    // Müşteriye tam iade bildirimi gönder
                    notificationService.createNotification(
                        order.getUser().getUserId(),
                        "All items in your order #" + orderId + " have been cancelled. A full refund of $" + 
                        order.getTotalAmount() + " has been processed.",
                        "ORDER_FULLY_CANCELLED",
                        "/profile?tab=orders"
                    );
                } else {
                    // Kısmi iade işlemi yap
                    System.out.println("Processing partial refund of $" + refundAmount);
                    stripeService.createPartialRefund(order.getPayment().getStripePaymentIntentId(), refundAmount);
                    
                    // Kısmi iadeyi kaydet
                    Payment refundPayment = Payment.builder()
                        .user(order.getUser())
                        .amount(refundAmount.negate())
                        .currency("USD")
                        .paymentMethod("refund")
                        .status("REFUNDED")
                        .build();
                    payRepo.save(refundPayment);
                    
                    // Müşteriye kısmi iade bildirimi gönder
                    notificationService.createNotification(
                        order.getUser().getUserId(),
                        "Some items in your order #" + orderId + " have been cancelled. $" + refundAmount + 
                        " has been refunded to your original payment method.",
                        "PARTIAL_ORDER_CANCELLED",
                        "/profile?tab=orders"
                    );
                }
            } catch (Exception e) {
                System.err.println("Refund error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to process refund: " + e.getMessage());
            }
        }
        
        // 4. Sipariş durumunu güncelle
        if (isLastSeller) {
            // Bu son satıcı ise, siparişi tamamen iptal edilmiş olarak işaretle
            order.setOrderStatus("CANCELLED");
        } else {
            // Değilse, kalan öğelerin durumuna göre sipariş durumunu güncelle
            updateOrderStatusBasedOnItems(order);
        }
        
        Orders savedOrder = orderRepo.save(order);
        
        // Force refresh order data to ensure all clients see current state
        entityManager.flush();
        entityManager.refresh(savedOrder);
        
        return savedOrder;
    }

    @Override
    public List<Orders> getOrdersByUserId(Long userId) {
        // Find orders belonging to this user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        return orderRepo.findByUserOrderByOrderDateDesc(user);
    }

    // Helper method to deduct seller earnings
    private void deductSellerEarnings(OrderItem item, Long orderId) {
        User seller = item.getProduct().getSeller();
        
        // Calculate the original seller earnings for this item
        BigDecimal itemPrice = BigDecimal.valueOf(item.getProduct().getPrice());
        BigDecimal quantity = BigDecimal.valueOf(item.getQuantityInOrder());
        BigDecimal subtotal = itemPrice.multiply(quantity);
        
        // Apply platform fee calculation
        BigDecimal platformFeePercent = new BigDecimal("0.10");
        BigDecimal platformFee = subtotal.multiply(platformFeePercent);
        BigDecimal sellerAmount = subtotal.subtract(platformFee);
        
        // Track this deduction with a negative payment record
        Payment deductionPayment = Payment.builder()
            .user(seller)
            .amount(sellerAmount.negate())
            .currency("USD")
            .paymentMethod("stripe_deduction")
            .status("DEDUCTED")
            .build();
        
        payRepo.save(deductionPayment);
        
        // Notify seller about earnings deduction
        notificationService.createNotification(
            seller.getUserId(),
            "Items from order #" + orderId + " have been cancelled. $" + sellerAmount + 
            " has been deducted from your earnings.",
            "EARNINGS_DEDUCTION",
            "/profile?tab=earnings"
        );
    }

    // Helper method to update order status based on remaining items
    private void updateOrderStatusBasedOnItems(Orders order) {
        // Count items by status
        int totalItems = 0;
        int cancelledItems = 0;
        int shippedItems = 0;
        int deliveredItems = 0;
        
        for (OrderItem item : order.getItems()) {
            totalItems++;
            switch (item.getItemStatus()) {
                case "CANCELLED":
                    cancelledItems++;
                    break;
                case "SHIPPED":
                    shippedItems++;
                    break;
                case "DELIVERED":
                    deliveredItems++;
                    break;
            }
        }
        
        // Determine overall order status
        if (cancelledItems == totalItems) {
            // All items cancelled = order cancelled
            order.setOrderStatus("CANCELLED");
        } else if (deliveredItems > 0) {
            // Any items delivered = show as partially or fully delivered
            if (deliveredItems + cancelledItems == totalItems) {
                order.setOrderStatus("DELIVERED");
            } else {
                order.setOrderStatus("PARTIALLY_DELIVERED");
            }
        } else if (shippedItems > 0) {
            // Any items shipped = show as partially or fully shipped
            if (shippedItems + cancelledItems == totalItems) {
                order.setOrderStatus("SHIPPED");
            } else {
                order.setOrderStatus("PARTIALLY_SHIPPED");
            }
        } else {
            // Otherwise pending
            order.setOrderStatus("PENDING");
        }
    }

    // Helper method to validate order status
    private boolean isValidOrderStatus(String status) {
        // Define the valid status values your application supports
        List<String> validStatuses = List.of("PENDING", "APPROVED", "SHIPPED", "DELIVERED", "CANCELLED");
        return validStatuses.contains(status);
    }
}
