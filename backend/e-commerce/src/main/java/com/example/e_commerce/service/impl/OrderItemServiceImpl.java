package com.example.e_commerce.service.impl;


import com.example.e_commerce.dto.OrderItemDTO;
import com.example.e_commerce.entity.Order;
import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.entity.ProductItem;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.OrderItemRepository;
import com.example.e_commerce.repository.OrderRepository;
import com.example.e_commerce.repository.ProductItemRepository;
import com.example.e_commerce.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository repo;
    private final OrderRepository orderRepo;
    private final ProductItemRepository productItemRepo;

    private OrderItemDTO toDto(OrderItem e) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(e.getId());
        dto.setOrderId(e.getOrder().getId());
        dto.setProductItemId(e.getProductItem().getId());
        dto.setQuantity(e.getQuantity());
        return dto;
    }

    private OrderItem toEntity(OrderItemDTO dto) {
        OrderItem e = new OrderItem();
        Order order = orderRepo.findById(dto.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order","id",dto.getOrderId()));
        ProductItem pi = productItemRepo.findById(dto.getProductItemId())
            .orElseThrow(() -> new ResourceNotFoundException("ProductItem","id",dto.getProductItemId()));
        e.setOrder(order);
        e.setProductItem(pi);
        e.setQuantity(dto.getQuantity());
        return e;
    }

    @Override
    public OrderItemDTO create(OrderItemDTO dto) {
        OrderItem saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public OrderItemDTO getById(Integer id) {
        return repo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("OrderItem","id",id));
    }

    @Override
    public List<OrderItemDTO> getAll() {
        return repo.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<OrderItemDTO> getByOrderId(Integer orderId) {
        return repo.findByOrderId(orderId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public OrderItemDTO update(Integer id, OrderItemDTO dto) {
        OrderItem existing = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OrderItem","id",id));
        if (dto.getQuantity() != null) {
            existing.setQuantity(dto.getQuantity());
        }
        // Ürün veya sipariş değişimi genelde yapılmaz, gerekirse ekleyebilirsin
        OrderItem updated = repo.save(existing);
        return toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        OrderItem e = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OrderItem","id",id));
        repo.delete(e);
    }
}
