// OrdersServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.dto.OrderItemRequest;
import com.example.e_commerce.dto.OrderRequest;
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
         // eger kullanıcı customer değilse hata fırlat
            if(user.getUserId() != 3) {
            throw new RuntimeException("User not authorized to place order");
        }
        
        // 2️⃣ (İsteğe bağlı) Ödeme bilgisi varsa al
        Payment payment = null;
        if (req.getPaymentId() != null) {
            payment = payRepo.findById(req.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
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

    // Implement the new methods in your OrdersServiceImpl

    @Override
    public List<Orders> getOrdersBySellerId(Long sellerId) {
        // Find all orders through order items that contain products from this seller
        // This is a more robust approach that doesn't require a custom repository method
        
        // 1. Get all orders
        List<Orders> allOrders = orderRepo.findAll();
        
        // 2. Filter orders that contain products from the specified seller
        return allOrders.stream()
            .filter(order -> order.getItems() != null && 
                    order.getItems().stream()
                        .anyMatch(item -> item.getProduct() != null && 
                                  item.getProduct().getSeller().getUserId() != null &&
                                  item.getProduct().getSeller().getUserId().equals(sellerId)))
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

// Helper method to validate order status
private boolean isValidOrderStatus(String status) {
    // Define the valid status values your application supports
    List<String> validStatuses = List.of("PENDING", "APPROVED", "SHIPPED", "DELIVERED", "CANCELLED");
    return validStatuses.contains(status);
}
}
