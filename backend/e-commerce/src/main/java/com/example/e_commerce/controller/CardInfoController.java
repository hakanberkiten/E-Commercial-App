package com.example.e_commerce.controller;


import com.example.e_commerce.dto.CardInfoDTO;
import com.example.e_commerce.service.CardInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cardinfo")
@RequiredArgsConstructor
public class CardInfoController {

    private final CardInfoService service;

    @PostMapping
    public ResponseEntity<CardInfoDTO> create(@Valid @RequestBody CardInfoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardInfoDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CardInfoDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardInfoDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody CardInfoDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
