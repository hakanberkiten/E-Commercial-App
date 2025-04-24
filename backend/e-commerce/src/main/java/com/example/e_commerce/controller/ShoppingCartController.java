package com.example.e_commerce.controller;


import com.example.e_commerce.dto.ShoppingCartDTO;
import com.example.e_commerce.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class ShoppingCartController {
    private final ShoppingCartService service;

    @PostMapping
    public ResponseEntity<ShoppingCartDTO> create(@Valid @RequestBody ShoppingCartDTO dto) {
        ShoppingCartDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingCartDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ShoppingCartDTO> getByUser(@PathVariable Integer userId) {
        Optional<ShoppingCartDTO> opt = service.getByUserId(userId);
        return opt
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
