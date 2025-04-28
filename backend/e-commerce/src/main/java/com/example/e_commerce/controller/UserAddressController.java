// UserAddressController.java
package com.example.e_commerce.controller;

import com.example.e_commerce.entity.UserAddress;
import com.example.e_commerce.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @PostMapping("/save")
    public UserAddress saveUserAddress(@RequestBody UserAddress ua) {
        return userAddressService.saveUserAddress(ua);
    }

    @GetMapping("/all")
    public List<UserAddress> getAll() {
        return userAddressService.getAllUserAddresses();
    }

    @GetMapping("/{id}")
    public UserAddress getById(@PathVariable Long id) {
        return userAddressService.getUserAddressById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        userAddressService.deleteUserAddress(id);
    }
}
