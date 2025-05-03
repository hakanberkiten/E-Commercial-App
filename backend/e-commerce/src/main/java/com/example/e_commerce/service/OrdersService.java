// OrdersService.java
package com.example.e_commerce.service;

import com.example.e_commerce.dto.OrderRequest;
import com.example.e_commerce.entity.Orders;

import java.util.List;

public interface OrdersService {
    Orders saveOrder(Orders order);
    List<Orders> getAllOrders();
    Orders getOrderById(Long id);
    void deleteOrder(Long id);
    Orders placeOrder(OrderRequest request);
    List<Orders> getOrdersBySellerId(Long sellerId);
    List<Orders> getOrdersByUserId(Long userId);
    Orders updateOrderStatus(Long orderId, String status);
    Orders refundAndCancelOrder(Long orderId);
    Orders approveSellerItems(Long orderId, Long sellerId);
    Orders approveSellerItemsAndProcessPayment(Long orderId, Long sellerId);
    Orders getOrderByIdFresh(Long id);
    Orders cancelSellerItems(Long orderId, Long sellerId);
    Orders refundDeliveredOrder(Long orderId);
}
