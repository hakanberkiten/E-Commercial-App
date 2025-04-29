// UserAddressRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.UserAddress;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUser(User user);
    Optional<UserAddress> findByUserAndAddressAddressId(User user, Long addressId);

List<UserAddress> findByAddressAddressId(Long addressId);
}
