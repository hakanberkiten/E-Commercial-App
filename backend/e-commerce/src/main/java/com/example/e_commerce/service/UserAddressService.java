// UserAddressService.java
package com.example.e_commerce.service;

import com.example.e_commerce.entity.UserAddress;

import java.util.List;

public interface UserAddressService {
    UserAddress saveUserAddress(UserAddress userAddress);
    List<UserAddress> getAllUserAddresses();
    UserAddress getUserAddressById(Long id);
    void deleteUserAddress(Long id);
    void deleteUserAddressByAddressId(Long addressId);
}
