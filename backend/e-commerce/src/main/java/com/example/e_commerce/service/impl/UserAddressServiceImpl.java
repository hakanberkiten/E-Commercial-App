// UserAddressServiceImpl.java
package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Address;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.UserAddress;
import com.example.e_commerce.repository.AddressRepository;
import com.example.e_commerce.repository.UserAddressRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {
    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public UserAddress saveUserAddress(UserAddress ua) {
        // JSON’dan gelen sadece ID’ler var, eksiksiz ilişkiyi kurmak için önce DB’den çekiyoruz:
        User user = userRepository.findById(ua.getUser().getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        Address address = addressRepository.findById(ua.getAddress().getAddressId())
            .orElseThrow(() -> new RuntimeException("Address not found"));

        ua.setUser(user);
        ua.setAddress(address);
        return userAddressRepository.save(ua);
    }
// UserAddressServiceImpl sınıfına ekleyin

@Override
public void deleteUserAddressByAddressId(Long addressId) {
    List<UserAddress> userAddresses = userAddressRepository.findByAddressAddressId(addressId);
    userAddressRepository.deleteAll(userAddresses);
}

@Override
    public List<UserAddress> findByUser(User user) {
        return userAddressRepository.findByUser(user);
    }

    @Override
    public List<UserAddress> findByAddressAddressId(Long addressId) {
        return userAddressRepository.findByAddressAddressId(addressId);
}
    @Override
    public List<UserAddress> getAllUserAddresses() {
        return userAddressRepository.findAll();
    }

    @Override
    public UserAddress getUserAddressById(Long id) {
        return userAddressRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("UserAddress not found"));
    }

    @Override
    public void deleteUserAddress(Long id) {
        userAddressRepository.deleteById(id);
    }
}
