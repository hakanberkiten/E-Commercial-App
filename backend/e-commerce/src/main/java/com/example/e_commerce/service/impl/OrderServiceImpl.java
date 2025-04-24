package com.example.e_commerce.service.impl;

// src/main/java/com/example/ecommerce/service/impl/OrderServiceImpl.java

import com.example.e_commerce.dto.OrderDTO;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.*;
import com.example.e_commerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final UserRepository  userRepo;
    private final AddressRepository addressRepo;

    private OrderDTO toDto(Order e) {
        OrderDTO dto = new OrderDTO();
        dto.setId(e.getId());
        dto.setUserId(e.getUser().getId());
        dto.setOrderDate(e.getOrderDate());
        dto.setAddressId(e.getAddress().getId());
        dto.setPaymentMethod(e.getPaymentMethod());
        dto.setStatus(e.getStatus());
        dto.setTotalAmount(e.getTotalAmount());
        // items alanını boş bırakıyoruz; OrderItem endpoint’leri kullanılsın
        return dto;
    }

    private Order toEntity(OrderDTO dto) {
        Order e = new Order();
        User u = userRepo.findById(dto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User","id",dto.getUserId()));
        e.setUser(u);

        Address a = addressRepo.findById(dto.getAddressId())
            .orElseThrow(() -> new ResourceNotFoundException("Address","id",dto.getAddressId()));
        e.setAddress(a);

        e.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDateTime.now());
        e.setPaymentMethod(dto.getPaymentMethod());
        e.setStatus(dto.getStatus());
        e.setTotalAmount(dto.getTotalAmount());
        return e;
    }

    @Override
    public OrderDTO create(OrderDTO dto) {
        Order saved = orderRepo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public OrderDTO getById(Integer id) {
        return orderRepo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Order","id",id));
    }

    @Override
    public List<OrderDTO> getAll() {
        return orderRepo.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public OrderDTO update(Integer id, OrderDTO dto) {
        Order existing = orderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order","id",id));
        existing.setPaymentMethod(dto.getPaymentMethod());
        existing.setStatus(dto.getStatus());
        existing.setTotalAmount(dto.getTotalAmount());
        // tarih, user ve address değişimi genelde yapılmaz; eklemek istersen buraya koy
        Order updated = orderRepo.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        Order e = orderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order","id",id));
        orderRepo.delete(e);
    }
}

