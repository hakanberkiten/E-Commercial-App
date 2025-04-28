// AddressService.java
package com.example.e_commerce.service;

import com.example.e_commerce.entity.Address;

import java.util.List;

public interface AddressService {
    Address saveAddress(Address address);
    List<Address> getAllAddresses();
    Address getAddressById(Long id);
    void deleteAddress(Long id);
}
