package com.example.e_commerce.controller;


import com.example.e_commerce.dto.OrderItemDTO;
import com.example.e_commerce.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService service;

    @PostMapping
    public ResponseEntity<OrderItemDTO> create(@Valid @RequestBody OrderItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderItemDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemDTO>> getByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(service.getByOrderId(orderId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody OrderItemDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
