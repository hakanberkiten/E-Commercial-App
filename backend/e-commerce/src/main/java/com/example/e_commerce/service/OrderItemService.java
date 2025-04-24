package com.example.e_commerce.service;

import com.example.e_commerce.dto.OrderItemDTO;
import java.util.List;

public interface OrderItemService {
    OrderItemDTO create(OrderItemDTO dto);
    OrderItemDTO getById(Integer id);
    List<OrderItemDTO> getAll();
    List<OrderItemDTO> getByOrderId(Integer orderId);
    OrderItemDTO update(Integer id, OrderItemDTO dto);
    void delete(Integer id);
}
