package com.example.e_commerce.controller;


import com.example.e_commerce.dto.AddressDTO;
import com.example.e_commerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDTO> create(@Valid @RequestBody AddressDTO dto) {
        AddressDTO created = addressService.create(dto);
        return ResponseEntity
            .status(201)
            .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(addressService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDTO>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(addressService.getAllByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody AddressDTO dto) {
        return ResponseEntity.ok(addressService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

