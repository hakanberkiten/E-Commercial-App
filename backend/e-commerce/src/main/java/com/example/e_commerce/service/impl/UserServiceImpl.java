package com.example.e_commerce.service.impl;

import com.example.e_commerce.dto.AddressDTO;
import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.UserAddress;
import com.example.e_commerce.entity.Address;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.UserAddressRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

   

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserAddressRepository userAddressRepository;


    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
public List<AddressDTO> getUserAddresses(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            
    // UserAddress tablosundan kullanıcının tüm adreslerini al
    List<UserAddress> userAddresses = userAddressRepository.findByUser(user);
    
    return userAddresses.stream().map(ua -> {
        Address address = ua.getAddress();
        return AddressDTO.builder()
                .id(address.getAddressId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getPincode())
                .country(address.getCountry())
                .buildingName(address.getBuildingName())
                .isDefault(ua.getIsDefault())
                .build();
    }).collect(Collectors.toList());
}

@Override
public void setDefaultAddress(Long userId, Long addressId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            
    // Önce tüm adreslerin varsayılan değerini false yap
    List<UserAddress> userAddresses = userAddressRepository.findByUser(user);
    userAddresses.forEach(ua -> {
        ua.setIsDefault(false);
        userAddressRepository.save(ua);
    });
    
    // Seçilen adresi varsayılan olarak ayarla
    UserAddress defaultAddress = userAddressRepository.findByUserAndAddressAddressId(user, addressId)
            .orElseThrow(() -> new RuntimeException("Address not found for this user"));
    defaultAddress.setIsDefault(true);
    userAddressRepository.save(defaultAddress);
}
    @Override
public User saveUser(User user) {
    Integer roleId = user.getRole().getRoleId();
    Role role = roleRepository.findById(roleId)
                               .orElseThrow(() -> new RuntimeException("Role not found"));

    user.setRole(role);  
    return userRepository.save(user);
}

@Override
public User updateUser(Long id, Map<String, Object> updates) {
    User existingUser = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    
    // Sadece güncellenebilir alanları güncelleme
    if (updates.containsKey("firstName")) {
        existingUser.setFirstName((String) updates.get("firstName"));
    }
    
    if (updates.containsKey("lastName")) {
        existingUser.setLastName((String) updates.get("lastName"));
    }
    
    if (updates.containsKey("mobileNumber")) {
        existingUser.setMobileNumber((String) updates.get("mobileNumber"));
    }
    
    // Email ve şifre gibi hassas alanları güncelleme mekanizması farklı olmalı
    // Bu metot sadece temel profil bilgilerini günceller
    
    return userRepository.save(existingUser);
}


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }



    
}
