// UserAddressRepository.java
package com.example.e_commerce.repository;

import com.example.e_commerce.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
}
