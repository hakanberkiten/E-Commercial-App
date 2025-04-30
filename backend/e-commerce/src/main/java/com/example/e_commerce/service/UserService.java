package com.example.e_commerce.service;

import com.example.e_commerce.dto.AddressDTO;
import com.example.e_commerce.entity.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    List<User> getAllUsers();
    User getUserById(Long id);
    User updateUser(Long id, Map<String, Object> updates); // Yeni metot
    List<AddressDTO> getUserAddresses(Long userId);
    void setDefaultAddress(Long userId, Long addressId);
    Optional<User> findByEmail(String email);
    void deleteUserWithAllData(Long userId);
}
