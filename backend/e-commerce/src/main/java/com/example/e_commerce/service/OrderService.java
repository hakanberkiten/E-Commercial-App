package com.example.e_commerce.service;
// src/main/java/com/example/ecommerce/service/OrderService.java

import com.example.e_commerce.dto.OrderDTO;
import java.util.List;

public interface OrderService {
    OrderDTO create(OrderDTO dto);
    OrderDTO getById(Integer id);
    List<OrderDTO> getAll();
    OrderDTO update(Integer id, OrderDTO dto);
    void delete(Integer id);
}
