package com.example.e_commerce.controller;


import com.example.e_commerce.dto.CartItemDTO;
import com.example.e_commerce.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {
    private final CartItemService service;

    @PostMapping
    public ResponseEntity<CartItemDTO> create(@Valid @RequestBody CartItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItemDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartItemDTO>> getByCart(@PathVariable Integer cartId) {
        return ResponseEntity.ok(service.getByCartId(cartId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartItemDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody CartItemDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
