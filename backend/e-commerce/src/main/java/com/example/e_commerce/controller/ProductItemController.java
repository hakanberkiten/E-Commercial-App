package com.example.e_commerce.controller;

import com.example.e_commerce.dto.ProductItemDTO;
import com.example.e_commerce.service.ProductItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/product-items")
@RequiredArgsConstructor
public class ProductItemController {

    private final ProductItemService service;

    @PostMapping
    public ResponseEntity<ProductItemDTO> create(@Valid @RequestBody ProductItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductItemDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductItemDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductItemDTO>> getByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(service.getByProductId(productId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductItemDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody ProductItemDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
